package ru.xpyctum.SignStatus;

import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ████████████████████████████████████████████
 * █──█──██────██──█──██────██───██─█─██─███─██
 * ██───███─██─███───███─██─███─███─█─██──█──██
 * ███─████────████─████─██████─███─█─██─█─█─██
 * ██───███─███████─████─██─███─███─█─██─███─██
 * █──█──██─███████─████────███─███───██─███─██
 * ████████████████████████████████████████████
 */
public class Refresher extends PluginTask {

    private Main owner;

    public Refresher(Plugin owner) {
        super(owner);
        this.owner = (Main) owner;
    }

    @Override
    public void onRun(int i) {
        ConfigSection data = owner.format.getSection("format");
        ConfigSection signs = owner.signs.getRootSection();

        //owner.getLogger().alert(signs.toString());
        signs.entrySet().forEach(entry ->{
            List<Map> cf = signs.getMapList(entry.getKey());
            for(Map joinable : cf){
                double x = Double.parseDouble(joinable.get("x").toString());
                double y = Double.parseDouble(joinable.get("y").toString());
                double z = Double.parseDouble(joinable.get("z").toString());
                String level_name = (String) joinable.get("level-name");
                Level level = this.owner.getServer().getLevelByName(level_name);
                if(level != null){
                    BlockEntitySign blockEntity = (BlockEntitySign) this.owner.getServer().getLevelByName(level_name).getBlockEntity(new Vector3(x,y,z));

                    if(blockEntity != null){
                        List<String> text_lines = new ArrayList<>();

                        data.entrySet().forEach(row_entry ->{
                            String formattedText = this.owner.formatedRow(row_entry.getValue().toString(),level);
                            text_lines.add(formattedText);
                        });
                        blockEntity.setText(text_lines.get(0),text_lines.get(1),text_lines.get(2),text_lines.get(3));
                    }
                }
            }
        });
    }
}
