package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class TriggerBotModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "Delay", "Smart", "NCP", "AAC", "Grim"));
    private final Setting.Number delay = (Setting.Number) add(new Setting.Number("Delai ms", 50, 0, 500, 25, true));
    private final Setting.Bool players = (Setting.Bool) add(new Setting.Bool("Joueurs", true));
    private final Setting.Bool mobs = (Setting.Bool) add(new Setting.Bool("Mobs", false));
    private long lastAttack = 0;

    public TriggerBotModule() { super("TriggerBot", Category.COMBAT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.objectMouseOver == null) return;
        if (mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return;

        Entity target = mc.objectMouseOver.entityHit;
        if (!(target instanceof EntityLivingBase)) return;
        if (target == mc.thePlayer) return;
        if (target.isDead) return;

        if (target instanceof net.minecraft.entity.player.EntityPlayer && !players.value) return;
        if (!(target instanceof net.minecraft.entity.player.EntityPlayer) && !mobs.value) return;

        String m = mode.current();

        if (m.equals("Smart") && mc.gameSettings.keyBindAttack.isKeyDown()) return;

        long now = System.currentTimeMillis();
        long d = m.equals("Normal") ? 0 : (long) delay.value;

        if (m.equals("NCP")) d = (long) (40 + Math.random() * 60);
        else if (m.equals("AAC")) d = (long) (80 + Math.random() * 40);
        else if (m.equals("Grim")) d = (long) (100 + Math.random() * 100);

        if (now - lastAttack < d) return;
        lastAttack = now;

        mc.playerController.attackEntity(mc.thePlayer, target);
        mc.thePlayer.swingItem();
    }
}
