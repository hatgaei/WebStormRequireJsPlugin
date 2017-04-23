package requirejs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class RequireJsRuntime {

    protected RequirejsProjectComponent component;
    protected Context ctx;
    protected Scriptable scope;

    public RequireJsRuntime(RequirejsProjectComponent component, String requirePath, String requireConfig) {
        Context ctx = Context.enter();
        Scriptable scope = ctx.initStandardObjects();
        this.component = component;
        this.ctx = ctx;
        this.scope = scope;
        initRequireJs(requirePath, requireConfig);
    }

    protected void initRequireJs(String requirePath, String requireConfig) {
        try {
            // Loads both require and requirejs as scope variables.
            InputStreamReader in = new InputStreamReader(new FileInputStream(requirePath));
            ctx.evaluateReader(scope, in, "requirejs", 1, null);
        } catch (IOException e) {
            component.showErrorConfigNotification("Could not load requirejs: " + requirePath
                    + "! Exception: " + e.getMessage());
        }
        try {
            // Actually configure require for real.
            InputStreamReader in = new InputStreamReader(new FileInputStream(requireConfig));
            ctx.evaluateReader(scope, in, "requireConfig", 1, null);
        } catch (IOException e) {
            component.showErrorConfigNotification("Could not load require config: " + requireConfig
                    + "! Exception: " + e.getMessage());
        }
    }

    public String resolvePath(String depName) {
        Scriptable require = (Scriptable) scope.get("require", scope);
        Object toUrl = require.get("toUrl", require);
        if (!(toUrl instanceof Function)) {
            component.showErrorConfigNotification("Failed to get require.toUrl() method (check your version of require?).");
            return null;
        }
        // Haxor time: require.toUrl() assumes file extensions, so just chuck a '.' at the end and remove it later.
        Object args[] = { depName + "." };
        Function f = (Function)toUrl;
        Object result = f.call(ctx, scope, require, args);
        String path = Context.toString(result);
        path = path.replaceAll("\\.$", "");
        return path;
    }
}
