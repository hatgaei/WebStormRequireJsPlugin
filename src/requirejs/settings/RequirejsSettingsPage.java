package requirejs.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.wrapreturnvalue.WrapReturnValueAction;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.EditableRowTable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.EditableModel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import requirejs.RequirejsProjectComponent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RequirejsSettingsPage implements Configurable {
    protected Project project;
    
    private JCheckBox pluginEnabledCheckbox;
    private JTextField publicPathField;
    private JTextField configFilePathField;
    private JPanel panel;
    private JCheckBox overrideBaseUrlCheckbox;
    private JTextField baseUrlField;
    private JCheckBox enableRequireJsCheckBox;
    private JTextField requireJsPathField;
    private JCheckBox enableLoggingCheckbox;
    private JBList<String> exclusionList;

    public RequirejsSettingsPage(@NotNull final Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Require.js Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        loadSettings();
        CollectionListModel<String> model = initListModel();
        exclusionList.setModel(model);
        exclusionList.setEnabled(true);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(exclusionList, model);
        decorator.setAddAction((b) -> {
            model.add(JOptionPane.showInputDialog("Module name:"));
        });
        GridConstraints constraints = new GridConstraints();
        constraints.setFill(GridConstraints.FILL_BOTH);
        constraints.setHSizePolicy(GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_CAN_SHRINK);
        constraints.setVSizePolicy(GridConstraints.SIZEPOLICY_FIXED);
        constraints.setAnchor(GridConstraints.ANCHOR_EAST);
        constraints.setRow(7);
        constraints.setColumn(1);
        constraints.setColSpan(3);
        constraints.setRowSpan(1);
        panel.add(decorator.createPanel(), constraints);
        return panel;
    }

    @Override
    public boolean isModified() {
        return
                !pluginEnabledCheckbox.isSelected() == getSettings().pluginEnabled
                || !publicPathField.getText().equals(getSettings().publicPath)
                || !configFilePathField.getText().equals(getSettings().configFilePath)
                || !overrideBaseUrlCheckbox.isSelected() == getSettings().overrideBaseUrl
                || !baseUrlField.getText().equals(getSettings().baseUrl)
                || !enableRequireJsCheckBox.isSelected() == getSettings().requireJsEnabled
                || !requireJsPathField.getText().equals(getSettings().requireJsPath)
                || !enableLoggingCheckbox.isSelected() == getSettings().enableLogging
                || !listChanged();
    }

    private boolean listChanged() {
        List<String> values = getExclusionList();
        List<String> settingsValues = getSettings().exclusionList;
        return values.containsAll(settingsValues) && settingsValues.containsAll(values);
    }

    @NotNull
    private List<String> getExclusionList() {
        ListModel<String> model = exclusionList.getModel();
        List<String> values = new ArrayList<>(model.getSize());
        for (int i = 0; i < model.getSize(); ++i) {
            values.add(model.getElementAt(i));
        }
        return values;
    }

    private CollectionListModel<String> initListModel() {
        return new CollectionListModel<>(getSettings().exclusionList);
    }

    @Override
    public void apply() throws ConfigurationException {
        project.getComponent(RequirejsProjectComponent.class).clearParse();
        saveSettings();

        PsiManager.getInstance(project).dropResolveCaches();
    }

    protected void saveSettings() {
        getSettings().pluginEnabled = pluginEnabledCheckbox.isSelected();
        getSettings().publicPath = publicPathField.getText();
        getSettings().configFilePath = configFilePathField.getText();
        getSettings().overrideBaseUrl = overrideBaseUrlCheckbox.isSelected();
        if (getSettings().overrideBaseUrl) {
            getSettings().baseUrl = baseUrlField.getText();
        }
        getSettings().requireJsEnabled = enableRequireJsCheckBox.isSelected();
        getSettings().requireJsPath = requireJsPathField.getText();
        getSettings().enableLogging = enableLoggingCheckbox.isSelected();
        getSettings().exclusionList = getExclusionList();

        project.getComponent(RequirejsProjectComponent.class).validateSettings();
    }

    protected void loadSettings() {
        pluginEnabledCheckbox.setSelected(getSettings().pluginEnabled);
        publicPathField.setText(getSettings().publicPath);
        configFilePathField.setText(getSettings().configFilePath);
        overrideBaseUrlCheckbox.setSelected(getSettings().overrideBaseUrl);
        baseUrlField.setText(getSettings().baseUrl);
        requireJsPathField.setText(getSettings().requireJsPath);
        enableRequireJsCheckBox.setSelected(getSettings().requireJsEnabled);
        enableLoggingCheckbox.setSelected(getSettings().enableLogging);
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {

    }

    protected Settings getSettings() {
        return Settings.getInstance(project);
    }
}
