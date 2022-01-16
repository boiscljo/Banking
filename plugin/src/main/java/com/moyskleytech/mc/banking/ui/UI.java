package com.moyskleytech.mc.banking.ui;

import java.util.List;

import com.moyskleytech.mc.banking.Banking;
import com.moyskleytech.mc.banking.config.BankingConfig;
import com.moyskleytech.mc.banking.storage.BankOre;
import com.moyskleytech.mc.banking.utils.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;

public class UI implements InventoryHolder {
    private int currentPage = 0;
    private int numberPage;
    private Player player;
    private @NotNull Inventory inventory;

    public UI(Player player) {
        this.player = player;
    }

    @NotNull
    @Override
    public Inventory getInventory() {

        inventory = Bukkit.createInventory(this, 27, BankingConfig.getInstance().getString("name"));

        List<BankOre> bank = Banking.getInstance().getStorage().retreiveBankStatus(player, null);

        numberPage = (int) Math.ceil(bank.size() / 8.0);

        updateUI();

        return inventory;
    }

    private void updateUI() {
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, new ItemStack(Material.RED_STAINED_GLASS_PANE));
        }

        List<BankOre> bank = Banking.getInstance().getStorage().retreiveBankStatus(player, null);
        int offset = 8 * currentPage;
        for (int i = 0; i < 8 && i + offset < bank.size(); i++) {
            BankOre bo = bank.get(i + offset);
            ItemStack oreStack;
            inventory.setItem(i, oreStack = withTitleAndLore(bo.getMaterial(), null, List.of(
                    "Balance: " + bo.getInBank())));

            if (bo.getInBank() > 0) {
                int withdrawable = bo.getInBank();
                withdrawable = Math.min(withdrawable, new ItemStack(bo.getMaterial()).getMaxStackSize());
                withdrawable = Math.min(withdrawable, getAmountOfSpaceFor(bo.getMaterial()));
                if (withdrawable > 0)
                    inventory.setItem(i + 18,
                            withTitleAndLore(Material.PLAYER_HEAD, oreStack.getI18NDisplayName(), List.of(
                                    "Withdraw: " + withdrawable), withdrawable));
            }

            var inventoryStock = player.getInventory().all((Material) bo.getMaterial());
            int inInventory = 0;
            for (var stack : inventoryStock.entrySet()) {
                inInventory += stack.getValue().getAmount();
            }

            if (inInventory > 0) {
                int depositable = inInventory;
                depositable = Math.min(depositable, new ItemStack(bo.getMaterial()).getMaxStackSize());

                inventory.setItem(i + 9, withTitleAndLore(Material.CHEST, oreStack.getI18NDisplayName(), List.of(
                        "Deposit: " + depositable), depositable));
            }
        }

        if (currentPage > 0)
            inventory.setItem(8, withTitleAndLore(Material.ARROW, "Back", null, 1));
        inventory.setItem(17, withTitleAndLore(Material.BARRIER, "Close", null, 1));
        if (currentPage < numberPage - 1)
            inventory.setItem(26, withTitleAndLore(Material.ARROW, "Next", null, 1));
    }

    private ItemStack withTitleAndLore(ItemStack itemStack, String title, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (title != null)
            meta.setDisplayName(title);
        if (lore != null)
            meta.setLore(lore);
        if (meta instanceof SkullMeta) {
            SkullMeta smeta = (SkullMeta) meta;
            smeta.setOwningPlayer(player);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack withTitleAndLore(Material m, String title, List<String> lore) {
        var itemStack = new ItemStack(m);
        itemStack.setAmount(itemStack.getMaxStackSize());
        return withTitleAndLore(itemStack, title, lore);
    }

    private ItemStack withTitleAndLore(Material m, String title, List<String> lore, int amount) {
        var itemStack = new ItemStack(m);
        itemStack.setAmount(amount);
        return withTitleAndLore(itemStack, title, lore);
    }

    public void click(InventoryClickEvent event) {
        Logger.trace("Clicked on slot {}", event.getSlot());

        int slot = event.getSlot();
        if (slot == 8) {
            if (currentPage > 0)
                currentPage--;
        }
        if (slot == 17) {
            inventory.close();
        }
        if (slot == 26) {
            if (currentPage < numberPage - 1)
                currentPage++;
        }
        handleOreClick(event.getSlot());
        updateUI();
    }

    private int getAmountOfSpaceFor(Material m) {
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

    private void addToInventory(Material m, int withdrawable) {
        var oneStack = new ItemStack(m).getMaxStackSize();
        var inventoryStock = player.getInventory().all(Material.AIR);

        inventoryStock = player.getInventory().all(m);
        for (var stack : inventoryStock.entrySet()) {
            var space = oneStack - stack.getValue().getAmount();
            var amountToAdd = Math.min(space, withdrawable);
            stack.getValue().setAmount(stack.getValue().getAmount() + amountToAdd);
            withdrawable -= amountToAdd;
        }

        var emptySlot = player.getInventory().firstEmpty();
        if (emptySlot >= 0) {
            var amount = Math.min(oneStack, withdrawable);
            var stack_ = new ItemStack(m);
            stack_.setAmount(amount);
            player.getInventory().setItem(emptySlot, stack_);
            withdrawable -= amount;
        }
    }

    private void handleOreClick(int slot) {
        List<BankOre> bank = Banking.getInstance().getStorage().retreiveBankStatus(player, null);
        int offset = 8 * currentPage;
        for (int i = 0; i < 8 && i + offset < bank.size(); i++) {
            BankOre bo = bank.get(i + offset);

            if (slot == i + 18) {
                int withdrawable = bo.getInBank();
                withdrawable = Math.min(withdrawable, new ItemStack(bo.getMaterial()).getMaxStackSize());
                withdrawable = Math.min(withdrawable, getAmountOfSpaceFor(bo.getMaterial()));
                if (withdrawable > 0) {
                    if (Banking.getInstance().getStorage().removeFromPlayerBank(bo.getMaterial(), player, withdrawable,
                            null))
                        addToInventory(bo.getMaterial(), withdrawable);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                } else
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
            }

            var inventoryStock = player.getInventory().all((Material) bo.getMaterial());
            int inInventory = 0;
            for (var stack : inventoryStock.entrySet()) {
                inInventory += stack.getValue().getAmount();
            }

            if (slot == i + 9) {
                int depositable = inInventory;
                depositable = Math.min(depositable, new ItemStack(bo.getMaterial()).getMaxStackSize());
                if (depositable > 0) {
                    if (Banking.getInstance().getStorage().addToPlayerBank(bo.getMaterial(), player, depositable,
                            null)) {
                        for (var stack : inventoryStock.entrySet()) {
                            var count = stack.getValue().getAmount();
                            if (count > depositable) {
                                stack.getValue().setAmount(count - depositable);
                                break;
                            } else {
                                depositable -= count;
                                player.getInventory().setItem(stack.getKey(), new ItemStack(Material.AIR));
                            }
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
                    }
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
                } else
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);

            }
        }
    }

}
