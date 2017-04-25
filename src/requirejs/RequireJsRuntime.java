package requirejs;

import jdk.nashorn.internal.runtime.ECMAException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
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
            this.component = component;
            this.globalScope = initScope(ctx);
            initRequireJs(ctx, requirePath, requireConfig);
            return null;
        });
    }

    private Scriptable initScope(Context ctx) {
        Scriptable scope = ctx.initStandardObjects();
        // Init the window and console objects to kindly avoid reference errors.
        ctx.evaluateString(scope, "var window = null;", "global", 1, null);
        ctx.evaluateString(scope, "var console = null;", "global", 1, null);
        return scope;
    }

    protected void initRequireJs(Context ctx, String requirePath, String requireConfig) {
        evaluateFile(ctx, requirePath);
        evaluateFile(ctx, requireConfig);
    }

    private void evaluateFile(Context ctx, String sourceFile) {
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(sourceFile));
            ctx.evaluateReader(globalScope, in, sourceFile, 1, null);
        } catch (IOException e) {
            component.showErrorConfigNotification("IOException occurred while evaluating file '" + sourceFile
                    + "'! Exception: \n\t" + e.getMessage());
        } catch (ECMAException | EcmaError e) {
            component.showErrorConfigNotification("Got js error evaluating file '" + sourceFile
                    + "'! Exception: \n\t" + e.getMessage());
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
