package dev.mikan.modules.core;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CoreConstructor extends BukkitRunnable {

    private final Queue<Block> lanterns = new LinkedList<>();
    private final Queue<Block> glassUpper = new LinkedList<>();
    private final Queue<Block> glassLower = new LinkedList<>();
    private final @Getter Block startBlock;
    private final @Getter List<Block> blocks = new ArrayList<>();
    private final @Getter List<Location> blockLocations = new ArrayList<>();

    private final int x;
    private final int y;
    private final int z;
    private final World world;
    private @Getter Runnable endTask;

    private @Getter boolean valid;

    private @Getter boolean started;

    public CoreConstructor(Block startBlock) {
        this.startBlock = startBlock;

        x = startBlock.getX();
        y = startBlock.getY();
        z = startBlock.getZ();

        world = startBlock.getWorld();

        this.loadBlocks();
    }

    public void start(){
        if (isStarted()) return;
        started = true;
        this.runTaskTimer(CoreModule.instance().getPluginInstance(), 0,3);
    }

    public void whenFinished(Runnable r){
        this.endTask = r;
    }

    @Override
    public void run() {

        int BATCH_SIZE = 2;
        if (!lanterns.isEmpty()) {
            for (int i = 0; i < BATCH_SIZE && !lanterns.isEmpty(); i++) {
                lanterns.poll().setType(Material.SEA_LANTERN);
            }
            return;
        }

        if (!glassUpper.isEmpty() || !glassLower.isEmpty()) {
            for (int i = 0; i < BATCH_SIZE && (!glassLower.isEmpty() || !glassUpper.isEmpty()); i++) {
                Block upperBlock = glassUpper.poll();
                Block lowerBlock = glassLower.poll();

                setGlass(upperBlock);
                setGlass(lowerBlock);

            }
            return;
        }


        this.cancel();
        endTask.run();
    }

    private void setGlass(Block block){
        if (block == null) return;
        block.setType(Material.STAINED_GLASS);
        block.setData((byte) 3);
    }

    private void loadBlocks(){

        blocks.add(this.startBlock);

        surround(3);
        surround(4);

        this.lanterns.add(this.startBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP));
        this.lanterns.add(this.startBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP));
        this.lanterns.add(this.startBlock.getRelative(BlockFace.UP));

        this.glassUpper.add(world.getBlockAt(x,y+6,z));
        this.glassLower.add(this.startBlock);

        surroundLower(this.startBlock.getRelative(BlockFace.UP));
        surroundLower(this.startBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP));

        surroundUpper(world.getBlockAt(x,y+5,z));

        surroundUpper(world.getBlockAt(x + 1,y+4,z));
        surroundUpper(world.getBlockAt(x - 1,y+4,z));
        surroundUpper(world.getBlockAt(x,y+4,z + 1));
        surroundUpper(world.getBlockAt(x,y+4,z - 1));

        surroundLower(world.getBlockAt(x + 1, y + 3 , z));
        surroundLower(world.getBlockAt(x - 1, y + 3 , z));
        surroundLower(world.getBlockAt(x, y + 3 , z + 1));
        surroundLower(world.getBlockAt(x, y + 3 , z - 1));

        blocks.addAll(this.glassLower);
        blocks.addAll(this.glassUpper);
        blocks.addAll(this.lanterns);

        blocks.forEach(block -> this.blockLocations.add(block.getLocation()));


        startBlock.setType(Material.AIR);

        valid = blockLocations.stream().allMatch(location -> {
            Faction blockFaction = Board.getInstance().getFactionAt(new FLocation(location.getChunk()));
            Faction startBlockFaction = Board.getInstance().getFactionAt(new FLocation(startBlock.getChunk()));
            return location.getBlock().getType() == Material.AIR &&
                    blockFaction.getId().equals(startBlockFaction.getId());
        });
    }

    public void surround(int y){
        y += this.y;
        this.lanterns.add(world.getBlockAt(x,y,z+1));
        this.lanterns.add(world.getBlockAt(x,y,z-1));
        this.lanterns.add(world.getBlockAt(x+1,y,z));
        this.lanterns.add(world.getBlockAt(x-1,y,z));
    }



    private void surroundUpper(Block block){

        if (!this.glassUpper.contains(block.getRelative(BlockFace.EAST))) {
            this.glassUpper.add(block.getRelative(BlockFace.EAST));
        }
        if (!this.glassUpper.contains(block.getRelative(BlockFace.WEST))) {
            this.glassUpper.add(block.getRelative(BlockFace.WEST));
        }
        if (!this.glassUpper.contains(block.getRelative(BlockFace.NORTH))) {
            this.glassUpper.add(block.getRelative(BlockFace.NORTH));
        }
        if (!this.glassUpper.contains(block.getRelative(BlockFace.SOUTH))) {
            this.glassUpper.add(block.getRelative(BlockFace.SOUTH));
        }


    }

    private void surroundLower(Block block){

        if (!this.glassLower.contains(block.getRelative(BlockFace.EAST))) {
            this.glassLower.add(block.getRelative(BlockFace.EAST));
        }
        if (!this.glassLower.contains(block.getRelative(BlockFace.WEST))) {
            this.glassLower.add(block.getRelative(BlockFace.WEST));
        }
        if (!this.glassLower.contains(block.getRelative(BlockFace.NORTH))) {
            this.glassLower.add(block.getRelative(BlockFace.NORTH));
        }
        if (!this.glassLower.contains(block.getRelative(BlockFace.SOUTH))) {
            this.glassLower.add(block.getRelative(BlockFace.SOUTH));
        }

    }


    public static void surround(Set<Block> blockSet, Block startBlock, int offsetY){
        int y = startBlock.getY() + offsetY;
        int x = startBlock.getX();
        int z = startBlock.getZ();
        World world = startBlock.getWorld();
        blockSet.add(world.getBlockAt(x,y,z+1));
        blockSet.add(world.getBlockAt(x,y,z-1));
        blockSet.add(world.getBlockAt(x+1,y,z));
        blockSet.add(world.getBlockAt(x-1,y,z));
    }


    public static Set<Block> getBlocks(Block startBlock){
        Set<Block> blockSet = new HashSet<>();
        int x = startBlock.getX();
        int z = startBlock.getZ();
        int y = startBlock.getY();
        World world = startBlock.getWorld();

        surround(blockSet,startBlock,3);
        surround(blockSet,startBlock,4);

        blockSet.add(startBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP));
        blockSet.add(startBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP));
        blockSet.add(startBlock.getRelative(BlockFace.UP));

        blockSet.add(startBlock.getWorld().getBlockAt(startBlock.getX(),startBlock.getY()+6,startBlock.getZ()));

        surround(blockSet,startBlock,1);
        surround(blockSet,startBlock,2);

        surround(blockSet,startBlock,5);

        surround(blockSet,world.getBlockAt(x + 1,y,z),4);
        surround(blockSet,world.getBlockAt(x - 1,y,z),4);
        surround(blockSet,world.getBlockAt(x,y,z + 1),4);
        surround(blockSet,world.getBlockAt(x,y,z - 1),4);



        surround(blockSet,world.getBlockAt(x + 1,y,z),3);
        surround(blockSet,world.getBlockAt(x - 1,y,z),3);
        surround(blockSet,world.getBlockAt(x,y,z + 1),3);
        surround(blockSet,world.getBlockAt(x,y,z - 1),3);


        return blockSet;
    }



}
