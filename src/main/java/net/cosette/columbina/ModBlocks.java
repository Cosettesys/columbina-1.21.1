package net.cosette.columbina;

import net.cosette.columbina.mysterychest.MysteryChestBlock;
import net.cosette.columbina.portal.*;
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
    public static final NetherPortalCustomBlock NETHER_PORTAL_CUSTOM = registerBlock(
            "nether_portal_custom",
            new NetherPortalCustomBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.PURPLE)
                            .noCollision()
                            .strength(-1.0f, 3600000.0f)
                            .sounds(BlockSoundGroup.GLASS)
                            .luminance(state -> 11)
                            .dropsNothing()
            )
    );
    public static final MysteryChestBlock MYSTERY_CHEST_PETIT = registerBlock(
            "mystery_chest_petit",
            new MysteryChestBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.BROWN)
                            .strength(-1.0f, 3600000.0f)
                            .sounds(BlockSoundGroup.WOOD)
                            .dropsNothing(),
                    MysteryChestBlock.ChestSize.PETIT
            )
    );
    public static final MysteryChestBlock MYSTERY_CHEST_MOYEN = registerBlock(
            "mystery_chest_moyen",
            new MysteryChestBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.BROWN)
                            .strength(-1.0f, 3600000.0f)
                            .sounds(BlockSoundGroup.WOOD)
                            .dropsNothing(),
                    MysteryChestBlock.ChestSize.MOYEN
            )
    );
    public static final MysteryChestBlock MYSTERY_CHEST_GRAND = registerBlock(
            "mystery_chest_grand",
            new MysteryChestBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.BROWN)
                            .strength(-1.0f, 3600000.0f)
                            .sounds(BlockSoundGroup.WOOD)
                            .dropsNothing(),
                    MysteryChestBlock.ChestSize.GRAND
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