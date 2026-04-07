package fr.ekrec.teams;

import fr.ekrec.text.EKTextFormatter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EKTeamManager {

    private static final Map<String, EKTeam> teams = new HashMap<>();
    private static final Map<UUID, String> playerTeamIndex = new HashMap<>();
    private static int tickCount = 0;

    public static boolean createTeam(String name, UUID leader) {
        if (teams.containsKey(name)) {
            return false;
        }

        EKTeam team = new EKTeam(name, leader);
        teams.put(name, team);
        playerTeamIndex.put(leader, name);
        return true;
    }

    public static boolean joinTeam(String name, UUID player) {
        EKTeam team = teams.get(name);
        if (team == null) {
            return false;
        }

        removePlayer(player);
        team.addMember(player);
        playerTeamIndex.put(player, name);
        return true;
    }

    public static EKTeam getTeamOfPlayer(UUID player) {
        String name = playerTeamIndex.get(player);
        if (name == null) {
            return null;
        }

        return teams.get(name);
    }

    public static void removePlayer(UUID player) {
        String name = playerTeamIndex.remove(player);
        if (name == null) {
            return;
        }

        EKTeam team = teams.get(name);
        if (team == null) {
            return;
        }

        team.removeMember(player);
        if (team.isEmpty()) {
            teams.remove(name);
        }
    }

    public static boolean startTimer(UUID player) {
        EKTeam team = getTeamOfPlayer(player);
        if (team == null) {
            return false;
        }

        return team.startTimer(System.currentTimeMillis());
    }

    public static void tickTimers(MinecraftServer server) {
        tickCount++;
        if (tickCount < 20) {
            return;
        }
        tickCount = 0;

        long now = System.currentTimeMillis();

        for (EKTeam team : teams.values()) {
            long elapsedMs = team.getElapsedMs(now);
            if (elapsedMs == -1) {
                continue;
            }

            Text message = EKTextFormatter.buildLiveTimerText(team.getName(), elapsedMs);

            for (UUID memberId : team.getMembers()) {
                ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberId);
                if (member != null) {
                    member.networkHandler.sendPacket(new OverlayMessageS2CPacket(message));
                }
            }
        }
    }

    public static long stopTimer(UUID player) {
        EKTeam team = getTeamOfPlayer(player);
        if (team == null) {
            return -1;
        }

        return team.stopTimer(System.currentTimeMillis());
    }

    public static void onPlayerDisconnect(UUID player, MinecraftServer server) {
        EKTeam team = getTeamOfPlayer(player);
        if (team == null) {
            return;
        }

        if (team.isTimerRunning()) {
            for (UUID memberId : team.getMembers()) {
                if (memberId.equals(player)) {
                    continue;
                }

                ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(
                            Text.translatable("ek.team.member_disconnected").formatted(Formatting.YELLOW),
                            false
                    );
                }
            }
            return;
        }

        removePlayer(player);
    }

    public static void onPlayerReconnect(ServerPlayerEntity player, MinecraftServer server) {
        EKTeam team = getTeamOfPlayer(player.getUuid());
        if (team == null || !team.isTimerRunning()) {
            return;
        }

        player.sendMessage(
                Text.translatable("ek.team.reconnected", team.getName()).formatted(Formatting.GREEN),
                false
        );
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(EKTeamManager::tickTimers);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                EKTeamManager.onPlayerDisconnect(handler.player.getUuid(), server)
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EKTeamManager.onPlayerReconnect(handler.player, server)
        );
    }
}
