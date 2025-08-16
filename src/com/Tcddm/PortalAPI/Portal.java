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
    private PortalDirection direction;
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


    public void generatePortal(){

        setPortalFrame(portalFrameBlockType);
    }
    public void cleanPortal(){
        setPortalFrame(Material.AIR);
        stopParticleEffect();
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
    public void portalSetting(String type, int length, int width, int waitTime,
                              Material portalBlockType, PortalDirection direction) {
        Type = type;
        this.length = length;
        this.width = width;
        this.portalFrameBlockType = portalBlockType;
        this.waitTime = waitTime;
        this.direction = direction;  // 设置方向
    }

    public Boolean isInPortal(Location loc){
        // 获取传送门位置和方向
        Location portalLoc = getPortalLocation();
        PortalDirection direction = getDirection(); // 需要Portal类提供getDirection()方法

        int x = portalLoc.getBlockX();
        int y = portalLoc.getBlockY();
        int z = portalLoc.getBlockZ();

// 根据传送门方向判断位置是否在内部
        if (direction == PortalDirection.X_AXIS) {
            // X轴延伸：宽度沿X轴，长度沿Y轴，Z轴固定
            return loc.getBlockX() >= x && loc.getBlockX() < x + getWidth()
                    && loc.getBlockY() >= y && loc.getBlockY() < y + getLength()
                    && loc.getBlockZ() == z;
        } else {
            // Z轴延伸：宽度沿Z轴，长度沿Y轴，X轴固定
            return loc.getBlockZ() >= z && loc.getBlockZ() < z + getWidth()
                    && loc.getBlockY() >= y && loc.getBlockY() < y + getLength()
                    && loc.getBlockX() == x;
        }
    }
    // 修改canGeneratePortal方法以支持方向判断
    public boolean canGeneratePortal() {
        if (portalLocation == null || portalLocation.getWorld() == null) {
            return false;
        }

        World world = portalLocation.getWorld();
        int x = portalLocation.getBlockX();
        int y = portalLocation.getBlockY();
        int z = portalLocation.getBlockZ();

        // 根据方向检查内部方块
        if (direction == PortalDirection.X_AXIS) {
            // X轴延伸 - 检查x和y方向
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < length; j++) {
                    Block block = world.getBlockAt(x + i, y + j, z);
                    if (!block.getType().equals(Material.AIR)) {
                        return false;
                    }
                }
            }
        } else {
            // Z轴延伸 - 检查z和y方向
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < length; j++) {
                    Block block = world.getBlockAt(x, y + j, z + i);
                    if (!block.getType().equals(Material.AIR)) {
                        return false;
                    }
                }
            }
        }

        // 根据方向检查边框
        if (direction == PortalDirection.X_AXIS) {
            // 上边框
            for (int i = -1; i <= width; i++) {
                Block block = world.getBlockAt(x + i, y - 1, z);
                if (!block.getType().equals(Material.AIR)) return false;
            }
            // 下边框
            for (int i = -1; i <= width; i++) {
                Block block = world.getBlockAt(x + i, y + length, z);
                if (!block.getType().equals(Material.AIR)) return false;
            }
            // 左边框
            for (int j = 0; j < length; j++) {
                Block block = world.getBlockAt(x - 1, y + j, z);
                if (!block.getType().equals(Material.AIR)) return false;
            }
            // 右边框
            for (int j = 0; j < length; j++) {
                Block block = world.getBlockAt(x + width, y + j, z);
                if (!block.getType().equals(Material.AIR)) return false;
            }
        } else {
            // 上边框
            for (int i = -1; i <= width; i++) {
                Block block = world.getBlockAt(x, y - 1, z + i);
                if (!block.getType().equals(Material.AIR)) return false;
            }
            // 下边框
            for (int i = -1; i <= width; i++) {
                Block block = world.getBlockAt(x, y + length, z + i);
                if (!block.getType().equals(Material.AIR)) return false;
            }
            // 左边框
            for (int j = 0; j < length; j++) {
                Block block = world.getBlockAt(x, y + j, z - 1);
                if (!block.getType().equals(Material.AIR)) return false;
            }
            // 右边框
            for (int j = 0; j < length; j++) {
                Block block = world.getBlockAt(x, y + j, z + width);
                if (!block.getType().equals(Material.AIR)) return false;
            }
        }

        return true;
    }

    // 修改生成框架的方法
    public boolean setPortalFrame(Material frameBlockType) {
        World world = portalLocation.getWorld();
        int x = portalLocation.getBlockX();
        int y = portalLocation.getBlockY();
        int z = portalLocation.getBlockZ();

        if (direction == PortalDirection.X_AXIS) {
            // 生成X轴方向的边框
            // 上边框
            for (int i = -1; i <= width; i++) {
                world.getBlockAt(x + i, y - 1, z).setType(frameBlockType);
            }
            // 下边框
            for (int i = -1; i <= width; i++) {
                world.getBlockAt(x + i, y + length, z).setType(frameBlockType);
            }
            // 左边框
            for (int j = 0; j < length; j++) {
                world.getBlockAt(x - 1, y + j, z).setType(frameBlockType);
            }
            // 右边框
            for (int j = 0; j < length; j++) {
                world.getBlockAt(x + width, y + j, z).setType(frameBlockType);
            }
        } else {
            // 生成Z轴方向的边框
            // 上边框
            for (int i = -1; i <= width; i++) {
                world.getBlockAt(x, y - 1, z + i).setType(frameBlockType);
            }
            // 下边框
            for (int i = -1; i <= width; i++) {
                world.getBlockAt(x, y + length, z + i).setType(frameBlockType);
            }
            // 左边框
            for (int j = 0; j < length; j++) {
                world.getBlockAt(x, y + j, z - 1).setType(frameBlockType);
            }
            // 右边框
            for (int j = 0; j < length; j++) {
                world.getBlockAt(x, y + j, z + width).setType(frameBlockType);
            }
        }


        return true;
    }

    // 修改获取内部方块位置的方法
    public List<Location> getPortalInternalAllBlockLocation() {
        List<Location> locs = new ArrayList<>();
        World world = portalLocation.getWorld();
        int x = portalLocation.getBlockX();
        int y = portalLocation.getBlockY();
        int z = portalLocation.getBlockZ();

        if (direction == PortalDirection.X_AXIS) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < length; j++) {
                    locs.add(new Location(world, x + i, y + j, z));
                }
            }
        } else {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < length; j++) {
                    locs.add(new Location(world, x, y + j, z + i));
                }
            }
        }
        return locs;
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

    public void setDirection(PortalDirection direction) {
        this.direction = direction;
    }

    public PortalDirection getDirection() {
        return direction;
    }

    public void setPortalLocation(Location portalLocation) {
        this.portalLocation = portalLocation;
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
