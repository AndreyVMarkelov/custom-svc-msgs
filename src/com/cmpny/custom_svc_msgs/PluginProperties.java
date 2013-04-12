package com.cmpny.custom_svc_msgs;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;

/* Get aParam from teamcity-plugin.xml parameters */
public class PluginProperties {
    private final Logger LOG = Logger.getLogger(Loggers.SERVER_CATEGORY);
    private static String aParam = "";

    public PluginProperties(PluginDescriptor pd) {
        // Other stuff you can get from PluginDescriptor...
        LOG.info("Initialized CustomServiceMessages PluginDescriptor with Spring, Woohoo!");
//        LOG.info("Plugin Resources Path = " + pd.getPluginResourcesPath());
//        LOG.info("Relative Plugin Resources Path = " + pd.getPluginResourcesPath("."));
//        LOG.info("Plugin Root = " + pd.getPluginRoot().getAbsolutePath());
        aParam = pd.getParameterValue("aParam");
    }

    public PluginProperties() {
        LOG.info("Called CustomServiceMessages PluginProperties(), interesting");
    }

    public static String aParam() { return aParam; }
}
