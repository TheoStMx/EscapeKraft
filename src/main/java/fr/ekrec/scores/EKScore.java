package fr.ekrec.scores;

public record EKScore(String teamName, long elapsedMs) {

    // Display time -> mm:ss:ms
    public String getFormattedTime() {
        long seconds = elapsedMs / 1000;
        long ms = (elapsedMs % 1000) / 10; // 2 digits
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d:%02d", minutes, secs, ms);
    }

}
