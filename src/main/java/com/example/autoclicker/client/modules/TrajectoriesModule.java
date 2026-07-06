package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class TrajectoriesModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Ligne", "Points", "Both"));
    private final Setting.Bool rainbow = (Setting.Bool) add(new Setting.Bool("Arc-en-ciel", false));

    public TrajectoriesModule() { super("Trajectories", Category.RENDER); }

    @Override
    public void onRenderWorld(float partialTicks) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isHoldingBow() && !isHoldingThrowable()) return;

        double posX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double posY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks + mc.thePlayer.getEyeHeight();
        double posZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;

        double motionX = -MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);
        double motionZ = MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);
        double motionY = -MathHelper.sin(pitch / 180.0F * (float) Math.PI);

        float velocity = 1.5F;
        float inaccuracy = 0.0F;

        ItemStack held = mc.thePlayer.getHeldItem();
        if (held != null && held.getItem() instanceof ItemBow) {
            float useDuration = 72000 - mc.thePlayer.getItemInUseCount();
            float charge = useDuration / 20.0F;
            charge = (charge * charge + charge * 2.0F) / 3.0F;
            if (charge < 0.1F) charge = 0.1F;
            if (charge > 1.0F) charge = 1.0F;
            velocity = charge * 3.0F;
            inaccuracy = 0.0F;
        }

        double vx = motionX * velocity;
        double vy = motionY * velocity;
        double vz = motionZ * velocity;

        double gravity = 0.05;
        double drag = 0.99;

        GlStateManager.pushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(2.0F);

        int ticks = 0;
        double prevX = posX, prevY = posY, prevZ = posZ;
        List<double[]> points = new ArrayList<double[]>();

        while (ticks++ < 200 && posY > 0) {
            posX += vx;
            posY += vy;
            posZ += vz;

            vx *= drag;
            vy *= drag;
            vy -= gravity;
            vz *= drag;

            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                    AxisAlignedBB.fromBounds(posX - 0.1, posY - 0.1, posZ - 0.1,
                                             posX + 0.1, posY + 0.1, posZ + 0.1)).size() > 0) {
                break;
            }
            points.add(new double[]{posX, posY, posZ});
        }

        boolean doLine = mode.is("Ligne") || mode.is("Both");
        boolean doPoints = mode.is("Points") || mode.is("Both");

        for (int i = 0; i < points.size(); i++) {
            double[] p = points.get(i);
            float hue = rainbow.value ? (i / (float) points.size()) : 0.0F;
            int color = rainbow.value ? java.awt.Color.HSBtoRGB(hue, 0.8F, 1.0F) : 0xFFFFFFFF;

            if (doLine && i > 0) {
                double[] pp = points.get(i - 1);
                GL11.glColor4f(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, 0.7F);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex3d(pp[0] - mc.getRenderManager().viewerPosX, pp[1] - mc.getRenderManager().viewerPosY, pp[2] - mc.getRenderManager().viewerPosZ);
                GL11.glVertex3d(p[0] - mc.getRenderManager().viewerPosX, p[1] - mc.getRenderManager().viewerPosY, p[2] - mc.getRenderManager().viewerPosZ);
                GL11.glEnd();
            }

            if (doPoints) {
                GL11.glColor4f(((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, 0.9F);
                GL11.glPointSize(3.0F);
                GL11.glBegin(GL11.GL_POINTS);
                GL11.glVertex3d(p[0] - mc.getRenderManager().viewerPosX, p[1] - mc.getRenderManager().viewerPosY, p[2] - mc.getRenderManager().viewerPosZ);
                GL11.glEnd();
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.popMatrix();
    }

    private boolean isHoldingBow() {
        ItemStack held = mc.thePlayer.getHeldItem();
        return held != null && held.getItem() instanceof ItemBow;
    }

    private boolean isHoldingThrowable() {
        ItemStack held = mc.thePlayer.getHeldItem();
        if (held == null) return false;
        Item item = held.getItem();
        return item == Items.egg || item == Items.snowball || item == Items.experience_bottle
                || item == Items.ender_pearl || item == Items.potionitem;
    }
}
