package fr.ekrec.teams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EKTeam {

    public final String name;
    public final List<UUID> members = new ArrayList<>();
    public final UUID leader;
    public long startTime = -1;

    public EKTeam(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

}
