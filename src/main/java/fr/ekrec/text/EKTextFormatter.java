package fr.ekrec.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class EKTextFormatter {

    private static final String TIMER_ICON = "⏱";

    private EKTextFormatter() {}

    public static String formatElapsed(long elapsedMs) {
        long totalSeconds = elapsedMs / 1000;
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static MutableText buildLiveTimerText(String teamName, long elapsedMs) {
        return Text.literal(TIMER_ICON + " " + teamName + " | ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(formatElapsed(elapsedMs)).formatted(Formatting.WHITE));
    }

    public static MutableText buildLeaderboardLine(int rank, String teamName, long elapsedMs) {
        return Text.literal(rank + ". ")
                .formatted(Formatting.YELLOW)
                .append(Text.literal(teamName).formatted(Formatting.AQUA))
                .append(Text.literal(" - ").formatted(Formatting.GRAY))
                .append(Text.literal(formatElapsed(elapsedMs)).formatted(Formatting.GREEN));
    }
}
