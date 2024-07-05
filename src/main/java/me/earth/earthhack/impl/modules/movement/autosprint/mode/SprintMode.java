package me.earth.earthhack.impl.modules.movement.autosprint.mode;

import me.earth.earthhack.api.util.interfaces.Globals;
import net.minecraft.client.option.KeyBinding;

public enum SprintMode implements Globals
{
    Rage
            {
                @Override
                public void sprint()
                {
                    mc.player.setSprinting(true);
                }
            },
    Legit
            {
                @Override
                public void sprint()
                {
                    if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
                    mc.player.setSprinting(true);
                }
            };
    public abstract void sprint();

}
