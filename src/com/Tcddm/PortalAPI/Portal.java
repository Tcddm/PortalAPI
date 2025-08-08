package com.Tcddm.PortalAPI;



import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 传送门基类
 */
public class Portal {
    private String Type;
    private int length;
    private int width;
    private Material portalFrameBlockType;
    private Location portalLocation;
    private BukkitTask particleTask;
    private Plugin plugin;
    private Boolean isActivation=false;
    private int waitTime;
    public Portal(Plugin plugin, Location portalLocation) {
        this.portalLocation = portalLocation;
        this.plugin=plugin;
    }

    public void portalSetting(String type, int length, int width,int waitTime, Material portalBlockType) {
        Type = type;
        this.length = length;
        this.width = width;
        this.portalFrameBlockType = portalBlockType;
        this.waitTime=waitTime;
    }

    public boolean canGeneratePortal() {
        
        if (portalLocation == null || portalLocation.getWorld() == null) {
            return false;
        }

        World world = portalLocation.getWorld();
        int x = portalLocation.getBlockX();
        int y = portalLocation.getBlockY();
        int z = portalLocation.getBlockZ();

        // 检查传送门区域内的所有方块是否为空气（仅允许空气被替换）
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                Block block = world.getBlockAt(x + i, y + j, z);
                // 只允许空气方块
                if (!block.getType().equals(Material.AIR)) {
                    return false;
                }
            }
        }

        // 检查框架位置是否有足够空间（框架位置必须为空气）
        // 上边框
        for (int i = -1; i <= width; i++) {
            Block block = world.getBlockAt(x + i, y - 1, z);
            if (!block.getType().equals(Material.AIR)) {
                return false;
            }
        }

        // 下边框
        for (int i = -1; i <= width; i++) {
            Block block = world.getBlockAt(x + i, y + length, z);
            if (!block.getType().equals(Material.AIR)) {
                return false;
            }
        }

        // 左边框
        for (int j = 0; j < length; j++) {
            Block block = world.getBlockAt(x - 1, y + j, z);
            if (!block.getType().equals(Material.AIR)) {
                return false;
            }
        }

        // 右边框
        for (int j = 0; j < length; j++) {
            Block block = world.getBlockAt(x + width, y + j, z);
            if (!block.getType().equals(Material.AIR)) {
                return false;
            }
        }

        return true;
    }
    public void generatePortal(){

        setPortalFrame(portalFrameBlockType);
    }
    public void cleanPortal(){
        setPortalFrame(Material.AIR);
        stopParticleEffect();
    }
    public boolean setPortalFrame(Material frameBlockType) {
        World world = portalLocation.getWorld();
        int x = portalLocation.getBlockX();
        int y = portalLocation.getBlockY();
        int z = portalLocation.getBlockZ();

        // 生成上边框
        for (int i = -1; i <= width; i++) {
            world.getBlockAt(x + i, y - 1, z).setType(frameBlockType);
        }

        // 生成下边框
        for (int i = -1; i <= width; i++) {
            world.getBlockAt(x + i, y + length, z).setType(frameBlockType);
        }

        // 生成左边框
        for (int j = 0; j < length; j++) {
            world.getBlockAt(x - 1, y + j, z).setType(frameBlockType);
        }

        // 生成右边框
        for (int j = 0; j < length; j++) {
            world.getBlockAt(x + width, y + j, z).setType(frameBlockType);
        }

        // 生成传送门内部方块
        if(!isActivation){setActivation(true);}

        return true;
    }
    public List<Location> getPortalInternalAllBlockLocation(){
        List<Location> locs=new ArrayList<Location>();
        World world = portalLocation.getWorld();
        int x = portalLocation.getBlockX();
        int y = portalLocation.getBlockY();
        int z = portalLocation.getBlockZ();
        for (int i = 0; i < width; i++) {

            for (int j = 0; j < length; j++) {
                locs.add(new Location(world,x + i, y + j, z));


            }
        }
        return locs;
    }
    public void sendParticle(Location loc){
      return;
    }
    public void setActivation(Boolean activation) {
        isActivation = activation;
        if(activation){
            startParticleEffect();
        }else{stopParticleEffect();}
    }


    public void startParticleEffect() {
        // 取消可能已存在的粒子任务，避免重复
        stopParticleEffect();
        World world = portalLocation.getWorld();
        // 创建一个重复任务来播放粒子
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                // 检查世界是否有效
                if (portalLocation == null || portalLocation.getWorld() == null) {
                    cancel();
                    return;
                }
                for (Location particleLoc : getPortalInternalAllBlockLocation()) {
                        sendParticle(particleLoc);

                }
            }

        }.runTaskTimer(plugin, 0, 2); // 每2 ticks(0.1秒)播放一次

    }

    public void stopParticleEffect() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }
    public void handle(Player player,PortalAPI api){
        return;
    }
    public Boolean getActivation() {
        return isActivation;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public String getType() {
        return Type;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public Material getPortalFrameBlockType() {
        return portalFrameBlockType;
    }

    public Location getPortalLocation() {
        return portalLocation;
    }
}
