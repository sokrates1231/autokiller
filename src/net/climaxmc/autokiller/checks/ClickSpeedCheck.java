package net.climaxmc.autokiller.checks;

import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class ClickSpeedCheck extends Check implements Listener {

    private AutoKiller plugin;
    public ClickSpeedCheck(AutoKiller plugin) {
        super(plugin, "Click-Speed", true, 8, 30);

        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                checkForAlert();
            }
        }.runTaskTimer(plugin, 0L, 10L);
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForBan();
            }
        }.runTaskTimer(plugin, 0L, 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                decreaseAllVL(1);
            }
        }.runTaskTimer(plugin, 0L, 20L * 3);
    }

    private void checkForBan() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!isEnabled()) {
                resetVL(uuid);
                return;
            }
            if (getVL(uuid) >= plugin.config.getVLBan(this)) {
                plugin.autoBanPlayer(uuid, "AutoBan", getName());
                vls.put(uuid, 0);
            }
        }
    }

    private void checkForAlert() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!isEnabled()) {
                resetVL(uuid);
                return;
            }
            /*if (getVL(uuid) >= plugin.config.getVLAlert(this)) {
                if (getLastVL(uuid) <= getVL(uuid)) {
                    plugin.logCheat(uuid, getName(), getVL(uuid));
                }
            }*/
        }
    }

    /**
     * Click Speed Check
     */

    private HashMap<Player, Integer> clicks = new HashMap<>();

    @EventHandler
    public void speedCheck(PlayerInteractEvent event) {
        if (!event.getPlayer().getType().equals(EntityType.PLAYER)) {
            return;
        }
        Player player = event.getPlayer();

        if (!event.getAction().equals(Action.LEFT_CLICK_AIR)) {
            return;
        }

        if (player.getItemInHand() != null && player.getItemInHand().getType().equals(Material.FISHING_ROD)) {
            return;
        }

        if (!clicks.containsKey(player)) {
            clicks.put(player, 0);
        }
        if (clicks.get(player) == 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if (clicks.get(player) >= plugin.config.getMaxSpeed()) {
                        if (!isEnabled()) {
                            resetVL(player.getUniqueId());
                            return;
                        }
                        plugin.logCheat(player.getUniqueId(), getName(), clicks.get(player));
                        increaseVL(player.getUniqueId(), 3);
                    }
                    clicks.put(player, 0);
                }
            }, 20L);
        }
        clicks.put(player, clicks.get(player) + 1);
    }
}
