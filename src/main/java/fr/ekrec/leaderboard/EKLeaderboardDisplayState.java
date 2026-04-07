package fr.ekrec.leaderboard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EKLeaderboardDisplayState extends PersistentState {

    private static final String STATE_KEY = "escapekraft_leaderboard_displays";
    private static final Codec<EKLeaderboardDisplayConfig> CONFIG_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("escape_name").forGetter(EKLeaderboardDisplayConfig::escapeName),
            Codec.STRING.fieldOf("world_id").forGetter(EKLeaderboardDisplayConfig::worldId),
            Codec.DOUBLE.fieldOf("x").forGetter(EKLeaderboardDisplayConfig::x),
            Codec.DOUBLE.fieldOf("y").forGetter(EKLeaderboardDisplayConfig::y),
            Codec.DOUBLE.fieldOf("z").forGetter(EKLeaderboardDisplayConfig::z)
    ).apply(instance, EKLeaderboardDisplayConfig::new));

    private static final Codec<EKLeaderboardDisplayState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, CONFIG_CODEC)
                    .fieldOf("displays")
                    .forGetter(state -> state.displays)
    ).apply(instance, EKLeaderboardDisplayState::new));

    private static final PersistentStateType<EKLeaderboardDisplayState> TYPE =
            new PersistentStateType<>(STATE_KEY, EKLeaderboardDisplayState::new, CODEC, null);

    private final Map<String, EKLeaderboardDisplayConfig> displays = new HashMap<>();

    public EKLeaderboardDisplayState() {}

    public EKLeaderboardDisplayState(Map<String, EKLeaderboardDisplayConfig> displays) {
        this.displays.putAll(displays);
    }

    public void setDisplay(EKLeaderboardDisplayConfig config) {
        displays.put(config.escapeName(), config);
        markDirty();
    }

    public Optional<EKLeaderboardDisplayConfig> getDisplay(String escapeName) {
        return Optional.ofNullable(displays.get(escapeName));
    }

    public boolean hasDisplay(String escapeName) {
        return displays.containsKey(escapeName);
    }

    public boolean removeDisplay(String escapeName) {
        boolean removed = displays.remove(escapeName) != null;
        if (removed) {
            markDirty();
        }
        return removed;
    }

    public static EKLeaderboardDisplayState get(MinecraftServer server) {
        ServerWorld world = Objects.requireNonNull(
                server.getWorld(RegistryKey.of(
                        RegistryKeys.WORLD,
                        Identifier.ofVanilla("overworld")
                ))
        );
        PersistentStateManager manager = world.getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }
}
