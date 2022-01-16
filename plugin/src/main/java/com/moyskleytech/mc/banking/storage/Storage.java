package com.moyskleytech.mc.banking.storage;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.directory.InvalidAttributesException;

import com.moyskleytech.mc.banking.Banking;
import com.moyskleytech.mc.banking.config.BankingConfig;
import com.moyskleytech.mc.banking.utils.Logger;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Storage {

    public Storage() {
        Connection c = null;

        try {
            c = establish();
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                Logger.error("Could not close connection to database");
            }
        }
    }

    private Connection establish() {
        Connection c = null;
        try {
            File dataFolder = Banking.getPluginInstance().getDataFolder();
            Class.forName("org.sqlite.JDBC");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            c = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/bank.db");
            var stmt = c.createStatement();
            try {
                String sql = "CREATE TABLE BALANCES " +
                        "(amount INTEGER NOT NULL," +
                        " material TEXT NOT NULL, " +
                        " gp TEXT NOT NULL, " +
                        " uuid TEXT NOT NULL, " +
                        " PRIMARY KEY(material,gp,uuid)) ";
                stmt.executeUpdate(sql);
                stmt.close();
            } catch (SQLException sq) {

            }
            try {
                String sql = "CREATE TABLE GROUPS " +
                        "(world TEXT NOT NULL," +
                        " gp TEXT NOT NULL, " +
                        " PRIMARY KEY(world,gp)) ";
                stmt.executeUpdate(sql);
                stmt.close();
            } catch (SQLException sq) {

            }

        } catch (Exception e) {
            Logger.error("Could not open database");
            Logger.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return c;
    }

    public List<BankOre> retreiveBankStatus(Player player, String groupName) {
        if (groupName == null)
            groupName = getGroupForCurrentLocation(player);
        String fGroupName = groupName;
        var ores = BankingConfig.getInstance().getStringList("ores");

        List<Material> ores_material = ores.stream().map((s) -> Material.matchMaterial(s)).collect(Collectors.toList());

        List<BankOre> ret = ores_material.stream().map(m -> getBalanceFor(m, player, fGroupName))
                .collect(Collectors.toList());
        return ret;
    }

    public BankOre getBalanceFor(Material material, Player player, String groupName) {
        if (groupName == null)
            groupName = getGroupForCurrentLocation(player);
        return getBalanceFor(material, (OfflinePlayer) player, groupName);
    }

    public BankOre getBalanceFor(Material material, OfflinePlayer player, String groupName) {
        int amount = 0;
        Connection c = null;
        try {
            c = establish();

            // PreparedStatement pstmt = conn.prepareStatement(sql);
            // pstmt.setString(1, name);
            // pstmt.setDouble(2, capacity);
            PreparedStatement stmt = c
                    .prepareStatement("SELECT amount FROM BALANCES WHERE material=? and gp=? and uuid=?;");
            stmt.setString(1, material.getKey().asString());
            stmt.setString(2, groupName);
            stmt.setString(3, player.getUniqueId().toString());
            ResultSet rs = stmt.executeQuery();

            // loop through the result set
            if (rs.next()) {
                amount = rs.getInt("amount");
            } else {
                amount = 0;
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            Logger.error("Could not query database :: {}", e);
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                Logger.error("Could not close connection to database");
            }
        }

        return new BankOre(material, amount);
    }

    public boolean setBalanceFor(Material material, OfflinePlayer player, int amount, String groupName) {
        Connection c = null;
        try {
            c = establish();

            // PreparedStatement pstmt = conn.prepareStatement(sql);
            // pstmt.setString(1, name);
            // pstmt.setDouble(2, capacity);
            PreparedStatement istmt = c
                    .prepareStatement("INSERT INTO BALANCES(amount,material,gp,uuid) VALUES(?,?,?,?)");
            istmt.setInt(1, amount);
            istmt.setString(2, material.getKey().asString());
            istmt.setString(3, groupName);
            istmt.setString(4, player.getUniqueId().toString());

            PreparedStatement ustmt = c
                    .prepareStatement("UPDATE BALANCES set amount=? where material=? and gp=? and uuid=?");
            ustmt.setInt(1, amount);
            ustmt.setString(2, material.getKey().asString());
            ustmt.setString(3, groupName);
            ustmt.setString(4, player.getUniqueId().toString());

            if (ustmt.executeUpdate() == 0)
                if (istmt.executeUpdate() == 0) {
                    Logger.error("Coult not update balance");
                    return false;
                }
            return true;
        } catch (Exception e) {
            Logger.error("Could not query database :: {}", e);
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                Logger.error("Could not close connection to database");
            }
        }
        return false;
    }

    public String getGroupForCurrentLocation(Player p) {
        return getGroupForWorld(p.getWorld().getName());
    }

    public HashMap<String, List<String>> getGroupWorldMapping() {
        var ret = new HashMap<String, List<String>>();
        var map = getWorldGroupMapping();

        for (var iterable_element : map.entrySet()) {
            ret.putIfAbsent(iterable_element.getValue(), new ArrayList<>());
            ret.get(iterable_element.getValue()).add(iterable_element.getKey());
        }
        return ret;
    }

    public HashMap<String, String> getWorldGroupMapping() {
        HashMap<String, String> map = new HashMap<>();

        Connection c = null;
        try {
            c = establish();

            /*
             * String sql = "CREATE TABLE GROUPS " +
             * "(world TEXT NOT NULL," +
             * " gp TEXT NOT NULL, " +
             * " PRIMARY KEY(world,gp)) ";
             */

            PreparedStatement stmt = c
                    .prepareStatement("SELECT gp,world FROM GROUPS;");
            ResultSet rs = stmt.executeQuery();

            // loop through the result set
            while (rs.next()) {
                map.putIfAbsent(rs.getString("world"), rs.getString("gp"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            Logger.error("Could not query database :: {}", e);
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                Logger.error("Could not close connection to database");
            }
        }
        return map;
    }

    private String getGroupForWorld(@NotNull String name) {
        // if {_world} is not "world" or "world_nether" or "world_the_end":
        // set {_world} to {bank.groups.reverse::%{_world}%} if
        // {bank.groups.reverse::%{_world}%} is set/** */
        String group = "default";
        Connection c = null;
        try {
            c = establish();

            /*
             * String sql = "CREATE TABLE GROUPS " +
             * "(world TEXT NOT NULL," +
             * " gp TEXT NOT NULL, " +
             * " PRIMARY KEY(world,gp)) ";
             */

            PreparedStatement stmt = c
                    .prepareStatement("SELECT gp FROM GROUPS WHERE world=?;");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            // loop through the result set
            if (rs.next())
                group = rs.getString("gp");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            Logger.error("Could not query database :: {}", e);
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                Logger.error("Could not close connection to database");
            }
        }

        return group;
    }

    public boolean addToPlayerBank(Material material, Player player, int depositable, String groupName) {
        if (groupName == null)
            groupName = getGroupForCurrentLocation(player);
        return addToPlayerBank(material, (OfflinePlayer) player, depositable, groupName);
    }

    public boolean addToPlayerBank(Material material, OfflinePlayer player, int depositable, String groupName) {
        Logger.trace("Depositing {} {} for {} in group {}", depositable, material.getKey().asString(), player,
                groupName);

        var balance = getBalanceFor(material, player, groupName);
        return setBalanceFor(material, player, balance.getInBank() + depositable, groupName);
    }

    public boolean removeFromPlayerBank(Material material, Player player, int withdrawable, String groupName) {
        if (groupName == null)
            groupName = getGroupForCurrentLocation(player);

        return removeFromPlayerBank(material, (OfflinePlayer) player, withdrawable, groupName);
    }

    public boolean removeFromPlayerBank(Material material, OfflinePlayer player, int withdrawable, String groupName) {
        Logger.trace("Withdraw {} {} for {} in group {}", withdrawable, material.getKey().asString(), player,
                groupName);

        var balance = getBalanceFor(material, player, groupName);
        return setBalanceFor(material, player, balance.getInBank() - withdrawable, groupName);
    }

    public void setGroupForWorld(@NotNull World world, String group) {
        Connection c = null;
        try {
            c = establish();

            // PreparedStatement pstmt = conn.prepareStatement(sql);
            // pstmt.setString(1, name);
            // pstmt.setDouble(2, capacity);
            PreparedStatement istmt = c
                    .prepareStatement("INSERT INTO GROUPS(gp,world) VALUES(?,?)");
            istmt.setString(1, group);
            istmt.setString(2, world.getName());

            PreparedStatement ustmt = c
                    .prepareStatement("DELETE FROM GROUPS where world=?");
            ustmt.setString(1, world.getName());

            ustmt.executeUpdate();
            if (istmt.executeUpdate() == 0) {
                Logger.error("Coult not update balance");
            }
        } catch (Exception e) {
            Logger.error("Could not query database :: {}", e);
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                Logger.error("Could not close connection to database");
            }
        }
    }
}
