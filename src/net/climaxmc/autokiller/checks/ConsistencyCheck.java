package net.climaxmc.autokiller.checks;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.climaxmc.autokiller.AutoKiller;
import net.climaxmc.autokiller.packets.PacketUseEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ConsistencyCheck extends Check implements Listener {

    private AutoKiller plugin;
    public ConsistencyCheck(AutoKiller plugin) {
        super(plugin, "Consistent-Clicks", true, 10, 30);

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
        }.runTaskTimer(plugin, 0L, 20L);
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
     * Consistency 1 Check
     */

    private HashMap<Player, Long> lastTime = new HashMap<>();
    private HashMap<Player, ArrayList<Long>> lastDifferences = new HashMap<>();
    private HashMap<Player, Long> lastTimeDifference = new HashMap<>();
    private HashMap<Player, Float> lastTargetYaw = new HashMap<>();

    @EventHandler
    public void onClick(PacketUseEntityEvent event) {
        if (!event.getAttacked().getType().equals(EntityType.PLAYER)) {
            return;
        }
        Player player = event.getAttacker();
        Player target = (Player) event.getAttacked();

        if (event.getAction() == EnumWrappers.EntityUseAction.ATTACK) {

            if (lastTargetYaw.containsKey(target)) {
                if (lastTargetYaw.get(target) == target.getLocation().getYaw()) {
                    return;
                }
            }
            lastTargetYaw.put(target, target.getLocation().getYaw());

            if (!lastTime.containsKey(player)) {
                lastTime.put(player, System.currentTimeMillis());
            }
            if (!lastTimeDifference.containsKey(player)) {
                lastTimeDifference.put(player, 200L);
            }
            long currentTimeDifference = System.currentTimeMillis() - lastTime.get(player);

            if (!lastDifferences.containsKey(player)) {
                lastDifferences.put(player, new ArrayList<>());
            }
            if (lastDifferences.get(player).size() < plugin.config.getSensitivity()) {
                double differenceOfDifferences = Math.abs(currentTimeDifference - lastTimeDifference.get(player));
                if (differenceOfDifferences > 200) {
                    lastDifferences.get(player).add(70L);
                } else {
                    lastDifferences.get(player).add(currentTimeDifference - lastTimeDifference.get(player));
                }
            } else {
                lastDifferences.get(player).remove(0);
                double differenceOfDifferences = Math.abs(currentTimeDifference - lastTimeDifference.get(player));
                if (differenceOfDifferences > 200) {
                    lastDifferences.get(player).add(70L);
                } else {
                    lastDifferences.get(player).add(currentTimeDifference - lastTimeDifference.get(player));
                }
            }

            lastTimeDifference.put(player, currentTimeDifference);
            lastTime.put(player, System.currentTimeMillis());

            for (Long differences : lastDifferences.get(player)) {
                if (Math.abs(differences) > 70) {
                    return;
                }
            }

            if (clicks.containsKey(player)) {
                if (clicks.get(player) > 6) {
                    increaseVL(player.getUniqueId(), 1);
                }
            }
        }
    }

    /**
     * Click Speed Check (Is used in consistency check)
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
                    clicks.put(player, 0);
                }
            }, 20L);
        }
        clicks.put(player, clicks.get(player) + 1);
    }
}
