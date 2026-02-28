package com.korosensei.simplerenderlib;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPowerChair extends Block {
    public BlockPowerChair() {
        super(Material.iron);
        this.setResistance(20f);
        this.setHardness(5.0f);
        this.setBlockName("tst.PowerChair");
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.55F, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        blockIcon = iconRegister.registerIcon("simplerenderlib:TRANSPARENT");
    }

    @Override
    public String getUnlocalizedName() {
        return "BlockPowerChair";
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canRenderInPass(int a) {
        return true;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }


    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TilePowerChair();
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    public static class ItemBlockPowerChair extends ItemBlock {

        public ItemBlockPowerChair(Block p_i45328_1_) {
            super(p_i45328_1_);
            setHasSubtypes(true);
            setMaxDamage(0);
        }

        @Override
        public String getUnlocalizedName(ItemStack aStack) {
            return this.field_150939_a.getUnlocalizedName() + "." + this.getDamage(aStack);
        }

        @Override
        public int getMetadata(int aMeta) {
            return aMeta;
        }

    }
}
