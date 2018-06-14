package com.smartbear.readyapi.plugin.hg;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

@PluginConfiguration(groupId = "com.smartbear.readyapi.plugins", name = "Hg (Mercurial) Integration Plugin",
        version = "0.7-SNAPSHOT", autoDetect = true,
        description = "A hg (mercurial) plugin to share composite projects", infoUrl = "https://github.com/Bortulev/ready-api-hg-plugin",
        minimumReadyApiVersion = "2.4.0")
public class HgPluginConfig extends PluginAdapter {
}
