package dev.mikan.modules.core;

import dev.mikan.packets.ParticleAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.Queue;

public class CoreDestructor extends BukkitRunnable {

    private final Queue<Block> blocks ;
    private final Core core;
    private @Getter boolean started;
    private @Getter @Setter boolean animated;
    private @Getter Runnable endTask;

    public CoreDestructor(Core core) {
        this.blocks = new LinkedList<>(core.getConstructor().getBlocks());
        this.core = core;
    }




    public void whenFinished(Runnable r){
        this.endTask = r;
    }




    public void start(){
        if (isStarted()) return;
        started = true;
        this.runTaskTimerAsynchronously(CoreModule.instance().getPluginInstance(), 0,3);
    }

    @Override
    public void run() {

        if (!animated) {
            this.blocks.forEach(block -> block.setType(Material.AIR));
            this.cancel();
            this.endTask.run();
            return;
        }

        if (!blocks.isEmpty()){
            Block block = blocks.poll();
            Location centeredLoc = block.getLocation().clone().add(0.5,0.5,0.5);

            for (Player player : block.getWorld().getPlayers()) {
                if (player.getLocation().distance(centeredLoc) < 64)
                    ParticleAPI.sendExplosionAndFlame(player,centeredLoc);
            }
            Bukkit.getScheduler().runTask(CoreModule.instance().getPlugin().getBootstrap(),() -> block.setType(Material.AIR));

            return;
        }


        this.cancel();
        this.endTask.run();
    }

}
