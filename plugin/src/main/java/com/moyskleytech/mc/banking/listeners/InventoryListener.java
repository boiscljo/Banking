package com.moyskleytech.mc.banking.listeners;

import com.moyskleytech.mc.banking.Banking;
import com.moyskleytech.mc.banking.ui.UI;
import com.moyskleytech.mc.banking.utils.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

@Service
public class InventoryListener implements Listener {

    @OnPostEnable
    public void onPostEnable() {
        Banking.getInstance().registerListener(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getInventory().getHolder() != null
                && event.getInventory().getHolder() instanceof UI) {

            event.setCancelled(true);
            if (event.getClickedInventory() == event.getInventory()) {
                UI gui = (UI) event.getInventory().getHolder();
                gui.click(event);
            }
        }
    }
}