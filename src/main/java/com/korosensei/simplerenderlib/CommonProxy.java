package com.korosensei.simplerenderlib;

import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import net.minecraft.block.Block;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public abstract class CommonProxy {

    public static final Block PowerChair = new BlockPowerChair();

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {

    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerBlock(PowerChair, BlockPowerChair.ItemBlockPowerChair.class, "BlockPowerChair");
        GameRegistry.registerTileEntity(TilePowerChair.class, "TilePowerChair");
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}

}
