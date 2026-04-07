package fr.ekrec.sounds;

import fr.ekrec.EscapeKraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class EKSounds {

    public static final SoundEvent SONG_OF_THE_NIGHT = register("song_of_the_night");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(EscapeKraft.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {}
}