package net.climaxmc.autokiller.checks;

import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;

public class Check implements Listener {

    private String name;
    private boolean enabled = true;
    private boolean bannable;
    private AutoKiller plugin;
    public int defaultAlertVL;
    public int defaultBanVL;

    public HashMap<UUID, Long> disableTime = new HashMap<>();
    public HashMap<UUID, Integer> vls = new HashMap<>();
    public HashMap<UUID, Integer> lastVLs = new HashMap<>();

    public Check(AutoKiller plugin, String name, boolean bannable, int defaultAlertVL, int defaultBanVL) {
        this.plugin = plugin;
        this.name = name;
        this.bannable = bannable;
        this.defaultAlertVL = defaultAlertVL;
        this.defaultBanVL = defaultBanVL;
    }

    public int getDefaultAlertVL() {
        return defaultAlertVL;
    }
    public int getDefaultBanVL() {
        return defaultBanVL;
    }

    public void increaseVL(UUID uuid, int amount) {
        if (!enabled) {
            return;
        }
        if (!vls.containsKey(uuid)) {
            vls.put(uuid, amount);
        } else {
            lastVLs.put(uuid, vls.get(uuid));
            vls.put(uuid, vls.get(uuid) + amount);
        }
    }
    public int getVL(UUID uuid) {
        if (!vls.containsKey(uuid)) {
            vls.put(uuid, 0);
            return vls.get(uuid);
        } else {
            return vls.get(uuid);
        }
    }
    public int getLastVL(UUID uuid) {
        if (!lastVLs.containsKey(uuid)) {
            lastVLs.put(uuid, 0);
            return lastVLs.get(uuid);
        } else {
            return lastVLs.get(uuid);
        }
    }
    public void decreaseVL(UUID uuid, int amount) {
        if (!vls.containsKey(uuid)) {
            vls.put(uuid, amount);
        } else if (vls.get(uuid) - amount >= 0) {
            lastVLs.put(uuid, vls.get(uuid));
            vls.put(uuid, vls.get(uuid) - amount);
        }
    }
    public void decreaseAllVL(int amount) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!vls.containsKey(uuid)) {
                vls.put(uuid, amount);
            } else if (vls.get(uuid) - amount >= 0) {
                lastVLs.put(uuid, vls.get(uuid));
                vls.put(uuid, vls.get(uuid) - amount);
            }
        }
    }

    public void resetVL(UUID uuid) {
        lastVLs.put(uuid, vls.get(uuid));
        vls.put(uuid, 0);
    }

    public String getName() {
        return name;
    }
    public boolean isBannable() {
        return bannable;
    }

    public boolean isDisabled(Player player) {
        if (!disableTime.containsKey(player.getUniqueId())) {
            disableTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
        return disableTime.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public boolean isEnabled() {
        return plugin.config.getEnabled(this);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
            return;
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            return;
        }
        Player player = (Player) event.getEntity();
        disableTime.put(player.getUniqueId(), System.currentTimeMillis() + 1500);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        disableTime.put(player.getUniqueId(), System.currentTimeMillis() + 1500);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        vls.remove(player.getUniqueId());
    }
}
