package com.moyskleytech.mc.banking.storage;

import org.bukkit.Material;

import lombok.Getter;

public class BankOre {

    private Material material;
    private int inBank;

    public BankOre(Material ironIngot, int i) {
        material = ironIngot;
        inBank = i;
    }

    public Material getMaterial() {
        return material;
    }

    public int getInBank() {
        return inBank;
    }
}
