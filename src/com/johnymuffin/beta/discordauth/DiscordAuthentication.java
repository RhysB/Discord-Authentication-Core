package com.johnymuffin.beta.discordauth;

import com.johnymuffin.beta.discordauth.commands.DiscordAuthCommand;
import com.johnymuffin.discordcore.DiscordCore;
import com.projectposeidon.api.PoseidonUUID;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordAuthentication extends JavaPlugin {
    private Logger log;
    private PluginDescriptionFile pdf;
    private DiscordAuthentication plugin;
    public static DiscordCore discord;
    private DiscordAuthCache cache;
    private String pluginName;
    public DiscordAuthDatafile data;
    //Poseidon
    private boolean poseidonPresent = false;

    private DiscordAuthConfig config;

    @Override
    public void onEnable() {
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        plugin = this;
        pluginName = pdf.getName();
        log.info("[" + pdf.getName() + "] Is loading, Version: " + pdf.getVersion() + " | Bukkit: " + Bukkit.getServer().getVersion());
        //Enabling
        PluginManager pm = Bukkit.getServer().getPluginManager();
        if (pm.getPlugin("DiscordCore") == null) {
            log.info("}---------------ERROR---------------{");
            log.info("Discord Authentication Requires Discord Core");
            log.info("Download it at: https://github.com/RhysB/Discord-Bot-Core");
            log.info("}---------------ERROR---------------{");
            log.info("Discord Authentication Is Shutting Down Forcefully");
            pm.disablePlugin(this);
            return;
        }
        if (testClassExistence("com.projectposeidon.api.PoseidonUUID")) {
            poseidonPresent = true;
            logInfo("Project Poseidon detected, using valid UUIDs.");
        } else {
            logInfo("Project Poseidon support disabled.");
        }


        discord = (DiscordCore) getServer().getPluginManager().getPlugin("DiscordCore");
        data = new DiscordAuthDatafile(plugin);
        cache = new DiscordAuthCache(plugin);

        config = new DiscordAuthConfig(plugin);


        final DiscordAuthListener DAL = new DiscordAuthListener(plugin);
        discord.getDiscordBot().jda.addEventListener(DAL);

        final DiscordAuthenticationCommands DAC = new DiscordAuthenticationCommands(plugin);


        //Update Last Known Username Logic
        final DiscordAuthUUIDJoinListener DAUJL = new DiscordAuthUUIDJoinListener(plugin);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, DAUJL, Event.Priority.Monitor, plugin);


    }

    @Override
    public void onDisable() {
        data.saveConfig();
        log.info("[" + pdf.getName() + "] Has Been Disabled");
    }

    public void logger(Level level, String message) {
        log.log(level, "[" + pdf.getName() + "] " + message);
    }

    public DiscordCore getDiscord() {
        return discord;
    }

    public DiscordAuthCache getCache() {
        return cache;
    }

    public DiscordAuthDatafile getData() {
        return data;
    }

    private boolean testClassExistence(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void logInfo(String s) {
        log.info("[" + pluginName + "] " + s);
    }

    public boolean isPoseidonPresent() {
        return poseidonPresent;
    }


    public UUID getPlayerUUID(String playerName) {
        if (poseidonPresent) {
            return PoseidonUUID.getPlayerGracefulUUID(playerName);
        }
        return UUID.fromString(playerName);
    }

    public DiscordAuthConfig getConfig() {
        return config;
    }
}

class DiscordAuthenticationCommands {
    DiscordAuthentication plugin;

    public DiscordAuthenticationCommands(DiscordAuthentication plugin) {
        this.plugin = plugin;
        plugin.getCommand("discordauth").setExecutor(new DiscordAuthCommand(plugin));
    }

}
