package io.github.anjoismysign.phatlootsHelp;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandBuilder;
import io.github.anjoismysign.skeramidcommands.server.PermissionMessenger;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class PhatlootsHelp extends JavaPlugin {

    private HelpConfig helpConfig;

    @Override
    public void onEnable(){
        Command command = CommandBuilder.of("phatlootshelp").build();
        Command reload = command.child("reload");
        reload.onExecute((permissionMessenger, args) -> {
            reload();
            permissionMessenger.sendMessage("PhatlootsHelp reloaded!");
        });
        Command linkAll = command.child("linkall");
        linkAll.onExecute(((permissionMessenger, args) -> {
            @Nullable Player player = player(permissionMessenger);
            if (player == null){
                return;
            }
            @Nullable PhatLoot phatLoot = PhatLootsAPI.getPhatLoot(helpConfig.getPhatLoot());
            if (phatLoot == null){
                return;
            }
            Location location = player.getLocation();
            World world = player.getWorld();
            int radius = helpConfig.getRadius();
            Location lowest = location.clone().subtract((double) radius /2, (double) radius /2, (double) radius /2);
            Location highest = location.clone().add((double) radius /2, (double) radius /2, (double) radius /2);

            Cuboid cuboid = new Cuboid(lowest, highest);

            List<Block> blocks = new ArrayList<>();
            for (int x = lowest.getBlockX(); x <= highest.getBlockX(); x++) {
                for (int y = lowest.getBlockY(); y <= highest.getBlockY(); y++) {
                    for (int z = lowest.getBlockZ(); z <= highest.getBlockZ(); z++) {
                        Block block = world.getBlockAt(x,y,z);
                        if (block.getType() != helpConfig.getBlockType()){
                            continue;
                        }
                        blocks.add(block);
                    }
                }
            }
            Bukkit.getScheduler().runTask(this, ()->{
                blocks.forEach(phatLoot::addChest);
                phatLoot.saveChests();
                permissionMessenger.sendMessage("linkin done!");
                List<Location> edges = cuboid.drawEdges(0.5);
                final int[] timer = {0};
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if (timer[0] >= 200){
                            cancel();
                            return;
                        }
                        edges.forEach(loc->{
                            world.spawnParticle(Particle.CRIT, loc,1,0,0,0,0);
                        });
                        timer[0]++;
                    }
                }.runTaskTimer(this, 1, 1);
            });
        }));
        reload();
    }


    @Nullable
    private Player player(@NotNull PermissionMessenger permissionMessenger) {
        CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
        if (!(sender instanceof Player player)) {
            permissionMessenger.sendMessage("cannot run as console!");
            return null;
        }
        return player;
    }

    public void reload() {
        String fileName = "config.yml";
        File dataFolder = getDataFolder();
        File configurationFile = new File(dataFolder, fileName);
        saveResource(fileName, false);

        Constructor constructor = new Constructor(HelpConfig.class, new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        try (FileInputStream inputStream = new FileInputStream(configurationFile)) {
            helpConfig = yaml.load(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
