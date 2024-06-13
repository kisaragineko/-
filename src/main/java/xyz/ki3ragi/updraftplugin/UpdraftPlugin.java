package xyz.ki3ragi.updraftplugin;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import java.util.List;
public class UpdraftPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private double updraftSpeed = 0.5; // デフォルトの上昇速度

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("setupdraftspeed").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("setupdraftspeed")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage("使用法: /setupdraftspeed <速度>");
                return true;
            }
            try {
                double speed = Double.parseDouble(args[0]);
                if (speed <= 0) {
                    sender.sendMessage("速度は正の数である必要があります。");
                    return true;
                }
                this.updraftSpeed = speed;
                sender.sendMessage("上昇速度が " + speed + " に設定されました。");
            } catch (NumberFormatException e) {
                sender.sendMessage("速度は数値である必要があります。");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() == Material.WHITE_WOOL) {
            createUpdraft(block);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getBlock().getMetadata("Updraft").size() > 0) {
            player.setVelocity(new Vector(0, updraftSpeed, 0));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeUpdraft(player);
    }

    private void createUpdraft(Block block) {
        block.getWorld().createExplosion(block.getLocation(), 0, false);
        block.setMetadata("Updraft", new FixedMetadataValue(this, true));
    }

    private void removeUpdraft(Player player) {
        Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
        if (block.getType() == Material.AIR) {
            List<MetadataValue> metadataList = block.getMetadata("Updraft");
            for (MetadataValue value : metadataList) {
                if (value.getOwningPlugin().equals(this)) {
                    block.removeMetadata("Updraft", this);
                    return;
                }
            }
        }
    }
}
