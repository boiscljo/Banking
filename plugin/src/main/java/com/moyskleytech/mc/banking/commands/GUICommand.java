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
import org.bukkit.Material;
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
public class GUICommand {
    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if (init)
            return;
        com.moyskleytech.mc.banking.commands.CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("banking|bank")
    @CommandDescription("Show a GUI")
    @CommandPermission("banking.gui")
    private void commandGui(
            final @NotNull Player player) {
        player.openInventory(new UI(player).getInventory());
    }
    @CommandMethod("banking|bank gui")
    @CommandDescription("Show a GUI")
    @CommandPermission("banking.gui")
    private void commandGuiLong(
            final @NotNull Player player) {
        player.openInventory(new UI(player).getInventory());
    }
}
