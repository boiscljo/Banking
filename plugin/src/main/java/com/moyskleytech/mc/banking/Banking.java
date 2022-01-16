package com.moyskleytech.mc.banking;

import com.moyskleytech.mc.banking.commands.CommandManager;
import com.moyskleytech.mc.banking.config.BankingConfig;
import com.moyskleytech.mc.banking.listeners.InventoryListener;
import com.moyskleytech.mc.banking.placeholderapi.BankingExpansion;
import com.moyskleytech.mc.banking.storage.Storage;
import com.moyskleytech.mc.banking.utils.Logger;
import com.moyskleytech.mc.banking.utils.Logger.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.moyskleytech.mc.banking.VersionInfo;
import org.screamingsandals.lib.event.EventManager;
import org.screamingsandals.lib.healthindicator.HealthIndicatorManager;
import org.screamingsandals.lib.hologram.HologramManager;
import org.screamingsandals.lib.npc.NPCManager;
import org.screamingsandals.lib.packet.PacketMapper;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.plugin.PluginContainer;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.utils.PlatformType;
import org.screamingsandals.lib.utils.annotations.Init;
import org.screamingsandals.lib.utils.annotations.Plugin;
import org.screamingsandals.lib.utils.annotations.PluginDependencies;
import org.screamingsandals.simpleinventories.SimpleInventoriesCore;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin(id = "Banking", authors = { "boiscljo" }, loadTime = Plugin.LoadTime.POSTWORLD, version = VersionInfo.VERSION)
@PluginDependencies(platform = PlatformType.BUKKIT, dependencies = {}, softDependencies = { "PlaceholderAPI" ,"ViaVersion"})
@Init(services = {
        Logger.class,
        PacketMapper.class,
        HologramManager.class,
        HealthIndicatorManager.class,
        SimpleInventoriesCore.class,
        NPCManager.class,
        CommandManager.class,
        InventoryListener.class,
        BankingConfig.class
})
public class Banking extends PluginContainer {

    private static Banking instance;
    Storage storage;
    public static Banking getInstance() {
        return instance;
    }

    private JavaPlugin cachedPluginInstance;
    private final List<Listener> registeredListeners = new ArrayList<>();

    public static JavaPlugin getPluginInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("SBA has not yet been initialized!");
        }
        if (instance.cachedPluginInstance == null) {
            instance.cachedPluginInstance = (JavaPlugin) instance.getPluginDescription().as(JavaPlugin.class);
        }
        return instance.cachedPluginInstance;
    }

    @Override
    public void enable() {
        instance = this;
        cachedPluginInstance = instance.getPluginDescription().as(JavaPlugin.class);
        Logger.init(cachedPluginInstance);
    }

    @Override
    public void postEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Logger.trace("Registering SBAExpansion...");
            new BankingExpansion().register();
        }
        storage = new Storage();
        
        Logger.info("Plugin has finished loading!");
        Logger.info("banking Initialized on JAVA {}", System.getProperty("java.version"));
        Logger.trace("API has been registered!");

        Logger.setMode(Level.ERROR);
    }

    public void registerListener(@NotNull Listener listener) {
        if (registeredListeners.contains(listener)) {
            return;
        }
        Bukkit.getServer().getPluginManager().registerEvents(listener, getPluginInstance());
        Logger.trace("Registered listener: {}", listener.getClass().getSimpleName());
    }

    public void unregisterListener(@NotNull Listener listener) {
        if (!registeredListeners.contains(listener)) {
            return;
        }
        HandlerList.unregisterAll(listener);
        registeredListeners.remove(listener);
        Logger.trace("Unregistered listener: {}", listener.getClass().getSimpleName());
    }

    public List<Listener> getRegisteredListeners() {
        return List.copyOf(registeredListeners);
    }

    @Override
    public void disable() {
        EventManager.getDefaultEventManager().unregisterAll();
        EventManager.getDefaultEventManager().destroy();
        Bukkit.getServer().getServicesManager().unregisterAll(getPluginInstance());
    }

    public boolean isSnapshot() {
        return getVersion().contains("SNAPSHOT") || getVersion().contains("dev");
    }

    public String getVersion() {
        return getPluginDescription().getVersion();
    }

    public JavaPlugin getJavaPlugin() {
        return instance.getPluginDescription().as(JavaPlugin.class);
    }

    public Storage getStorage() {
        return storage;
    }
}
