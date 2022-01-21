package com.moyskleytech.mc.banking.utils;

import com.moyskleytech.mc.banking.Banking;
import lombok.NonNull;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.bukkit.entity.Player;
import org.screamingsandals.lib.utils.reflect.Reflect;

import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BankingUtil {

    public static int getAmountOfSpaceFor(Material m,Player player) {
        var oneStack = new ItemStack(m).getMaxStackSize();
        int space = 0;
        var inventoryContent = player.getInventory().getContents();
        for (int index = 0; index < 36; index++) {
            var itemStack = inventoryContent[index];
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                space += oneStack;
            }
        }

        var inventoryStock = player.getInventory().all(m);
        for (var stack : inventoryStock.entrySet()) {
            space += oneStack - stack.getValue().getAmount();
        }
        return space;
    }

    public static void addToInventory(Material m, int withdrawable,Player player) {
        var oneStack = new ItemStack(m).getMaxStackSize();
        var inventoryStock = player.getInventory().all(Material.AIR);

        inventoryStock = player.getInventory().all(m);
        for (var stack : inventoryStock.entrySet()) {
            var space = oneStack - stack.getValue().getAmount();
            var amountToAdd = Math.min(space, withdrawable);
            stack.getValue().setAmount(stack.getValue().getAmount() + amountToAdd);
            withdrawable -= amountToAdd;
        }

        while(withdrawable>0)
        {
            var emptySlot = player.getInventory().firstEmpty();
            if (emptySlot >= 0) {
                var amount = Math.min(oneStack, withdrawable);
                var stack_ = new ItemStack(m);
                stack_.setAmount(amount);
                player.getInventory().setItem(emptySlot, stack_);
                withdrawable -= amount;
            }
        }
    }

    
    public static List<Material> parseMaterialFromConfig(List<String> materialNames) {
        final var materialList = new ArrayList<Material>();
        materialNames.stream()
                .filter(mat -> mat != null && !mat.isEmpty())
                .forEach(material -> {
                    try {
                        final var mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
                        materialList.add(mat);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        return materialList;
    }

    public static void cancelTask(BukkitTask task) {
        if (task != null) {
            if (Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId())
                    || Bukkit.getScheduler().isQueued(task.getTaskId())) {
                task.cancel();
                Logger.trace("cancelTask {}", task);
            }
        }
    }

    public static List<String> translateColors(List<String> toTranslate) {
        return toTranslate.stream().map(string -> ChatColor
                .translateAlternateColorCodes('&', string)).collect(Collectors.toList());
    }

    public static String translateColors(String toTranslate) {
        return ChatColor.translateAlternateColorCodes('&', toTranslate);
    }

    public static Optional<Player> getPlayer(UUID uuid) {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }

    public static void disablePlugin(@NotNull JavaPlugin plugin) {
        // thank you Misat for this :)
        try {
            String message = String.format("Disabling %s", plugin.getDescription().getFullName());
            plugin.getLogger().info(message);
            Bukkit.getPluginManager().callEvent(new PluginDisableEvent(plugin));
            Reflect.setField(plugin, "isEnabled", false);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling "
                    + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getScheduler().cancelTasks(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for "
                    + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            // Bukkit.getServicesManager().unregisterAll(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Error occurred (in the plugin loader) while unregistering services for "
                            + plugin.getDescription().getFullName() + " (Is it up to date?)",
                    ex);
        }

        try {
            var handlers = HandlerList.getRegisteredListeners(plugin);
            Logger.trace("-----------------------{}-----------------", handlers.size());
            for (var handler : handlers) {
                if (handler.getListener().toString().contains("com.moyskleytech.mc.banking") ||
                        handler.getListener().toString().contains("io.github.pronze.lib.screaming") ||
                        handler.getListener().toString().contains("io.github.pronze.lib.simpleinventories") ||
                        handler.getListener().toString().contains("org.screamingsandals.bedwars.lib.sgui") ||
                        handler.getListener().toString().contains("io.github.pronze.lib.bedwars")) {
                    HandlerList.unregisterAll(handler.getListener());
                }
                Logger.trace("handler {}", handler.getListener().toString());
            }
            Logger.trace("-----------------------{}-----------------", handlers.size());

            // HandlerList.unregisterAll(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering events for "
                    + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }

        try {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin);
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);
        } catch (Throwable ex) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Error occurred (in the plugin loader) while unregistering plugin channels for "
                            + plugin.getDescription().getFullName() + " (Is it up to date?)",
                    ex);
        }

        try {
            for (World world : Bukkit.getWorlds()) {
                world.removePluginChunkTickets(plugin);
            }
        } catch (Throwable ex) {
            // older versions don't even have chunk tickets
            // Bukkit.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader)
            // while removing chunk tickets for " + plugin.getDescription().getFullName() +
            // " (Is it up to date?)", ex);
        }
    }

    public static void reloadPlugin(@NonNull JavaPlugin plugin) {
        disablePlugin(plugin);
        // PlayerWrapperService.getInstance().reload();
        Bukkit.getServer().getPluginManager().enablePlugin(plugin);
        if (plugin == Banking.getPluginInstance()) {
            //SBAConfig.getInstance().forceReload();
            //LanguageService.getInstance().load(plugin);
        }
        Bukkit.getLogger().info("Plugin reloaded! Keep in mind that restarting the server is safer!");
    }

    public static String capitalizeFirstLetter(@NotNull String toCap) {
        return toCap.substring(0, 1).toUpperCase() + toCap.substring(1).toLowerCase();
    }

    private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections)
            return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();

        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    public static int getAmountOfInInventory(Material material, Player player) {
        var inventoryStock = player.getInventory().all(material);
        int inInventory = 0;
        for (var stack : inventoryStock.entrySet()) {
            inInventory += stack.getValue().getAmount();
        }
        return inInventory;
    }

    public static void removeFromInventory(Material m, int amount, @NotNull Player player) {
        var inventoryStock = player.getInventory().all(m);
        for (var stack : inventoryStock.entrySet()) {
            var count = stack.getValue().getAmount();
            if (count > amount) {
                stack.getValue().setAmount(count - amount);
                break;
            } else {
                amount -= count;
                player.getInventory().setItem(stack.getKey(), new ItemStack(Material.AIR));
            }
        }
    }
}
