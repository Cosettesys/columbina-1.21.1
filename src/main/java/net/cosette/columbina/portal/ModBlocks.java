package net.cosette.columbina.portal;

import net.cosette.columbina.Columbina;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final PoketopiaPortalBlock POKETOPIA_PORTAL = registerBlock(
            "poketopia_portal",
            new PoketopiaPortalBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.PURPLE)
                            .noCollision()
                            .strength(-1.0f, 3600000.0f)
                            .sounds(BlockSoundGroup.GLASS)
                            .luminance(state -> 11)
                            .dropsNothing()
            )
    );
    private static <T extends Block> T registerBlock(String name, T block) {
        Registry.register(Registries.ITEM,
                Identifier.of(Columbina.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK,
                Identifier.of(Columbina.MOD_ID, name), block);
    }
    public static void registerBlocks() {}
}