package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.render.RenderUtil;
import com.example.autoclicker.client.render.Targets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

/** Tracers : trace une ligne du viseur vers chaque entité ciblée. */
public class TracersModule extends Module {

    private final Setting.Mode filter =
            (Setting.Mode) add(new Setting.Mode("Cibles", 0, "Mobs", "Joueurs", "Tous"));
    private final Setting.Mode color =
            (Setting.Mode) add(new Setting.Mode("Couleur", 0, "Par type", "Accent"));
    private final Setting.Number thickness =
            (Setting.Number) add(new Setting.Number("Epaisseur", 1.0, 0.5, 3, 0.5, false));

    public TracersModule() {
        super("Tracers", Category.VISUAL);
    }

    @Override
    public void onRenderWorld(float pt) {
        if (mc.theWorld == null) return;
        Entity view = mc.getRenderViewEntity();
        if (view == null) return;

        // point de départ : juste devant la caméra (émane du viseur)
        Vec3 eye = view.getPositionEyes(pt);
        Vec3 look = view.getLook(pt);
        double sx = eye.xCoord + look.xCoord;
        double sy = eye.yCoord + look.yCoord;
        double sz = eye.zCoord + look.zCoord;

        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (!Targets.matches(e, filter.current())) continue;

            int c = color.is("Accent") ? Theme.ACCENT : Targets.color(e);
            double ex = e.lastTickPosX + (e.posX - e.lastTickPosX) * pt;
            double ey = e.lastTickPosY + (e.posY - e.lastTickPosY) * pt + e.height / 2.0;
            double ez = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * pt;

            RenderUtil.line(sx, sy, sz, ex, ey, ez, c, (float) thickness.value);
        }
    }
}
