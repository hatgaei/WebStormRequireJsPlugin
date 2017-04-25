package requirejs.settings;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@State(
        name = "RequirejsProjectComponent",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/requirejsPlugin.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class Settings implements PersistentStateComponent<Settings> {
    public static final String REQUIREJS_REQUIRE_FUNCTION_NAME = "require";
    public static final String REQUIREJS_DEFINE_FUNCTION_NAME = "define";
    public static final String DEFAULT_PUBLIC_PATH = "public";
    public static final String DEFAULT_CONFIG_FILE_PATH = "main.js";
    public static final String DEFAULT_BASEURL = ".";
    public static final String DEFAULT_REQUIRE_JS_PATH = "public/lib/require.js";
    public static final boolean DEFAULT_PLUGIN_ENABLED = false;
    public static final boolean DEFAULT_OVERRIDE_BASEURL = false;
    public static final boolean DEFAULT_REQUIRE_JS_ENABLED = false;
    public static final boolean DEFAULT_LOGGING_ENABLED = false;

    public String publicPath = DEFAULT_PUBLIC_PATH;
    public String configFilePath = DEFAULT_CONFIG_FILE_PATH;
    public String baseUrl = DEFAULT_BASEURL;
    public String requireJsPath = DEFAULT_REQUIRE_JS_PATH;
    public boolean overrideBaseUrl = DEFAULT_OVERRIDE_BASEURL;
    public boolean pluginEnabled = DEFAULT_PLUGIN_ENABLED;
    public boolean requireJsEnabled = DEFAULT_REQUIRE_JS_ENABLED;
    public boolean enableLogging = DEFAULT_LOGGING_ENABLED;
    public List<String> exclusionList = new ArrayList<>();
    private List<SettingsListener> listeners = new ArrayList<>();

    protected Project project;

    public static Settings getInstance(Project project) {
        Settings settings = ServiceManager.getService(project, Settings.class);
        settings.project = project;
        return settings;
    }

    @Nullable
    @Override
    public Settings getState() {
        listeners.forEach(l -> l.settingsChanged(this));
        return this;
    }

    @Override
    public void loadState(Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getVersion() {
        return "" + Objects.hash(publicPath, configFilePath, baseUrl, requireJsPath, overrideBaseUrl, pluginEnabled,
                requireJsEnabled, enableLogging);
    }

    public void registerListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    public interface SettingsListener {
        void settingsChanged(Settings s);
    }
}
