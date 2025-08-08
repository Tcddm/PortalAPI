package com.Tcddm.PortalAPI;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 传送门管理器
 */
public class PortalManager {

    // 关联的游戏实例
    private final PortalAPI api;
    private Plugin plugin;
    // 存储当前游戏的所有传送门（按类型分组便于管理）
    private final List<Portal> portals = new ArrayList<>();
    private BukkitTask portalCheekTask;
    private Map<Player,BukkitTask> portalWaitTasks;
    private String waitMsg;
    // 构造方法：与游戏实例绑定
    public PortalManager(Plugin plugin,PortalAPI api,String waitMsg) {
        this.api = api;
        this.plugin= plugin;
        this.waitMsg= waitMsg;
        this.portalWaitTasks=new HashMap<Player,BukkitTask>();
    }

    /**
     * 启动进入传送门检查任务
     */
    public void startPortalCheekTask(){
        if(portalCheekTask !=null){
            portalCheekTask.cancel();}
        portalCheekTask =new BukkitRunnable() {
            @Override
            public void run() {
               for(Player player: api.getCanUsePortalPlayers()){
                   Portal portal=getPortalAtLocation(player);
                   if(portal==null){stopPortalWaitTask(player);continue;}
                startPortalWaitTask(player,portal);
               }
            }

        }.runTaskTimer(plugin, 0L, 10L);
    }

    /**
     * 通过玩家判断传送门方向
     * @param player 玩家
     * @return 传送门方向
     */
    public PortalDirection getDirectionByPlayerFacing(Player player) {
        Location loc = player.getLocation();
        float yaw = loc.getYaw();  // 获取玩家朝向角度（Yaw角）

        // 标准化Yaw角（转换为0-360度）
        yaw = (yaw % 360 + 360) % 360;

        // 判断朝向：
        // 1. 315°-45° 为北方（North），135°-225° 为南方（South）→ 沿Z轴延伸
        // 2. 45°-135° 为东方（East），225°-315° 为西方（West）→ 沿X轴延伸
        if ((yaw >= 315 && yaw < 360) || (yaw >= 0 && yaw < 45) || (yaw >= 135 && yaw < 225)) {
            return PortalDirection.X_AXIS;  // 南北方向 → Z轴延伸
        } else {
            return PortalDirection.Z_AXIS;  // 东西方向 → X轴延伸
        }
    }
    /**
     * 启动玩家进入传送门处理任务,倒计时完成后触发传送门的handle方法
     * @param player 玩家
     * @param portal 玩家所在的传送门
     */
    public void startPortalWaitTask(Player player,Portal portal){
        if(portalWaitTasks.containsKey(player)){return;}
       BukkitTask Task =new BukkitRunnable() {
           int time=portal.getWaitTime();
            @Override
            public void run() {
               if(time==0){
                   portal.handle(player, api);
                   cancel();
                   return;
               }
               player.sendMessage(waitMsg.replace("{time}",String.valueOf(time)));
                time--;
            }

        }.runTaskTimer(plugin, 0L, 20L);
       portalWaitTasks.put(player,Task);
    }

    /**
     * 停止玩家进入传送门处理任务
     * @param player 玩家
     */
    public void stopPortalWaitTask(Player player){
        if(!portalWaitTasks.containsKey(player)){return;}
        BukkitTask task=portalWaitTasks.get(player);
        task.cancel();
        portalWaitTasks.remove(player);
    }

    /**
     * 添加传送门
     * @param portal 传送门
     * @param direction 方向
     * @return 是否成功创建
     */
    public boolean addPortal(Portal portal,PortalDirection direction){
        // 检查传送门是否已存在（同位置同类型）
        if (isPortalExists(portal)) {
            return false;
        }
        portal.setDirection(PortalDirection.Z_AXIS);
        // 检查生成合法性并生成方块
        portal.setDirection(direction);
        if (portal.canGeneratePortal()) {
            portal.generatePortal();
            portals.add(portal);
            return true;
        }
        return false;
    }

    /**
     * 添加传送门（通过玩家朝向判断传送门方向，玩家点击的方块的位置来判断生成位置）
     * @param portal 传送门(初始化可以不传入Location)
     * @param player 玩家
     * @param clickedBlockLocation 玩家点击的方块的位置
     * @return 是否成功创建
     */
    public boolean addPortal(Portal portal,Player player,Location clickedBlockLocation){
        PortalDirection direction=getDirectionByPlayerFacing(player);
        Location loc=clickedBlockLocation;
        int temp=(int)portal.getWidth()/2;
        if(direction==PortalDirection.X_AXIS){

            loc.add(-temp,2,0);
        }else{
            loc.add(0,2,-temp);
        }
        portal.setPortalLocation(loc);
        return addPortal(portal,direction);
    }

    /**
     * 移除并清理传送门（删除方块并从列表中移除）
     * @param portal 要移除的传送门
     */
    public void removePortal(Portal portal) {
        if (portals.contains(portal)) {
            portal.cleanPortal(); // 清理方块
            portals.remove(portal);
        }
    }
    /**
     * 按类型获取传送门列表（如 "WinPortal"）
     * @param type 传送门类型（对应 Portal.getType()）
     * @return 同类型传送门列表
     */
    public List<Portal> getPortalsByType(String type) {
        return portals.stream()
                .filter(portal -> type.equals(portal.getType()))
                .collect(Collectors.toList());
    }

    /**
     * 获得玩家所在的传送门
     * @param player 玩家
     * @return 玩家所在的传送门
     */
    public Portal getPortalAtLocation(Player player) {
        Location loc=player.getLocation();
        loc.add(0,1,0);
        return getPortalAtLocation(loc);
    }
    /**
     * 检查位置是否在某个传送门内部（用于玩家交互检测）
     * @param loc 待检测位置
     * @return 若在传送门内部，返回该传送门；否则返回 null
     */
    public Portal getPortalAtLocation(Location loc) {
        for (Portal portal : portals) {


            if(!portal.getActivation()){continue;}
            if(portal.isInPortal(loc)){
                return portal;
            }

           // Location portalLoc = portal.getPortalLocation();
            //            int x = portalLoc.getBlockX();
            //            int y = portalLoc.getBlockY();
            //            int z = portalLoc.getBlockZ();
            //            // 检查位置是否在传送门的宽度（x轴）和长度（y轴）范围内
            //            if (loc.getBlockX() >= x && loc.getBlockX() < x + portal.getWidth()
            //                    && loc.getBlockY() >= y && loc.getBlockY() < y + portal.getLength()
            //                    && loc.getBlockZ() == z) { // z轴固定（传送门为平面）
            //                return portal;
            //            }

        }
        return null;
    }

    /**
     * 清理当前游戏的所有传送门（游戏或插件结束时调用）
     */
    public void cleanupAllPortals() {
        if(portalCheekTask!=null){portalCheekTask.cancel();}
        portals.forEach(Portal::cleanPortal); // 清理所有传送门的方块
        portals.clear(); // 清空列表
    }

    /**
     * 检查传送门是否已存在（避免同位置同类型重复生成）
     */
    private boolean isPortalExists(Portal portal) {
        Location loc = portal.getPortalLocation();
        String type = portal.getType();
        return portals.stream().anyMatch(p ->
                p.getType().equals(type) &&
                        p.getPortalLocation().equals(loc)
        );
    }



    public void setWaitMsg(String waitMsg) {
        this.waitMsg = waitMsg;
    }

    public BukkitTask getPortalCheekTask() {
        return portalCheekTask;
    }

    public Map<Player, BukkitTask> getPortalWaitTasks() {
        return portalWaitTasks;
    }

    // 获取当前游戏的所有传送门
    public List<Portal> getAllPortals() {
        return new ArrayList<>(portals); // 返回副本，避免外部直接修改
    }
}