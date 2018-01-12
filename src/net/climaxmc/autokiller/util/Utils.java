package net.climaxmc.autokiller.util;

import net.climaxmc.autokiller.AutoKiller;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utils {

    private AutoKiller plugin;

    public Utils(AutoKiller plugin) {
        this.plugin = plugin;
    }

    public static int getPing(Player player) {
        int ping = -1;

        //return ((CraftPlayer) player).getHandle().ping;

        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);

        try {
            Object nmsPlayer = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer").cast(player).getClass().getMethod("getHandle").invoke(player);
            ping = nmsPlayer.getClass().getField("ping").getInt(nmsPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ping;
    }
}
