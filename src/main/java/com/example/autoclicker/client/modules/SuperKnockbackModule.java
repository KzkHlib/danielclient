package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class SuperKnockbackModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "Reverse", "Vertical", "Stun"));
    private final Setting.Number multiplier = (Setting.Number) add(new Setting.Number("Multiplicateur", 1.5, 0.5, 5.0, 0.5, false));
    private final Setting.Number chance = (Setting.Number) add(new Setting.Number("Chance %", 100, 10, 100, 5, true));

    public SuperKnockbackModule() { super("SuperKnockback", Category.COMBAT); }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        if (mc.thePlayer == null) return;
        if (Math.random() > chance.value / 100.0) return;

        double mult = multiplier.value;
        String m = mode.current();

        if (m.equals("Normal")) {
            mc.thePlayer.motionX *= mult;
            mc.thePlayer.motionZ *= mult;
        } else if (m.equals("Reverse")) {
            mc.thePlayer.motionX *= -mult * 0.5;
            mc.thePlayer.motionZ *= -mult * 0.5;
        } else if (m.equals("Vertical")) {
            mc.thePlayer.motionY = 0.55 * mult;
        } else if (m.equals("Stun")) {
            mc.thePlayer.motionX *= 0.1;
            mc.thePlayer.motionZ *= 0.1;
        }
    }
}
