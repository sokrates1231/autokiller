package net.climaxmc.autokiller.packets;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketUseEntityEvent extends Event {

    private EnumWrappers.EntityUseAction action;
    private Player attacker;
    private Entity attacked;
    private static final HandlerList handlers = new HandlerList();

    public PacketUseEntityEvent(EnumWrappers.EntityUseAction Action, Player Attacker, Entity Attacked)
    {
        this.action = Action;
        this.attacker = Attacker;
        this.attacked = Attacked;
    }

    public EnumWrappers.EntityUseAction getAction()
    {
        return this.action;
    }

    public Player getAttacker()
    {
        return this.attacker;
    }

    public Entity getAttacked()
    {
        return this.attacked;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
