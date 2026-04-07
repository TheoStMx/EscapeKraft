package fr.ekrec.creativetabs;

import fr.ekrec.EscapeKraft;
import fr.ekrec.items.EKItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EKCreativeTabs {

    public static final RegistryKey<ItemGroup> ESCAPE_KRAFT_TAB_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(EscapeKraft.MOD_ID, "creative_tab")
    );

    public static final ItemGroup ESCAPE_KRAFT_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(EKItems.MUSIC_DISC_SONG_OF_THE_NIGHT))
            .displayName(Text.translatable("itemGroup." + EscapeKraft.MOD_ID + ".creative_tab"))
            .entries((params, output) -> {
                output.add(EKItems.MUSIC_DISC_SONG_OF_THE_NIGHT);
            })
            .build();

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, ESCAPE_KRAFT_TAB_KEY, ESCAPE_KRAFT_TAB);
    }

}