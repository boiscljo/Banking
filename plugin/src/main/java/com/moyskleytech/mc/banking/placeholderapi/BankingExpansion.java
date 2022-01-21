package com.moyskleytech.mc.banking.placeholderapi;

import com.moyskleytech.mc.banking.Banking;
import com.moyskleytech.mc.banking.utils.Logger;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankingExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "banking";
    }

    @Override
    public @NotNull String getAuthor() {
        return "boiscljo";
    }

    @Override
    public @NotNull String getVersion() {
        return Banking.getInstance().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        Logger.trace("Placeholder '" + identifier + "' was requested.");
        String[] identifiers = identifier.split("_");
        if (identifiers.length <= 1)
            return null;

        return super.onPlaceholderRequest(player, identifier);
    }

    public String process(String process,Player player) {
        return PlaceholderAPI.setPlaceholders(player, process);
    }
    

}
