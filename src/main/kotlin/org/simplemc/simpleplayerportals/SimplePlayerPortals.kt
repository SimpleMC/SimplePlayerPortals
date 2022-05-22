package org.simplemc.simpleplayerportals

import org.bukkit.plugin.java.JavaPlugin

/**
 * KotlinPluginTemplate plugin
 */
class SimplePlayerPortals : JavaPlugin() {

    override fun onEnable() {
        // ensure config file exists
        saveDefaultConfig()

        logger.info("${description.name} version ${description.version} enabled!")
    }

    override fun onDisable() {
        logger.info("${description.name} disabled.")
    }
}
