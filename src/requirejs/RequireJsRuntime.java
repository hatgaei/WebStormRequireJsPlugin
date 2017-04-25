package requirejs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RequireJsRuntime {

    protected RequirejsProjectComponent component;
    protected Scriptable globalScope;
    private Map<String, String> cache = new HashMap<>();

    public RequireJsRuntime(RequirejsProjectComponent component, String requirePath, String requireConfig) {
        withContext((ctx) -> {
            Scriptable scope = ctx.initStandardObjects();
            this.component = component;
            this.globalScope = scope;
            initRequireJs(ctx, requirePath, requireConfig);
            return null;
        });
    }

    protected void initRequireJs(Context ctx, String requirePath, String requireConfig) {
        try {
            // Loads both require and requirejs as scope variables.
            InputStreamReader in = new InputStreamReader(new FileInputStream(requirePath));
            ctx.evaluateReader(globalScope, in, "requirejs", 1, null);
        } catch (IOException e) {
            component.showErrorConfigNotification("Could not load requirejs: " + requirePath
                    + "! Exception: " + e.getMessage());
        }
        try {
            // Actually configure require for real.
            InputStreamReader in = new InputStreamReader(new FileInputStream(requireConfig));
            ctx.evaluateReader(globalScope, in, "requireConfig", 1, null);
        } catch (IOException e) {
            component.showErrorConfigNotification("Could not load require config: " + requireConfig
                    + "! Exception: " + e.getMessage());
        }
    }

    public String resolvePath(String depName) {
        if (cache.containsKey(depName)) {
            return cache.get(depName);
        }
        String path = resolveWithRequire(depName);
        cache.put(depName, path);
        return path;
    }

    private String resolveWithRequire(String depName) {
        return withContext( (ctx) -> {
            Scriptable require = (Scriptable) globalScope.get("require", globalScope);
            Object toUrl = require.get("toUrl", require);
            if (!(toUrl instanceof Function)) {
                component.showErrorConfigNotification("Failed to get require.toUrl() method (check your version of require?).");
                return null;
            }
            component.showDebugNotification("Attempting to load module '" + depName + "' from require config.");
            // Haxor time: require.toUrl() assumes file extensions, so just chuck a '.' at the end and remove it later.
            Object args[] = { depName + "." };
            Function f = (Function)toUrl;
            Object result = f.call(ctx, globalScope, require, args);
            String path = Context.toString(result);
            path = path.replaceAll("\\.$", "");
            component.showDebugNotification("Looking for module '" + depName + "' at path '" + component.getConfigFileDir().toString() + "/" + path + "'");
            return path;
        });
    }

    private <T> T withContext(java.util.function.Function<Context, T> fn) {
        Context ctx = Context.enter();
        try {
            return fn.apply(ctx);
        } finally {
            Context.exit();
        }
    }
}
