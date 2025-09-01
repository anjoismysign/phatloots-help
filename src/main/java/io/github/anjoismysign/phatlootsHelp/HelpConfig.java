package io.github.anjoismysign.phatlootsHelp;

import org.bukkit.Material;

public class HelpConfig {

    private Material blockType;
    private String phatLoot;
    private int radius;

    public Material getBlockType() {
        return blockType;
    }

    public void setBlockType(Material blockType) {
        this.blockType = blockType;
    }

    public String getPhatLoot() {
        return phatLoot;
    }

    public void setPhatLoot(String phatLoot) {
        this.phatLoot = phatLoot;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
