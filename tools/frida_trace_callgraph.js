var TARGET_PACKAGE = '%TARGET_PACKAGE%';
var EXCLUDE_REGEX = [%EXCLUDE_REGEX%];
var BUFFER_SIZE = 200;
var FLUSH_INTERVAL_MS = 250;
var SCAN_INTERVAL_MS = 3000;

var callStacks = new Map();
var edgeCounts = new Map();
var hookedClasses = new Set();
var hookedMethodCount = 0;
var pendingClasses = [];
var scanning = false;

function getThreadStack() {
    var tid = Process.getCurrentThreadId();
    if (!callStacks.has(tid)) callStacks.set(tid, []);
    return callStacks.get(tid);
}

function flushEdges() {
    if (edgeCounts.size === 0) return;
    var batch = [];
    edgeCounts.forEach(function (count, key) {
        var parts = key.split('|||');
        batch.push({ caller: parts[0], callee: parts[1], count: count });
    });
    send({ type: 'edges', data: batch });
    edgeCounts.clear();
}

function recordEdge(caller, callee) {
    var key = caller + '|||' + callee;
    edgeCounts.set(key, (edgeCounts.get(key) || 0) + 1);
    if (edgeCounts.size >= BUFFER_SIZE) flushEdges();
}

function getSignature(className, methodName, overload) {
    var paramTypes = [];
    try {
        paramTypes = overload.argumentTypes.map(function (t) { return String(t.className || t); });
    } catch (e) {
        paramTypes = [];
    }
    return className + '.' + methodName + '(' + paramTypes.join(',') + ')';
}

function shouldExclude(className) {
    for (var i = 0; i < EXCLUDE_REGEX.length; i++) {
        if (EXCLUDE_REGEX[i].test(className)) return true;
    }
    return false;
}

function isSafeToHook(methodName) {
    var unsafe = ['toString', 'hashCode', 'equals', 'getClass', 'finalize', 'clone', 'wait', 'notify', 'notifyAll'];
    return unsafe.indexOf(methodName) === -1;
}

function hookClass(className) {
    var clazz = Java.use(className);
    var methods = clazz.class.getDeclaredMethods();
    var hooked = 0;
    for (var mi = 0; mi < methods.length; mi++) {
        var name = methods[mi].getName();
        if (name === '<clinit>' || name === '<init>' || !isSafeToHook(name)) continue;
        try {
            var prop = clazz[name];
            if (!prop || typeof prop !== 'function' || !prop.overloads) continue;
            prop.overloads.forEach(function (overload) {
                var sig = getSignature(className, name, overload);
                try {
                    var origImpl = overload.implementation;
                    if (typeof origImpl !== 'function') {
                        send({ type: 'log', msg: 'Skip ' + sig + ' - no original implementation' });
                        return;
                    }
                    overload.implementation = function () {
                        var stack = getThreadStack();
                        var caller = stack.length > 0 ? stack[stack.length - 1] : '_ROOT_';
                        stack.push(sig);
                        recordEdge(caller, sig);
                        try {
                            return origImpl.apply(this, arguments);
                        } finally {
                            stack.pop();
                        }
                    };
                    hooked++;
                } catch (ex) {
                    send({ type: 'log', msg: 'Hook fail: ' + sig + ' -> ' + ex });
                }
            });
        } catch (ex) {
            send({ type: 'log', msg: 'Prop fail: ' + className + '.' + name + ' -> ' + ex });
        }
    }
    hookedMethodCount += hooked;
    return hooked;
}

function hookPendingClasses() {
    if (pendingClasses.length === 0) return;
    var batch = pendingClasses.splice(0, pendingClasses.length);
    Java.perform(function () {
        var totalHooked = 0;
        for (var i = 0; i < batch.length; i++) {
            var cn = batch[i];
            if (!hookedClasses.has(cn)) {
                try {
                    var h = hookClass(cn);
                    totalHooked += h;
                    hookedClasses.add(cn);
                } catch (ex) {
                    send({ type: 'log', msg: 'HookClass error: ' + cn + ' -> ' + ex });
                }
            }
        }
        send({ type: 'scanDone', hookedCount: hookedClasses.size, hookedMethods: hookedMethodCount });
    });
}

function scanClasses() {
    if (scanning) return;
    scanning = true;
    Java.perform(function () {
        try {
            Java.enumerateLoadedClasses({
                onMatch: function (className) {
                    if (className.indexOf(TARGET_PACKAGE) !== 0) return;
                    if (hookedClasses.has(className)) return;
                    if (shouldExclude(className)) return;
                    pendingClasses.push(className);
                },
                onComplete: function () {
                    hookPendingClasses();
                    scanning = false;
                }
            });
        } catch (e) {
            send({ type: 'error', msg: 'enumerateLoadedClasses failed: ' + e });
            scanning = false;
        }
    });
}

function startTracing() {
    send({ type: 'log', msg: 'Starting class scan & hook injection...' });
    scanClasses();
    setInterval(scanClasses, SCAN_INTERVAL_MS);
}

setInterval(flushEdges, FLUSH_INTERVAL_MS);

send({ type: 'log', msg: 'Agent loaded, waiting for Java bridge...' });

(function tryStart(retries) {
    if (typeof Java !== 'undefined' && Java.perform) {
        Java.perform(function () {
            send({ type: 'log', msg: 'Java bridge ready, starting tracing...' });
            startTracing();
        });
    } else if (retries > 0) {
        setTimeout(function () { tryStart(retries - 1); }, 500);
    } else {
        send({ type: 'error', msg: 'Java bridge not available after retries' });
    }
})(40);

setTimeout(function () {
    Java.perform(function () {
        send({ type: 'log', msg: 'Late rescan for Hilt/Room classes...' });
        scanning = false;
        scanClasses();
    });
}, 6000);

setTimeout(function () {
    Java.perform(function () {
        scanning = false;
        scanClasses();
    });
}, 12000);

recv('recreate', function () {
    Java.perform(function () {
        send({ type: 'log', msg: 'Recreate triggered, re-scanning classes...' });
        scanning = false;
        scanClasses();
    });
});
