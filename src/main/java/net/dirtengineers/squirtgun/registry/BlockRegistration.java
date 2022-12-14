package net.dirtengineers.squirtgun.registry;

import net.dirtengineers.squirtgun.Constants;
import net.dirtengineers.squirtgun.Squirtgun;
import net.dirtengineers.squirtgun.common.block.BrassBlock;
import net.dirtengineers.squirtgun.common.block.EncapsulatorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistration {
    public static final DeferredRegister<Block> BLOCKS;
    public static final RegistryObject<Block> ENCAPSULATOR_BLOCK;
    public static final RegistryObject<Block> BRASS_BLOCK;

    public BlockRegistration() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }


    static{
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Squirtgun.MOD_ID);
        ENCAPSULATOR_BLOCK = BLOCKS.register(Constants.encapsulatorBlockName, EncapsulatorBlock::new);
        BRASS_BLOCK = BLOCKS.register(Constants.brassBlockName, BrassBlock::new);
    }
}