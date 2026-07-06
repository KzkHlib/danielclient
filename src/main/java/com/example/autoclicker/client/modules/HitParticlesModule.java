package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/** HitParticles : petites particules cosmétiques sur la cible quand tu frappes. */
public class HitParticlesModule extends Module {

    public HitParticlesModule() {
        super("HitParticles", Category.COMBAT);
    }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        if (mc.theWorld == null || e.target == null) return;
        Entity t = e.target;
        for (int i = 0; i < 6; i++) {
            double sx = (Math.random() - 0.5) * 0.5;
            double sy = Math.random() * 0.4;
            double sz = (Math.random() - 0.5) * 0.5;
            mc.theWorld.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                    t.posX, t.posY + t.height / 2.0, t.posZ, sx, sy, sz);
        }
    }
}
