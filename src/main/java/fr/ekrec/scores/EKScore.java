package fr.ekrec.scores;

import fr.ekrec.text.EKTextFormatter;

public record EKScore(String teamName, long elapsedMs) {

    public String getFormattedTime() {
        return EKTextFormatter.formatElapsed(elapsedMs);
    }

}
