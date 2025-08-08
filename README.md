# PortalAPI
PortalAPI 是在我的世界Java版插件中轻松创建和管理自定义传送门的 Java API，适用于基于 Bukkit/Spigot 框架的 Minecraft 插件开发。

## 主要类说明

1. **Portal**：传送门基类，包含传送门的基本属性和操作方法
    - 在类初始化时执行 `portalSetting(...)` 方法配置传送门参数
    - 提供粒子效果控制方法：`startParticleEffect()`、`stopParticleEffect()`
    - 需要重写 `handle(Player, PortalAPI)` 方法处理玩家进入传送门的交互
    - 需要重写`sendParticle(Location)`来实现播放粒子
    - 提供传送门激活状态设置：`setActivation(Boolean)`、`setPortalFrame(Material)`

2. **PortalAPI**：API 核心接口
    - 要重写 `getCanUsePortalPlayers()` 方法获取可使用传送门的玩家列表

3. **PortalManager**：传送门管理器
    - 负责管理所有传送门实例
    - 提供传送门任务管理、状态检查等功能
    - 可通过 `getAllPortals()` 获取所有传送门列表
    - 提供任务控制方法：`startPortalCheekTask()`、`startPortalWaitTask(Player, Portal)`、`stopPortalWaitTask(Player)`
    - 提供提示消息设置：`setWaitMsg(String)`

4. **PortalAPIInfo**：API 信息类
    - 包含 API 版本、作者等信息
    - 通过 `toString()` 方法获取信息字符串

## 文档说明

API 文档已通过 Javadoc 生成，位于 `JavaDoc` 目录下，可通过浏览器打开查看详细文档。
## 依赖环境

- JDK 1.8 及以上
- Bukkit/Spigot 服务器环境

## 使用示例

### 1. 定义自定义传送门

```java
import com.Tcddm.PortalAPI.Portal;
import com.Tcddm.PortalAPI.PortalAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class YourPortal extends Portal {

    public YourPortal(Location portalLocation) {
        //初始化,plugin为插件主类实例
        super(plugin,portalLocation);
        //自定义配置
        //（类型，内部长度，内部宽度，进入等待时间，框架方块）
        portalSetting("YourPortal",5,5,3,Material.DIAMOND_BLOCK);
    }

    @Override
    public void handle(Player player, PortalAPI api) {
        //玩家进入传送门后的处理
        player.sendMessage("Hello MyPortal!");
    }
    @Override
    public void sendParticle(Location loc){
        //播放粒子的方法
    }
}
```
### 2. 实现接口
```java
public class main extends JavaPlugin implements PortalAPI {
    @Override
    public List<Player> getCanUsePortalPlayers() {
        //所有在线玩家
        return Bukkit.getOnlinePlayers().stream()
                .collect(Collectors.toList());
        //有portal.use权限的玩家
        return onlinePlayers.stream()
                .filter(player -> player.hasPermission("portal.use"))
                .collect(Collectors.toList());
    }
}
```
### 3. 初始化和销毁
```java
public class main extends JavaPlugin implements PortalAPI {
    private PortalManager portalManager;
    public PortalManager getPortalManager() {
        return portalManager;
    }
    @Override
    public void onEnable() {
       portalManager=new PortalManager(this,this,"正在进入传送门,还剩{time}秒");
    }
    @Override
    public void onDisable() {
       portalManager.cleanupAllPortals();
    }
}
```
### 4. 创建传送门
```
Location loc=player.getLocation();
loc.add(-2,1,0);
System.out.print(getPortalManager().addPortal(new YourPortal(loc)));
```
## 如何在项目中使用
PortalAPI并非独立插件，无法在服务器中使用，将其加入项目需要首先在IDE中依赖jar文件，并在你项目的编译产物中添加PortalAPI的编译产物
