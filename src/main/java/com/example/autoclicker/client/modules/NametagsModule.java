package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.render.Projection;
import com.example.autoclicker.client.render.Targets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Nametags : tag flottant complet au-dessus des entités —
 * pseudo, barre de vie, stuff (armure + main) avec icônes, enchantements
 * détaillés (Sharpness V, Protection IV…) et effets de potion actifs.
 */
public class NametagsModule extends Module {

    private final Setting.Mode filter =
            (Setting.Mode) add(new Setting.Mode("Cibles", 1, "Mobs", "Joueurs", "Tous"));
    private final Setting.Bool showItems =
            (Setting.Bool) add(new Setting.Bool("Stuff + main", true));
    private final Setting.Bool showEnchants =
            (Setting.Bool) add(new Setting.Bool("Enchants détaillés", true));
    private final Setting.Bool showEffects =
            (Setting.Bool) add(new Setting.Bool("Effets", true));
    private final Setting.Number scale =
            (Setting.Number) add(new Setting.Number("Taille", 1.0, 0.5, 2, 0.1, false));

    // barre de vie lissée par entité
    private final java.util.Map<Integer, Float> hpAnim = new java.util.HashMap<Integer, Float>();
    private long nLastFrame = System.currentTimeMillis();
    private float nFrameDt = 0f;

    public NametagsModule() {
        super("Nametags", Category.VISUAL);
        showInArrayList = true;
    }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        float pt = Projection.partialTicks();
        long now = System.currentTimeMillis();
        nFrameDt = Math.min(0.1f, (now - nLastFrame) / 1000f);
        nLastFrame = now;

        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (!Targets.matches(e, filter.current())) continue;
            if (e.isInvisible()) continue;

            double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * pt;
            double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * pt + e.height + 0.6;
            double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * pt;

            Projection.Screen s = Projection.project(x, y, z);
            if (s == null) continue;

            drawTag(e, s.x, s.y);
        }
    }

    private void drawTag(EntityLivingBase e, float cx, float cy) {
        FontRenderer fr = mc.fontRendererObj;
        float sc = (float) scale.value * 0.5f; // 0.5 = tags compacts par défaut

        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy, 0);
        GlStateManager.scale(sc, sc, 1f);

        // ---- contenu ----
        String name = e.getDisplayName().getUnformattedText();
        String hp = " " + (int) Math.ceil(e.getHealth());
        String dist = mc.thePlayer == null ? "" :
                ("  " + (int) mc.thePlayer.getDistanceToEntity(e) + "m");
        int nameW = fr.getStringWidth(name) + fr.getStringWidth(hp) + fr.getStringWidth(dist);

        List<ItemStack> items = showItems.value ? gear(e) : new ArrayList<ItemStack>();
        List<String> effects = showEffects.value ? effects(e) : new ArrayList<String>();
        List<String> enchLines = showEnchants.value ? enchantLines(items) : new ArrayList<String>();

        int boxW = Math.max(nameW + 8, items.size() * 18 + 4);
        for (String l : effects) boxW = Math.max(boxW, fr.getStringWidth(l) + 8);
        for (String l : enchLines) boxW = Math.max(boxW, fr.getStringWidth(l) + 8);

        int rows = 1;                                  // ligne nom + vie
        int itemY = -2;                                // calculé plus bas
        int contentTop = -(11 + (items.isEmpty() ? 0 : 18)
                + effects.size() * 9 + enchLines.size() * 9);
        int half = boxW / 2;

        int y = contentTop;
        // fond TRANSPARENT (pas de boîte) — on garde juste de fins liserés d'accent
        Gui.drawRect(-half - 2, y - 2, half + 2, y - 1, Theme.ACCENT);
        Gui.drawRect(-half - 2, 0, half + 2, 1, Theme.ACCENT2);

        // nom + vie
        fr.drawStringWithShadow(name, -half + 2, y, Theme.TEXT);
        // ratio de vie lissé par entité
        float target = Math.max(0f, Math.min(1f, e.getHealth() / e.getMaxHealth()));
        Float cur = hpAnim.get(e.getEntityId());
        float ratio = (cur == null) ? target : cur;
        ratio += (target - ratio) * Math.min(1f, nFrameDt * 8f);
        hpAnim.put(e.getEntityId(), ratio);
        int hpColor = healthColor(ratio);
        fr.drawStringWithShadow(hp, -half + 2 + fr.getStringWidth(name), y, hpColor);
        if (!dist.isEmpty())
            fr.drawStringWithShadow(dist, half - 2 - fr.getStringWidth(dist), y, Theme.ACCENT2);
        // barre de vie fine animée sous le nom
        Gui.drawRect(-half + 2, y + 9, half - 2, y + 10, 0xFF2A2A33);
        Gui.drawRect(-half + 2, y + 9, -half + 2 + (int) ((boxW - 4) * ratio), y + 10, hpColor);
        y += 11;

        // items (armure + main) avec niveau d'enchant en badge
        if (!items.isEmpty()) {
            RenderItem ri = mc.getRenderItem();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            int ix = -items.size() * 9; // centré (18px/2)
            for (ItemStack st : items) {
                ri.renderItemAndEffectIntoGUI(st, ix, y);
                ri.renderItemOverlayIntoGUI(fr, st, ix, y, null);
                ix += 18;
            }
            RenderHelper.disableStandardItemLighting();
            y += 18;
        }

        // enchants détaillés
        for (String l : enchLines) {
            fr.drawStringWithShadow(l, -half + 2, y, Theme.ACCENT2);
            y += 9;
        }
        // effets de potion
        for (String l : effects) {
            fr.drawStringWithShadow(l, -half + 2, y, 0xFFB0B0C0);
            y += 9;
        }

        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /** Armure (casque->bottes) + item en main, dans l'ordre d'affichage. */
    private List<ItemStack> gear(EntityLivingBase e) {
        List<ItemStack> out = new ArrayList<ItemStack>();
        for (int slot = 4; slot >= 1; slot--) {        // 4=casque .. 1=bottes
            ItemStack st = e.getEquipmentInSlot(slot);
            if (st != null) out.add(st);
        }
        ItemStack held = e.getHeldItem();
        if (held != null) out.add(held);
        return out;
    }

    /** "Épée: Sharpness V, Fire Aspect II" par item enchanté. */
    private List<String> enchantLines(List<ItemStack> items) {
        List<String> out = new ArrayList<String>();
        for (ItemStack st : items) {
            if (st == null || !st.isItemEnchanted()) continue;
            NBTTagList list = st.getEnchantmentTagList();
            if (list == null) continue;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                int id = tag.getShort("id");
                int lvl = tag.getShort("lvl");
                Enchantment en = Enchantment.getEnchantmentById(id);
                if (en == null) continue;
                if (sb.length() > 0) sb.append(", ");
                sb.append(en.getTranslatedName(lvl)); // ex. "Sharpness V"
            }
            if (sb.length() > 0) {
                String item = st.getDisplayName();
                out.add(trim(item, 14) + ": " + sb);
            }
        }
        return out;
    }

    /** Effets de potion actifs : "Speed II 0:42". */
    private List<String> effects(EntityLivingBase e) {
        List<String> out = new ArrayList<String>();
        for (Object o : e.getActivePotionEffects()) {
            PotionEffect pe = (PotionEffect) o;
            Potion p = Potion.potionTypes[pe.getPotionID()];
            if (p == null) continue;
            String name = StatCollector.translateToLocal(p.getName());
            int amp = pe.getAmplifier() + 1;
            String dur = Potion.getDurationString(pe);
            out.add(name + " " + amp + "  " + dur);
        }
        return out;
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static int healthColor(float ratio) {
        return Theme.lerp(0xFFE74C3C, 0xFF2ECC71, ratio);
    }
}
