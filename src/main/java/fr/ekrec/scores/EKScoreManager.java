package fr.ekrec.scores;

import net.minecraft.server.MinecraftServer;
import java.util.ArrayList;
import java.util.List;

public class EKScoreManager {

    public static void saveScore(MinecraftServer server, String escapeName, String teamName, long elapsedMs) {
        EKPersistentState.get(server).saveScore(escapeName, teamName, elapsedMs);
    }

    public static List<EKScore> getScores(MinecraftServer server, String escapeName) {
        return EKPersistentState.get(server).getScores(escapeName);
    }

    public static List<EKScore> getTopScores(MinecraftServer server, String escapeName, int limit) {
        List<EKScore> scores = EKPersistentState.get(server).getScores(escapeName);
        return new ArrayList<>(scores.subList(0, Math.min(limit, scores.size())));
    }

    public static boolean removeEscape(MinecraftServer server, String escapeName) {
        return EKPersistentState.get(server).removeEscape(escapeName);
    }

    public static boolean removeScore(MinecraftServer server, String escapeName, String teamName) {
        return EKPersistentState.get(server).removeScore(escapeName, teamName);
    }

    public static void setScore(MinecraftServer server, String escapeName, String teamName, long elapsedMs) {
        EKPersistentState.get(server).setScore(escapeName, teamName, elapsedMs);
    }
}
