package com.korosensei.simplerenderlib;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public class TilePowerChair extends TileEntity {

    private static final int GRID_SIZE_X = 32;
    private static final int GRID_SIZE_Y = 32;
    private static final int GRID_SIZE_Z = 128;

    public EnumFacing Facing;
    public int facing;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
            xCoord,
            yCoord,
            zCoord,
            xCoord + GRID_SIZE_X,
            yCoord + GRID_SIZE_Y,
            zCoord + GRID_SIZE_Z
        );
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 16777216.0;
    }


}
