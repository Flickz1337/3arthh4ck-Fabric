package me.earth.earthhack.impl.modules.combat.autocrystal;

import me.earth.earthhack.api.util.interfaces.Globals;
import me.earth.earthhack.impl.core.ducks.entity.IEntity;
import me.earth.earthhack.impl.core.ducks.network.IEntitySpawnS2CPacket;
import me.earth.earthhack.impl.event.events.network.PacketEvent;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.modules.combat.autocrystal.modes.SwingTime;
import me.earth.earthhack.impl.modules.combat.autocrystal.util.WeaknessSwitch;
import me.earth.earthhack.impl.util.minecraft.Swing;
import me.earth.earthhack.impl.util.thread.Locks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Hand;

/**
 * Manages AutoCrystal - Instant.
 * For usage, see {@link ListenerSpawnObject}.
 */
public class HelperInstantAttack implements Globals
{
    public static void attack(AutoCrystal module,
                              EntitySpawnS2CPacket packet,
                              PacketEvent.Receive<?> event,
                              EndCrystalEntity entityIn,
                              boolean slow)
    {
        attack(module, packet, event, entityIn, slow, true);
    }

    public static void attack(AutoCrystal module,
                              EntitySpawnS2CPacket packet,
                              PacketEvent.Receive<?> event,
                              EndCrystalEntity entityIn,
                              boolean slow,
                              boolean allowAntiWeakness)
    {
        ((IEntitySpawnS2CPacket) event.getPacket()).setAttacked(true);
        PlayerInteractEntityC2SPacket p = PlayerInteractEntityC2SPacket.attack(entityIn, mc.player.isSneaking());
        WeaknessSwitch w;
        if (allowAntiWeakness)
        {
            w = HelperRotation.antiWeakness(module);
            if (w.needsSwitch())
            {
                if (w.getSlot() == -1 || !module.instantAntiWeak.getValue())
                {
                    return;
                }
            }
        }
        else
        {
            w = WeaknessSwitch.NONE;
        }

        int lastSlot = mc.player.getInventory().selectedSlot;
        Runnable runnable = () ->
        {
            if (w.getSlot() != -1)
            {
                module.antiWeaknessBypass.getValue().switchTo(w.getSlot());
            }

            if (module.breakSwing.getValue() == SwingTime.Pre)
            {
                Swing.Packet.swing(Hand.MAIN_HAND);
            }

            mc.player.networkHandler.sendPacket(p);

            if (module.breakSwing.getValue() == SwingTime.Post)
            {
                Swing.Packet.swing(Hand.MAIN_HAND);
            }

            if (w.getSlot() != -1)
            {
                module.antiWeaknessBypass.getValue().switchBack(
                    lastSlot, w.getSlot());
            }
        };

        if (w.getSlot() != -1)
        {
            Locks.acquire(Locks.PLACE_SWITCH_LOCK, runnable);
        }
        else
        {
            runnable.run();
        }

        module.breakTimer.reset(slow ? module.slowBreakDelay.getValue()
                                     : module.breakDelay.getValue());

        event.addPostEvent(() ->
        {
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (entity instanceof EndCrystalEntity)
            {
                module.setCrystal(entity);
            }
        });

        if (module.simulateExplosion.getValue())
        {
            HelperUtil.simulateExplosion(
                module, packet.getX(), packet.getY(), packet.getZ());
        }

        if (module.pseudoSetDead.getValue())
        {
            event.addPostEvent(() ->
            {
                Entity entity = mc.world.getEntityById(packet.getEntityId());
                if (entity != null)
                {
                    ((IEntity) entity).earthhack$setPseudoDead(true);
                }
            });

            return;
        }

        if (module.instantSetDead.getValue())
        {
            event.setCancelled(true);
            mc.execute(() ->
            {
                Entity entity = mc.world.getEntityById(packet.getEntityId());
                if (entity instanceof EndCrystalEntity crystal)
                {
                    module.crystalRender.onSpawn(crystal);
                }

                if (!event.isCancelled())
                {
                    return;
                }

                // ((IClientWorld) mc.world).earthhack$getEntityManager().updateEntityPosition(entityIn,
                //                                    packet.getX(),
                // TODO: Do this stuff at some point  packet.getY(),
                //                                    packet.getZ());
                Managers.SET_DEAD.setDead(entityIn);
            });
        }
    }

}
