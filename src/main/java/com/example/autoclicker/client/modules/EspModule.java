package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.render.RenderUtil;
import com.example.autoclicker.client.render.Targets;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

/** ESP : surligne les entités avec une boîte 2D (billboard) ou 3D (filaire). */
public class EspModule extends Module {

    private final Setting.Mode shape =
            (Setting.Mode) add(new Setting.Mode("Forme", 0, "Boite 2D", "Boite 3D"));
    private final Setting.Mode filter =
            (Setting.Mode) add(new Setting.Mode("Cibles", 0, "Mobs", "Joueurs", "Tous"));
    private final Setting.Mode color =
            (Setting.Mode) add(new Setting.Mode("Couleur", 0, "Par type", "Accent"));
    private final Setting.Number thickness =
            (Setting.Number) add(new Setting.Number("Epaisseur", 1.5, 0.5, 4, 0.5, false));

    public EspModule() {
        super("ESP", Category.VISUAL);
    }

    @Override
    public void onRenderWorld(float pt) {
        if (mc.theWorld == null) return;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (!Targets.matches(e, filter.current())) continue;

            int c = color.is("Accent") ? com.example.autoclicker.client.Theme.ACCENT
                                       : Targets.color(e);
            AxisAlignedBB box = RenderUtil.interpolatedBox(e, pt);

            if (shape.is("Boite 3D")) {
                RenderUtil.box3D(grow(box, 0.05), c, (float) thickness.value);
            } else {
                int fill = (c & 0x00FFFFFF) | 0x33000000; // remplissage léger
                float w = (float) (box.maxX - box.minX);
                float h = (float) (box.maxY - box.minY);
                RenderUtil.box2D((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0,
                        w * 1.05f, h * 1.05f, c, fill, (float) thickness.value);
            }
        }
    }

    private static AxisAlignedBB grow(AxisAlignedBB b, double m) {
        return new AxisAlignedBB(b.minX - m, b.minY - m, b.minZ - m,
                                 b.maxX + m, b.maxY + m, b.maxZ + m);
    }
}
