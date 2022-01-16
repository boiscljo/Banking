package com.moyskleytech.mc.banking.config;

import com.moyskleytech.mc.banking.utils.Logger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import com.moyskleytech.mc.banking.utils.BankingUtil;
import java.io.File;
import java.util.*;
import java.util.List;

@Service
public class BankingConfig {

    public static BankingConfig getInstance() {
        return ServiceManager.get(BankingConfig.class);
    }

    public JavaPlugin plugin;
    public File dataFolder;
    public File langFolder, shopFolder, gamesInventoryFolder;

    private ConfigurationNode configurationNode;
    private YamlConfigurationLoader loader;
    private ConfigGenerator generator;

    public BankingConfig(JavaPlugin plugin) {
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
                    .path(dataFolder.toPath().resolve("config.yml"))
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            configurationNode = loader.load();

            generator = new ConfigGenerator(loader, configurationNode);
            generator.start()
                    .key("version").defValue(plugin.getDescription().getVersion())
                    .key("locale").defValue("en")
                    .key("name").defValue("&f&k[&c&lB&c&la&c&ln&c&lk&f&k]&f")
                    .key("ores").defValue(
                            List.of(
                                    "minecraft:copper_ingot",
                                    Material.IRON_INGOT.getKey().asString(),
                                    Material.GOLD_INGOT.getKey().asString(),
                                    Material.LAPIS_LAZULI.getKey().asString(),
                                    Material.EMERALD.getKey().asString(),
                                    Material.DIAMOND.getKey().asString(),
                                    Material.NETHERITE_INGOT.getKey().asString(),
                                    Material.ENDER_PEARL.getKey().asString()
                                    ));

            generator.saveIfModified();
            if (!node("debug", "enabled").getBoolean()) {
                Logger.setMode(Logger.Level.DISABLED);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnPostEnable
    public void postEnable() {

    }

    public void forceReload() {
        loader = YamlConfigurationLoader
                .builder()
                .path(dataFolder.toPath().resolve("sbaconfig.yml"))
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        try {
            configurationNode = loader.load();
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
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
        return ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(node((Object[]) path.split("\\.")).getString()));
    }

    public String getString(String path, String def) {
        final var str = getString(path);
        if (str == null) {
            return def;
        }
        return str;
    }

    public Item readDefinedItem(ConfigurationNode node, String def) {
        if (!node.empty()) {
            var obj = node.raw();
            return ItemFactory.build(obj).orElse(ItemFactory.getAir());
        }

        return ItemFactory.build(def).orElse(ItemFactory.getAir());
    }

    public @NotNull String logo() {
        return getString("name");
    }
}
