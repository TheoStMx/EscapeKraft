package fr.ekrec.leaderboard;

public record EKLeaderboardDisplayLine(
        int rank,
        String teamName,
        long elapsedMs
) {}
