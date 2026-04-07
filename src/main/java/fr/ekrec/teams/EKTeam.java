package fr.ekrec.teams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EKTeam {

    private final String name;
    private final List<UUID> members = new ArrayList<>();
    private final UUID leader;
    private long startTime = -1;

    public EKTeam(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        addMember(leader);
    }

    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public List<UUID> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public boolean addMember(UUID playerId) {
        if (members.contains(playerId)) {
            return false;
        }

        return members.add(playerId);
    }

    public boolean removeMember(UUID playerId) {
        return members.remove(playerId);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean isTimerRunning() {
        return startTime != -1;
    }

    public boolean startTimer(long startedAtMs) {
        if (isTimerRunning()) {
            return false;
        }

        startTime = startedAtMs;
        return true;
    }

    public long stopTimer(long stoppedAtMs) {
        if (!isTimerRunning()) {
            return -1;
        }

        long elapsed = stoppedAtMs - startTime;
        startTime = -1;
        return elapsed;
    }

    public long getElapsedMs(long nowMs) {
        if (!isTimerRunning()) {
            return -1;
        }

        return nowMs - startTime;
    }
}
