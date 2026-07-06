package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

/**
 * AutoTool : passe automatiquement au meilleur outil de la hotbar pour casser
 * le bloc visé, pendant que tu mines (clic gauche maintenu).
 */
public class AutoToolModule extends Module {

    public AutoToolModule() {
        super("AutoTool", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (!Mouse.isButtonDown(0)) return; // uniquement en minant

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        BlockPos pos = mop.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();

        int current = mc.thePlayer.inventory.currentItem;
        int best = current;
        float bestSpeed = speed(mc.thePlayer.inventory.getStackInSlot(current), block);

        for (int i = 0; i < 9; i++) {
            float sp = speed(mc.thePlayer.inventory.getStackInSlot(i), block);
            if (sp > bestSpeed) {
                bestSpeed = sp;
                best = i;
            }
        }
        if (best != current) {
            mc.thePlayer.inventory.currentItem = best; // le client envoie le changement au serveur
        }
    }

    private float speed(ItemStack stack, Block block) {
        if (stack == null) return 1.0f;
        try {
            return stack.getStrVsBlock(block);
        } catch (Exception e) {
            return 1.0f;
        }
    }
}
