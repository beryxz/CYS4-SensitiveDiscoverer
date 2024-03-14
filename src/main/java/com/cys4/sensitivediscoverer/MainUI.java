/*
Copyright (C) 2023 CYS4 Srl
See the file 'LICENSE' for copying permission
*/
package com.cys4.sensitivediscoverer;

import burp.api.montoya.MontoyaApi;
import com.cys4.sensitivediscoverer.model.RegexEntity;
import com.cys4.sensitivediscoverer.model.ScannerOptions;
import com.cys4.sensitivediscoverer.tab.AboutTab;
import com.cys4.sensitivediscoverer.tab.ApplicationTab;
import com.cys4.sensitivediscoverer.tab.LoggerTab;
import com.cys4.sensitivediscoverer.tab.OptionsTab;

import javax.swing.*;
import java.util.List;
import java.util.Properties;

import static com.cys4.sensitivediscoverer.Utils.loadConfigFile;

public class MainUI {
    private final MontoyaApi burpApi;
    private final List<RegexEntity> generalRegexList;
    private final List<RegexEntity> extensionsRegexList;
    private final Properties configProperties;
    private final ScannerOptions scannerOptions;
    private JTabbedPane mainPanel;
    private boolean interfaceInitialized;

    public MainUI(MontoyaApi burpApi) throws Exception {
        this.interfaceInitialized = false;
        this.burpApi = burpApi;

        // parse configurations
        this.configProperties = loadConfigFile();
        scannerOptions = new ScannerOptions();
        scannerOptions.setConfigMaxResponseSize(Integer.parseInt(configProperties.getProperty("config.max_response_size")));
        scannerOptions.setConfigNumberOfThreads(Integer.parseInt(configProperties.getProperty("config.number_of_threads")));
        scannerOptions.setFilterInScopeCheckbox(Boolean.parseBoolean(configProperties.getProperty("config.filter.in_scope")));
        scannerOptions.setFilterSkipMaxSizeCheckbox(Boolean.parseBoolean(configProperties.getProperty("config.filter.skip_max_size")));
        scannerOptions.setFilterSkipMediaTypeCheckbox(Boolean.parseBoolean(configProperties.getProperty("config.filter.skip_media_type")));

        this.generalRegexList = RegexSeeder.getGeneralRegexes();
        this.extensionsRegexList = RegexSeeder.getExtensionRegexes();
    }

    public boolean isInterfaceInitialized() {
        return interfaceInitialized;
    }

    public ScannerOptions getScannerOptions() {
        return scannerOptions;
    }

    /**
     * Main function that initializes the extension and creates the UI, asynchronously
     */
    public void initializeUI() {
        SwingUtilities.invokeLater(this::_initializeUI);
    }

    private void _initializeUI() {
        mainPanel = new JTabbedPane();
        LoggerTab loggerTab = new LoggerTab(this);
        mainPanel.addTab(loggerTab.getTabName(), loggerTab.getPanel());
        ApplicationTab optionsTab = new OptionsTab(this);
        mainPanel.addTab(optionsTab.getTabName(), optionsTab.getPanel());
        ApplicationTab aboutTab = new AboutTab();
        mainPanel.addTab(aboutTab.getTabName(), aboutTab.getPanel());

        burpApi.userInterface().applyThemeToComponent(mainPanel);
        burpApi.userInterface().registerSuiteTab(this.getExtensionName(), this.getMainPanel());

        this.interfaceInitialized = true;
    }

    /**
     * Returns the extension's main panel
     */
    public JTabbedPane getMainPanel() {
        return mainPanel;
    }

    public MontoyaApi getBurpApi() {
        return burpApi;
    }

    public List<RegexEntity> getGeneralRegexList() {
        return generalRegexList;
    }

    public List<RegexEntity> getExtensionsRegexList() {
        return extensionsRegexList;
    }

    /**
     * getExtensionName return the name of the extension from the configuration file
     */
    public String getExtensionName() {
        return configProperties.getProperty("ui.extension_name");
    }

}