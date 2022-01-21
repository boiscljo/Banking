package com.moyskleytech.mc.banking.config;

import com.moyskleytech.mc.banking.utils.Logger;

import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import me.clip.placeholderapi.PlaceholderAPI;

import com.moyskleytech.mc.banking.Banking;
import com.moyskleytech.mc.banking.utils.BankingUtil;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;

@Service
public class LanguageConfig {

    public class LanguagePlaceholder {
        private int amount;
        private Player p;
        private OfflinePlayer target;
        private Material ore = Material.AIR;

        public LanguagePlaceholder(Player p2) {
            this.p = p2;
        }

        public LanguagePlaceholder ore(Material ore) {
            this.ore = ore;
            return this;
        }

        public LanguagePlaceholder with(Player p) {
            this.p = p;
            return this;
        }

        public LanguagePlaceholder target(OfflinePlayer p) {
            this.target = p;
            return this;
        }
        public LanguagePlaceholder amount(int p) {
            this.amount = p;
            return this;
        }

       
        public Component deposited(int amount) {
            this.amount = amount;
            return of("transaction.deposit");
        }

        public Component withdrawed(int amount) {
            this.amount = amount;
            return of("transaction.withdraw");
        }

        public Component aboveZero() {
            return of("above-zero");
        }

        public Component missingOre(int amount) {
            this.amount = amount;
            return of("missing-ore");
        }

        public Component notEnoughSpace(int amount) {
            this.amount = amount;
            return of("missing-space");
        }
        
        public Component invalidOre() {
            return of("invalid-ore");
        }
        
        public Component of(String s)
        {
            Component c = MiniMessage.get().parse(placeholders(getString(s)));
            Logger.trace("Loaded translation for {}={}", s, c);
            return c;
        }

        

        private String placeholders(String src) {
            String process = src;
            var papi = Banking.getInstance().papi();

            process = process.replaceAll("%sender%", p.getName())
                    .replaceAll("%ore%", new ItemStack(ore).getI18NDisplayName())
                    .replaceAll("%logo%", BankingConfig.getInstance().logo())
                    .replaceAll("%amount%", String.valueOf(amount));
            if (target != null)
                process = process.replaceAll("%target%", target.getName());
            if (papi != null)
                process = papi.process(process, p);
            return process;
        }

     

     
    }

    public static LanguageConfig getInstance() {
        return ServiceManager.get(LanguageConfig.class);
    }

    public LanguagePlaceholder with(Player p) {
        return new LanguagePlaceholder(p);
    }

    public JavaPlugin plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;

    public LanguageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadDefaults();
    }

    public ConfigurationNode node(Object... keys) {
        return configurationNode.node(keys);
    }

    public void loadDefaults() {
        this.dataFolder = plugin.getDataFolder();

        try {

            loader = YamlConfigurationLoader
                    .builder()
                    .path(dataFolder.toPath().resolve("language.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            configurationNode = loader.load();

            generator = new ConfigGenerator(loader, configurationNode);
            generator.start()
                    .key("version").defValue(plugin.getDescription().getVersion())
                    .section("transaction")
                    .key("deposit").defValue("%logo%&aDeposited %amount% %ore%")
                    .key("withdraw").defValue("%logo%&bWithdrew %amount% %ore%")
                    .back()
                    .key("above-zero").defValue("%logo%&cAmount must be above 0")
                    .key("missing-ore").defValue("%logo%&cYou do not have the required %amount% %ore%")
                    .key("invalid-ore").defValue("%logo%&c%ore% is not a valid bank ore")
                    .key("missing-space").defValue("%logo%&cYou do not have enough place in inventory for %amount% %ore%");

            generator.saveIfModified();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnPostEnable
    public void postEnable() {

    }

    public void forceReload() {
        loadDefaults();
    }

    private void saveFile(String fileName, String saveTo) {
        final var file = new File(dataFolder, fileName);
        if (!file.exists()) {
            plugin.saveResource(saveTo, false);
        }
    }

    private void deleteFile(String fileName) {
        final var file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    private void saveFile(String fileName) {
        saveFile(fileName, fileName);
    }

    public double getDouble(String path, double def) {
        return node((Object[]) path.split("\\.")).getDouble(def);
    }

    public void saveConfig() {
        try {
            this.loader.save(this.configurationNode);
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

    public List<String> getStringList(String string) {
        final var list = new ArrayList<String>();
        try {
            for (String s : Objects.requireNonNull(node((Object[]) string.split("\\.")).getList(String.class))) {
                s = ChatColor.translateAlternateColorCodes('&', s);
                s = IridiumColorAPI.process(s);
                list.add(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public Integer getInt(String path, Integer def) {
        return node((Object[]) path.split("\\.")).getInt(def);
    }

    public Byte getByte(String path, Byte def) {
        final var val = node((Object[]) path.split("\\.")).getInt(def);
        if (val > 127 || val < -128)
            return def;
        return (byte) val;
    }

    public Boolean getBoolean(String path, boolean def) {
        return node((Object[]) path.split("\\.")).getBoolean(def);
    }

    public String getString(String path) {
        return IridiumColorAPI.process(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(node((Object[]) path.split("\\.")).getString())));
    }

    public String getString(String path, String def) {
        final var str = getString(path);
        if (str == null) {
            return def;
        }
        return str;
    }
}
