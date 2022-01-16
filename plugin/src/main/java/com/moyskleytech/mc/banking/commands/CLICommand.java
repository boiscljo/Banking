package com.moyskleytech.mc.banking.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;

import com.moyskleytech.mc.banking.utils.Logger;
import io.leangen.geantyref.TypeToken;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import com.moyskleytech.mc.banking.config.BankingConfig;
import com.moyskleytech.mc.banking.ui.UI;
import com.moyskleytech.mc.banking.utils.BankingUtil;
import com.moyskleytech.mc.banking.utils.Logger.Level;

import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CLICommand {
    static boolean init = false;

    public void playNotificationSoundToPlayer(Player p) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
            }
        };

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Banking.getPluginInstance(), r, 10);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Banking.getPluginInstance(), r, 20);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Banking.getPluginInstance(), r, 30);
    }

    public void playErrorSound(Player p) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
            }
        };
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Banking.getPluginInstance(), r, 0);
    }

    @OnPostEnable
    public void onPostEnabled() {
        if (init)
            return;
        CommandManager.getInstance().getManager().getParserRegistry().registerSuggestionProvider("ores",
                (commandSenderCommandContext, s) -> BankingConfig.getInstance().getStringList("ores"));
        com.moyskleytech.mc.banking.commands.CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("banking|bank groups list")
    @CommandDescription("Show group list")
    @CommandPermission("banking.groups.admin")
    private void commandGroupList(
            final @NotNull Player player) {
        var groups = Banking.getInstance().getStorage().getGroupWorldMapping();
        player.sendMessage("------<Groups>-----");
        for (var iterable_element : groups.entrySet()) {
            player.sendMessage(iterable_element.getKey() + ":");
            for (var world : iterable_element.getValue()) {
                player.sendMessage("  " + world);
            }
        }
        player.sendMessage("-----</Groups>-----");
    }

    @CommandMethod("banking|bank groups set <group>")
    @CommandDescription("Set the group for the current world")
    @CommandPermission("banking.groups.admin")
    private void commandGroupSet(
            final @NotNull Player player,
            final @Argument("group") String group) {
        Banking.getInstance().getStorage().setGroupForWorld(player.getWorld(), group);
    }

    @CommandMethod("banking|bank master <target> <amount> <ore> [group]")
    @CommandDescription("Set the group for the current world")
    @CommandPermission("banking.groups.admin")
    private void commandBankMaster(
            final @NotNull Player player,
            final @Argument("target") OfflinePlayer target,
            final @Argument("amount") int amount,
            final @Argument(value = "ore", suggestions = "ores") String ore,
            @Argument(value = "group") String group) {

        var storage = Banking.getInstance().getStorage();
        if (group == null)
            group = storage.getGroupForCurrentLocation(player);
        storage.addToPlayerBank(Material.matchMaterial(ore), target, amount, group);

        var newBalance = storage.getBalanceFor(Material.matchMaterial(ore), target, group);

        if (target instanceof Player) {
            Player onlineTarget = (Player) target;
            playNotificationSoundToPlayer(onlineTarget);

            onlineTarget
                    .sendMessage("A bank master adjusted your "
                            + new ItemStack(Material.matchMaterial(ore)).getI18NDisplayName() + " balance");
        }
        player.sendMessage(
                "You adjusted " + target.getName() + "'s "
                        + new ItemStack(Material.matchMaterial(ore)).getI18NDisplayName()
                        + " balance, their new balance is " + newBalance.getInBank());
    }

    @CommandMethod("banking|bank check <target> <ore> [group]")
    @CommandDescription("Set the group for the current world")
    @CommandPermission("banking.groups.admin")
    private void commandBankCheck(
            final @NotNull Player player,
            final @Argument("target") OfflinePlayer target,
            final @Argument(value = "ore", suggestions = "ores") String ore,

            @Argument("group") String group) {

        var storage = Banking.getInstance().getStorage();
        if (group == null)
            group = storage.getGroupForCurrentLocation(player);

        var newBalance = storage.getBalanceFor(Material.matchMaterial(ore), target, group);

        player.sendMessage(
                "" + target.getName() + "'s " + new ItemStack(Material.matchMaterial(ore)).getI18NDisplayName()
                        + " balance is " + newBalance.getInBank());
    }

    @CommandMethod("banking|bank transfer <target> <amount> <ore> [group]")
    @CommandDescription("Set the group for the current world")
    @CommandPermission("banking.groups.admin")
    private void commandBankTransfer(
            final @NotNull Player player,
            final @Argument("target") OfflinePlayer target,
            final @Argument("amount") int amount,
            final @Argument(value = "ore", suggestions = "ores") String ore_,
            @Argument("group") String group) {
      
        Material ore = Material.matchMaterial(ore_);
        ItemStack stack = new ItemStack(ore);
        if (amount <= 0) {
            player.sendMessage(
                    BankingConfig.getInstance().logo() + "§cCannot transfer 0 or less " + stack.getI18NDisplayName());
            playErrorSound(player);
            return;
        }
        var storage = Banking.getInstance().getStorage();
        var balance = storage.getBalanceFor(ore, player, group);
        if (group == null)
            group = storage.getGroupForCurrentLocation(player);
        
        if (balance.getInBank() < amount) {
            player.sendMessage(
                    BankingConfig.getInstance().logo() + "§cYou don't have " + amount + " " + stack.getI18NDisplayName()
                            + ", cannot transfer to " + target.getName());
            player.sendMessage("Your current balance is " + balance.getInBank());
            playErrorSound(player);
            return;
        }

        player.sendMessage(BankingConfig.getInstance().logo() + "Transfering " + amount + " "
                + stack.getI18NDisplayName() + " to " + target.getName());

        if (storage.removeFromPlayerBank(ore, player, amount, group)) {
            if (storage.addToPlayerBank(ore, target, amount, group))
            {
                if(target instanceof Player)
                {
                    //"%{@logo}%&a%player% sent you %arg-1% %arg-2%, your new balance is %arg-3's arg-2 bank balance in player's world%"
                    Player onlineTarget = (Player) target;
                    onlineTarget.sendMessage(BankingConfig.getInstance().logo()+"§a"+player.getName()+" sent you "+amount+" "+ stack.getI18NDisplayName()+
                            ", your new balance is " + storage.getBalanceFor(ore, target, group).getInBank());
                    playNotificationSoundToPlayer(onlineTarget);
                }
            }
            else
            {
                storage.addToPlayerBank(ore, player, amount, group);
            }
        }
    }

}
