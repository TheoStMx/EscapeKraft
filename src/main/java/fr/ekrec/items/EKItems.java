package fr.ekrec.items;

import fr.ekrec.EscapeKraft;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class EKItems {

    public static final Item MUSIC_DISC_SONG_OF_THE_NIGHT = register(
            "music_disc_song_of_the_night",
            Item::new,
            new Item.Settings()
                    .maxCount(1)
                    .jukeboxPlayable(RegistryKey.of(RegistryKeys.JUKEBOX_SONG,
                            Identifier.of(EscapeKraft.MOD_ID, "song_of_the_night")))
    );

    public static <T extends Item> T register(String name, Function<Item.Settings, T> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(EscapeKraft.MOD_ID, name));
        T item = itemFactory.apply(settings.registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    public static void initialize() {}

}