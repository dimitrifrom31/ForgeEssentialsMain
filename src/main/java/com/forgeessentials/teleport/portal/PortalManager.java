package com.forgeessentials.teleport.portal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

import com.forgeessentials.commons.selections.WorldPoint;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.data.v2.DataManager;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleServerStoppedEvent;
import com.forgeessentials.util.events.PlayerMoveEvent;
import com.forgeessentials.util.events.ServerEventHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 */
public class PortalManager extends ServerEventHandler {

    private static PortalManager instance;

    protected Map<String, Portal> portals = new HashMap<>();

    public PortalManager()
    {
        super();
        instance = this;
    }

    public static PortalManager getInstance()
    {
        return instance;
    }

    @Override
    @SubscribeEvent
    public void serverStopped(FEModuleServerStoppedEvent e)
    {
        super.serverStopped(e);
        save();
    }

    public void load()
    {
        portals = DataManager.getInstance().loadAll(Portal.class);
        for (Portal portal : portals.values())
            buildPortalFrame(portal);
    }

    public void save()
    {
        for (Entry<String, Portal> portal : portals.entrySet())
            DataManager.getInstance().save(portal.getValue(), portal.getKey());
    }

    @SubscribeEvent
    public void playerMove(PlayerMoveEvent e)
    {
        WorldPoint after = e.after.toWorldPoint();
        WorldPoint before = e.before.toWorldPoint();
        for (Portal portal : portals.values())
        {
            if (!portal.hasFrame() && portal.getPortalArea().contains(after) && !portal.getPortalArea().contains(before))
            {
                TeleportHelper.doTeleport((EntityPlayerMP) e.entityPlayer, portal.target.toWarpPoint(e.entityPlayer.rotationPitch,
                        e.entityPlayer.rotationYaw));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void breakEvent(BreakEvent e)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            return;
        WorldPoint point = new WorldPoint(e.getPlayer().dimension, e.x, e.y, e.z);
        Portal portal = getPortalAt(point);
        if (portal != null && portal.hasFrame())
            e.setCanceled(true);
    }

    public Portal getPortalAt(WorldPoint point)
    {
        for (Portal portal : portals.values())
            if (portal.getPortalArea().contains(point))
                return portal;
        return null;
    }

    // @SubscribeEvent
    // public void serverTick(ServerTickEvent e)
    // {
    // if (e.phase == Phase.END)
    // return;
    // for (Portal portal : portals.values())
    // {
    // if (!portal.getPortalArea().isValid())
    // continue;
    //
    // // WorldClient world = Minecraft.getMinecraft().theWorld;
    // // if (world.provider.dimensionId ==
    // portal.getPortalArea().getDimension())
    // // {
    // // if (new Random().nextInt(100) < 100)
    // // {
    // // NamedWorldArea area = portal.getPortalArea();
    // // Point start = area.getLowPoint();
    // // Point size = area.getSize();
    // // size.x++;
    // // size.y++;
    // // size.z++;
    // // Random rnd = new Random();
    // // world.spawnParticle("flame", start.getX() + rnd.nextFloat() *
    // size.getX(), start.getY() + rnd.nextFloat() * size.getY(),
    // // start.getZ() + rnd.nextFloat() * size.getZ(), 0, 0, 0);
    // // }
    // // }
    // }
    // }

    public Portal get(String name)
    {
        return portals.get(name);
    }

    public void remove(String name)
    {
        destroyPortalFrame(portals.remove(name));
        DataManager.getInstance().delete(Portal.class, name);
    }

    public void add(String name, Portal portal)
    {
        portals.put(name, portal);
        DataManager.getInstance().save(portal, name);
        buildPortalFrame(portal);
    }

    private static void buildPortalFrame(Portal portal)
    {
        if (!portal.hasFrame())
            return;
        World world = DimensionManager.getWorld(portal.getPortalArea().getDimension());
        if (world != null)
        {
            for (int ix = portal.getPortalArea().getLowPoint().getX(); ix <= portal.getPortalArea().getHighPoint().getX(); ix++)
                for (int iy = portal.getPortalArea().getLowPoint().getY(); iy <= portal.getPortalArea().getHighPoint().getY(); iy++)
                    for (int iz = portal.getPortalArea().getLowPoint().getZ(); iz <= portal.getPortalArea().getHighPoint().getZ(); iz++)
                        if (world.getBlock(ix, iy, iz) != Blocks.portal)
                            world.setBlock(ix, iy, iz, Blocks.portal);
        }
    }

    private static void destroyPortalFrame(Portal portal)
    {
        if (!portal.hasFrame())
            return;
        World world = DimensionManager.getWorld(portal.getPortalArea().getDimension());
        if (world != null)
        {
            for (int ix = portal.getPortalArea().getLowPoint().getX(); ix <= portal.getPortalArea().getHighPoint().getX(); ix++)
                for (int iy = portal.getPortalArea().getLowPoint().getY(); iy <= portal.getPortalArea().getHighPoint().getY(); iy++)
                    for (int iz = portal.getPortalArea().getLowPoint().getZ(); iz <= portal.getPortalArea().getHighPoint().getZ(); iz++)
                        if (world.getBlock(ix, iy, iz) == Blocks.portal)
                            world.setBlock(ix, iy, iz, Blocks.air);
        }
    }

}
