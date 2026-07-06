package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.render.RenderUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

/** ChestESP : affiche uniquement les coffres normaux et coffres pieges. */
public class ChestEspModule extends Module {

    private final Setting.Bool normalChests =
            (Setting.Bool) add(new Setting.Bool("Coffres normaux", true));
    private final Setting.Bool trappedChests =
            (Setting.Bool) add(new Setting.Bool("Coffres pieges", true));
    private final Setting.Bool tracers =
            (Setting.Bool) add(new Setting.Bool("Tracer", true));
    private final Setting.Mode color =
            (Setting.Mode) add(new Setting.Mode("Couleur", 0, "Type", "Accent"));
    private final Setting.Number range =
            (Setting.Number) add(new Setting.Number("Portee", 96, 8, 256, 4, true));
    private final Setting.Number thickness =
            (Setting.Number) add(new Setting.Number("Epaisseur", 1.5, 0.5, 4, 0.5, false));

    public ChestEspModule() {
        super("ChestESP", Category.VISUAL);
    }

    @Override
    public String arrayListSuffix() {
        return ((int) range.value) + "m";
    }

    @Override
    public void onRenderWorld(float pt) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        Entity view = mc.getRenderViewEntity();
        if (view == null) return;

        Vec3 eye = view.getPositionEyes(pt);
        Vec3 look = view.getLook(pt);
        double sx = eye.xCoord + look.xCoord;
        double sy = eye.yCoord + look.yCoord;
        double sz = eye.zCoord + look.zCoord;

        double maxDistSq = range.value * range.value;
        for (Object o : mc.theWorld.loadedTileEntityList) {
            if (!(o instanceof TileEntityChest)) continue;
            TileEntityChest chest = (TileEntityChest) o;
            BlockPos pos = chest.getPos();
            if (pos == null) continue;
            if (mc.thePlayer.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxDistSq) continue;

            Block block = mc.theWorld.getBlockState(pos).getBlock();
            boolean trapped = block == Blocks.trapped_chest;
            boolean normal = block == Blocks.chest;
            if ((!normal && !trapped) || (normal && !normalChests.value) || (trapped && !trappedChests.value)) continue;

            int c = color.is("Accent") ? Theme.ACCENT : (trapped ? 0xFFFF4D6D : 0xFFFFC857);
            AxisAlignedBB bb = new AxisAlignedBB(
                    pos.getX() + 0.06, pos.getY() + 0.06, pos.getZ() + 0.06,
                    pos.getX() + 0.94, pos.getY() + 0.94, pos.getZ() + 0.94);

            RenderUtil.box3D(bb, c, (float) thickness.value);
            if (tracers.value) {
                RenderUtil.line(sx, sy, sz,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        c, (float) thickness.value);
            }
        }
    }
}
