package ru.xpyctum.SignStatus;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.*;

/**
 ████████████████████████████████████████████
 █──█──██────██──█──██────██───██─█─██─███─██
 ██───███─██─███───███─██─███─███─█─██──█──██
 ███─████────████─████─██████─███─█─██─█─█─██
 ██───███─███████─████─██─███─███─█─██─███─██
 █──█──██─███████─████────███─███───██─███─██
 ████████████████████████████████████████████
 */
public class Main extends PluginBase implements Listener{

    public Config tranlations;
    public Config config;
    public Config format;
    public Config signs;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();

        this.saveResource("config.yml");
        this.saveResource("format.yml");
        this.saveResource("translations.yml");
        this.saveResource("signs.yml");

        this.tranlations = new Config(this.getDataFolder()+"/translations.yml");
        this.config = new Config(this.getDataFolder()+"/config.yml");
        this.format = new Config(this.getDataFolder()+"/format.yml");
        this.signs = new Config(this.getDataFolder()+"/signs.yml");

        getServer().getPluginManager().registerEvents(this, this);

        this.getServer().getScheduler().scheduleRepeatingTask(new Refresher(this), config.getInt("time")*20);

        getLogger().info("SignStatus successfully loaded!");
    }

    @Override
    public void onDisable(){
        getLogger().info("SignStatus successfully disabled!");
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if(Objects.equals(event.getLine(0).toLowerCase(), "status") || Objects.equals(event.getLine(0).toLowerCase(), "[status]")){
            if(player.hasPermission("signstatus") || player.hasPermission("signstatus.create")){
                ConfigSection data = format.getSection("format");

                data.entrySet().forEach(entry ->{
                    int line = Integer.parseInt(entry.getKey());
                    String formattedText = formatedRow(entry.getValue().toString(),player.getLevel());
                    event.setLine(line-1,formattedText);
                });
                player.sendMessage(tranlations.getString("sign_created"));

                long unixTime = System.currentTimeMillis() / 1000L;
                List<Object> listObj = new ArrayList<>();

                listObj.add(new LinkedHashMap<String, Object>() {
                    {
                        put("z", event.getBlock().getZ());
                        put("y", event.getBlock().getY());
                        put("x", event.getBlock().getX());
                        put("level-name", player.getLevel().getName());
                    }
                });

                signs.set("sign"+Long.toString(unixTime),listObj);
                signs.save();
            }else{
                player.sendMessage(tranlations.getString("sign_no_perms"));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if(block.getId() == Block.SIGN_POST || block.getId() == Block.WALL_SIGN || block.getId() == Item.SIGN){
            BlockEntitySign blockEntitySign = (BlockEntitySign) block.getLevel().getBlockEntity(new Vector3(block.getX(),block.getY(),block.getZ()));
            if(blockEntitySign != null){
                if(this.isCreated(new Vector3(block.getX(),block.getY(),block.getZ()))){
                    if(player.hasPermission("signstatus.break")) {
                        this.remove(new Vector3(block.getX(), block.getY(), block.getZ()));
                        player.sendMessage(tranlations.getString("sign_destroyed"));
                    }else{
                        player.sendMessage(tranlations.getString("sign_no_perms"));
                    }
                }
            }
        }
    }

    public void remove(Vector3 pos){
        ConfigSection signs = this.signs.getRootSection();
        for(Map.Entry<String, Object> entry : signs.entrySet()){
            List<Map> cf = signs.getMapList(entry.getKey().toString());
            for(Map joinable : cf){
                double x = Double.parseDouble(joinable.get("x").toString());
                double y = Double.parseDouble(joinable.get("y").toString());
                double z = Double.parseDouble(joinable.get("z").toString());
                String level_name = (String) joinable.get("level-name");
                Level level = this.getServer().getLevelByName(level_name);
                if(level != null) {
                    if (x == pos.getX() && y == pos.getY() && z == pos.getZ() && Objects.equals(level.getName(), level_name)){
                        signs.remove(entry.getKey().toString());
                        this.signs.save();
                    }
                }
            }
        }
    }

    public boolean isCreated(Vector3 pos){
        ConfigSection signs = this.signs.getRootSection();
        for(Map.Entry<String, Object> entry : signs.entrySet()){
            List<Map> cf = signs.getMapList(entry.getKey().toString());
            for(Map joinable : cf){
                double x = Double.parseDouble(joinable.get("x").toString());
                double y = Double.parseDouble(joinable.get("y").toString());
                double z = Double.parseDouble(joinable.get("z").toString());
                String level_name = (String) joinable.get("level-name");
                Level level = this.getServer().getLevelByName(level_name);
                if(level != null) {
                    if (x == pos.getX() && y == pos.getY() && z == pos.getZ() && Objects.equals(level.getName(), level_name)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String formatedRow(String line, Level level){
        float tps = getServer().getTicksPerSecond();
        int players = getServer().getOnlinePlayers().size();
        int max_online = getServer().getMaxPlayers();
        float load = getServer().getTickUsage();
        String world_name = level.getName();

        line = line.replace("{ONLINE}",String.valueOf(players));
        line = line.replace("{MAX_ONLINE}",String.valueOf(max_online));
        line = line.replace("{WORLD_NAME}",world_name);
        line = line.replace("{TPS}",String.valueOf(tps));
        line = line.replace("{SERVER_LOAD}",String.valueOf(load));

        return line;
    }


}
