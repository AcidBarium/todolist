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
