function lazyLoadBridge(e) {
    Object.defineProperty(globalThis, e, {
        enumerable: true,
        configurable: true,
        get: function() {
            var result;
            send({type: "frida:load-bridge", name: e});
            recv("frida:bridge-loaded", function(o) {
                result = Script.evaluate(
                    "/frida/bridges/" + o.filename,
                    "(function () { " + [
                        o.source,
                        "Object.defineProperty(globalThis, '" + e + "', { value: bridge });",
                        "return bridge;"
                    ].join("\n") + " })();"
                );
            }).wait();
            return result;
        }
    });
}

lazyLoadBridge("Java");

var TARGET_PACKAGE = 'com.ayse.todocompose';
var EXCLUDE_REGEX = [/.*\$Companion.*/,/.*\$DefaultImpls.*/,/.*\$inlined.*/,/.*WhenMappings.*/,/.*\$ExternalSynthetic.*/];
var BUFFER_SIZE = 200;
var FLUSH_INTERVAL_MS = 250;
var SCAN_INTERVAL_MS = 3000;

var callStacks = new Map();
var edgeCounts = new Map();
var hookedClasses = new Set();
var hookedMethodCount = 0;

function getThreadStack() {
    var tid = Process.getCurrentThreadId();
    if (!callStacks.has(tid)) callStacks.set(tid, []);
    return callStacks.get(tid);
}

function flushEdges() {
    if (edgeCounts.size === 0) return;
    var data = [];
    edgeCounts.forEach(function (count, key) {
        var parts = key.split('|||');
        data.push({ caller: parts[0], callee: parts[1], count: count });
    });
    send({ type: 'edges', data: data });
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
        paramTypes = overload.parameterTypes.map(function (t) { return String(t.className || t); });
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
    var unsafe = ['toString', 'hashCode', 'equals', 'getClass', 'finalize', 'clone'];
    return unsafe.indexOf(methodName) === -1;
}

function hookClass(className) {
    try {
        var clazz = Java.use(className);
        var methods = clazz.class.getDeclaredMethods();
        for (var mi = 0; mi < methods.length; mi++) {
            var name = methods[mi].getName();
            if (name === '<clinit>' || !isSafeToHook(name)) continue;
            try {
                var prop = clazz[name];
                if (!prop || typeof prop !== 'function' || !prop.overloads) continue;
                prop.overloads.forEach(function (overload) {
                    var sig = getSignature(className, name, overload);
                    try {
                        overload.implementation = function () {
                            var stack = getThreadStack();
                            var caller = stack.length > 0 ? stack[stack.length - 1] : '_ROOT_';
                            stack.push(sig);
                            recordEdge(caller, sig);
                            try {
                                return overload.apply(this, arguments);
                            } finally {
                                stack.pop();
                            }
                        };
                        hookedMethodCount++;
                    } catch (e) {}
                });
            } catch (e) {}
        }
    } catch (e) {}
}

function reportHooked() {
    send({type: 'hookedCount', count: hookedMethodCount});
}

setInterval(flushEdges, FLUSH_INTERVAL_MS);

var pendingClasses = [];

function hookPendingClasses() {
    if (pendingClasses.length === 0) return;
    var batch = pendingClasses.splice(0);
    Java.perform(function () {
        for (var i = 0; i < batch.length; i++) {
            if (!hookedClasses.has(batch[i])) {
                hookClass(batch[i]);
                hookedClasses.add(batch[i]);
            }
        }
        send({ type: 'scanDone', hookedCount: hookedClasses.size, hookedMethods: hookedMethodCount });
        reportHooked();
    });
}

function scanClasses() {
    Java.perform(function () {
        Java.enumerateLoadedClasses({
            onMatch: function (className) {
                if (className.indexOf(TARGET_PACKAGE) !== 0) return;
                if (hookedClasses.has(className)) return;
                if (shouldExclude(className)) return;
                pendingClasses.push(className);
            },
            onComplete: function () {
                hookPendingClasses();
            }
        });
    });
}

function startTracing() {
    scanClasses();
    setInterval(scanClasses, SCAN_INTERVAL_MS);
}

send({type: "log", msg: "Agent loaded"});

function tryStartTracing(retries) {
    if (typeof Java === 'undefined') {
        if (retries > 0) {
            setTimeout(function() { tryStartTracing(retries - 1); }, 500);
        } else {
            send({type: "error", msg: "Java never became available"});
        }
        return;
    }
    Java.perform(function() {
        send({type: "log", msg: "Java available, starting tracing..."});
        startTracing();
    });
}

tryStartTracing(20);
