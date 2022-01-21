package com.moyskleytech.mc.banking.config;

import com.moyskleytech.mc.banking.utils.Logger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Material;
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
import com.moyskleytech.mc.banking.utils.BankingUtil;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
                    .section("gui")
                    .key("previous-item").defValue(Material.ARROW.name())
                    .key("close-item").defValue(Material.BARRIER.name())
                    .key("next-item").defValue(Material.ARROW.name())
                    .section("offset")
                    .key("ore").defValue(0)
                    .key("deposit").defValue(9)
                    .key("withdraw").defValue(18)
                    .back()
                    .back()
                    .key("ores").defValue(
                            List.of(
                                    "minecraft:copper_ingot",
                                    Material.IRON_INGOT.name(),
                                    Material.GOLD_INGOT.name(),
                                    Material.LAPIS_LAZULI.name(),
                                    Material.EMERALD.name(),
                                    Material.DIAMOND.name(),
                                    Material.NETHERITE_INGOT.name(),
                                    Material.ENDER_PEARL.name()));

            generator.saveIfModified();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnPostEnable
    public void postEnable() {

    }

    public void forceReload() {
        /*
         * loader = YamlConfigurationLoader
         * .builder()
         * .path(dataFolder.toPath().resolve("config.yml"))
         * .nodeStyle(NodeStyle.BLOCK)
         * .build();
         * 
         * try {
         * configurationNode = loader.load();
         * } catch (ConfigurateException e) {
         * e.printStackTrace();
         * }
         */
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

    public @NotNull GUIConfig gui() {
        return new GUIConfig();
    }

    public class GUIConfig {
        public @NotNull Material previous() {
            return Material.matchMaterial(getString("gui.previous-item"));
        }

        public @NotNull Material close() {
            return Material.matchMaterial(getString("gui.close-item"));
        }

        public @NotNull Material next() {
            return Material.matchMaterial(getString("gui.next-item"));
        }

        public @NotNull OffsetConfig offset() {
            return new OffsetConfig();
        }

        public class OffsetConfig {
            // withdraw
            public @NotNull int ore() {
                return getInt("gui.offset.ore", 0);
            }

            public @NotNull int deposit() {
                return getInt("gui.offset.deposit", 9);
            }

            public @NotNull int withdraw() {
                return getInt("gui.offset.withdraw", 18);
            }
        }
    }

    public @NotNull String logo() {
        return getString("name");
    }

    public @NotNull List<String> ores() {
        return getStringList("ores");
    }

    public @NotNull List<Material> oresMaterial() {
        return getStringList("ores").stream().map(s -> Material.matchMaterial(s)).collect(Collectors.toList());
    }
}
