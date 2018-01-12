package net.climaxmc.autokiller.checks;

import net.climaxmc.autokiller.AutoKiller;
import net.climaxmc.autokiller.packets.PacketBlockDigEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class ZeroDelayCheck extends Check implements Listener {

    private AutoKiller plugin;
    public ZeroDelayCheck(AutoKiller plugin) {
        super(plugin, "Zero-Delay", true, 14, 30);

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
        }.runTaskTimer(plugin, 0L, 10L);
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
            if (getVL(uuid) >= plugin.config.getVLAlert(this)) {
                if (getLastVL(uuid) <= getVL(uuid)) {
                    plugin.logCheat(uuid, getName(), getVL(uuid));
                }
            }
        }
    }

    /**
     * Zero Delay Check
     */

    private HashMap<Player, Long> lastBlockTime = new HashMap<>();
    private HashMap<Player, Location> lastBlockLocation = new HashMap<>();

    @EventHandler
    public void blockCheck(PacketBlockDigEvent event) {
        Player player = event.getPlayer();

        if (lastBlockTime.containsKey(player) && lastBlockLocation.containsKey(player)) {
            if (lastBlockLocation.get(player).equals(event.getBlockLocation())) {
                if (Math.abs(System.currentTimeMillis() - lastBlockTime.get(player)) == 0) {
                    increaseVL(player.getUniqueId(), 1);
                }
            }
        }
        lastBlockTime.put(player, System.currentTimeMillis());
        lastBlockLocation.put(player, event.getBlockLocation());
    }
}
