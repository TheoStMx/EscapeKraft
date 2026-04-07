package fr.ekrec.scores;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EKPersistentState extends PersistentState {

    private static final String STATE_KEY = "escapekraft_scores";
    private static final Codec<EKScore> SCORE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("team").forGetter(EKScore::teamName),
            Codec.LONG.fieldOf("time").forGetter(EKScore::elapsedMs)
    ).apply(instance, EKScore::new));

    private static final Codec<EKPersistentState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, SCORE_CODEC.listOf())
                    .fieldOf("escapes")
                    .forGetter(state -> state.scores)
    ).apply(instance, EKPersistentState::new));

    private static final PersistentStateType<EKPersistentState> TYPE =
            new PersistentStateType<>(
                    STATE_KEY,
                    EKPersistentState::new,
                    CODEC,
                    null
            );

    private final Map<String, List<EKScore>> scores = new HashMap<>();

    public EKPersistentState() {}

    public EKPersistentState(Map<String, List<EKScore>> scores) {
        for (Map.Entry<String, List<EKScore>> entry : scores.entrySet()) {
            List<EKScore> escapeScores = new ArrayList<>(entry.getValue());
            escapeScores.sort(Comparator.comparingLong(EKScore::elapsedMs));
            this.scores.put(entry.getKey(), escapeScores);
        }
    }

    public void saveScore(String escapeName, String teamName, long elapsedMs) {
        scores.putIfAbsent(escapeName, new ArrayList<>());
        List<EKScore> escapeScores = scores.get(escapeName);
        escapeScores.removeIf(score -> score.teamName().equals(teamName));
        escapeScores.add(new EKScore(teamName, elapsedMs));
        escapeScores.sort(Comparator.comparingLong(EKScore::elapsedMs));
        markDirty();
    }

    public boolean removeEscape(String escapeName) {
        boolean removed = scores.remove(escapeName) != null;
        if (removed) {
            markDirty();
        }
        return removed;
    }

    public boolean removeScore(String escapeName, String teamName) {
        List<EKScore> escapeScores = scores.get(escapeName);
        if (escapeScores == null) {
            return false;
        }

        boolean removed = escapeScores.removeIf(score -> score.teamName().equals(teamName));
        if (!removed) {
            return false;
        }

        if (escapeScores.isEmpty()) {
            scores.remove(escapeName);
        }

        markDirty();
        return true;
    }

    public void setScore(String escapeName, String teamName, long elapsedMs) {
        saveScore(escapeName, teamName, elapsedMs);
    }

    public List<EKScore> getScores(String escapeName) {
        List<EKScore> escapeScores = scores.get(escapeName);
        if (escapeScores == null) {
            return List.of();
        }

        return List.copyOf(escapeScores);
    }

    public static EKPersistentState get(MinecraftServer server) {
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
