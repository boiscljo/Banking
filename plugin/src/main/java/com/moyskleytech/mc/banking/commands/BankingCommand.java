package com.moyskleytech.mc.banking.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.moyskleytech.mc.banking.utils.Logger;
import io.leangen.geantyref.TypeToken;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.npc.NPC;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.utils.Pair;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.world.LocationMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import com.moyskleytech.mc.banking.Banking;
import com.moyskleytech.mc.banking.utils.BankingUtil;
import com.moyskleytech.mc.banking.utils.Logger.Level;

import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BankingCommand {
    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if (init)
            return;
        com.moyskleytech.mc.banking.commands.CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("banking|bank reload")
    @CommandDescription("reload command")
    @CommandPermission("banking.reload")
    private void commandReload(
            final @NotNull CommandSender sender) {
        BankingUtil.reloadPlugin(Banking.getPluginInstance());
    }

    @CommandMethod("banking|bank dump")
    @CommandDescription("dump command")
    @CommandPermission("banking.dump")
    private void commandDump(
            final @NotNull CommandSender sender) {
        sender.sendMessage("Java version : " + System.getProperty("java.version"));
        String a = Bukkit.getServer().getClass().getPackage().getName();
        String version = a.substring(a.lastIndexOf('.') + 1);
        sender.sendMessage("Server version : " + version);
        sender.sendMessage("Server : " + Bukkit.getServer().getVersion());

        for (var plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            sender.sendMessage(plugin.getName() + " " + plugin.getDescription().getVersion());
        }
    }

    @CommandMethod("banking|bank debug <enabled>")
    @CommandDescription("debug command")
    @CommandPermission("banking.debug")
    private void commandDebug(
            final @NotNull CommandSender sender,
            final @NotNull @Argument(value = "enabled") boolean enabled) {
        if (enabled)
            Logger.setMode(Level.ALL);
        else
            Logger.setMode(Level.ERROR);

    }

    @CommandMethod("banking|bank resetconfig")
    @CommandDescription("reset banking configuration")
    @CommandPermission("banking.reset")
    private void commandReset(
            final @NotNull CommandSender sender) {
        final var component = Component.text("");

        PlayerMapper.wrapSender(sender).sendMessage(component);
        final var c2 = Component.text(""); 

        PlayerMapper.wrapSender(sender).sendMessage(c2);
    }
}
