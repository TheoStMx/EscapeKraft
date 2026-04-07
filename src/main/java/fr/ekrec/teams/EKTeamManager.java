package fr.ekrec.teams;

import fr.ekrec.scores.EKScore;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class EKTeamManager {

    private static final Map<String, EKTeam> teams = new HashMap<>();
    private static final Map<UUID, String> playerTeamIndex = new HashMap<>();
    private static int tickCount = 0;

    public static boolean createTeam(String name, UUID leader) {
        if (teams.containsKey(name)) return false; // team already existing
        EKTeam team = new EKTeam(name, leader);
        teams.put(name, team);
        playerTeamIndex.put(leader, name);
        return true;
    }

    public static boolean joinTeam(String name, UUID player) {
        EKTeam team = teams.get(name);
        if (team == null) return false;
        removePlayer(player);
        team.members.add(player);
        playerTeamIndex.put(player, name);
        return true;
    }

    public static EKTeam getTeamOfPlayer(UUID player) {
        String name = playerTeamIndex.get(player);
        if (name == null) return null;
        return teams.get(name);
    }

    public static void removePlayer(UUID player) {
        String name = playerTeamIndex.remove(player);
        if (name != null) {
            EKTeam team = teams.get(name);
            if (team != null) {
                team.members.remove(player);

                if (team.members.isEmpty()) {
                    teams.remove(name);
                }
            }
        }
    }

    public static boolean startTimer(UUID player) {
        EKTeam team = getTeamOfPlayer(player);
        if (team == null) return false;          // player without team
        if (team.startTime != -1) return false;  // timer still running

        team.startTime = System.currentTimeMillis();
        return true;
    }

    public static void tickTimers(MinecraftServer server) {
        tickCount++;
        if (tickCount < 20) return; // 20 ticks = 1 sec
        tickCount = 0;

        Scoreboard scoreboard = server.getScoreboard();

        for (EKTeam team : teams.values()) {
            if (team.startTime == -1) continue; // timer not running : skip

            long elapsed = (System.currentTimeMillis() - team.startTime) / 1000;
            int minutes = (int) (elapsed / 60);
            int seconds = (int) (elapsed % 60);
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);

            Text message = Text.literal("⏱ " + team.name + " | " + timeFormatted)
                    .formatted(Formatting.GREEN);

            // Message sent to every team members connected
            for (UUID memberId : team.members) {
                ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberId);
                if (member != null) {
                    member.networkHandler.sendPacket(
                            new OverlayMessageS2CPacket(message)
                    );
                }
            }
        }
    }

    public static long stopTimer(UUID player) {
        EKTeam team = getTeamOfPlayer(player);
        if (team == null) return -1;         // player without team
        if (team.startTime == -1) return -1; // timer not running

        long elapsed = System.currentTimeMillis() - team.startTime;
        team.startTime = -1; // -1 -> timer stopped
        return elapsed;
    }

    public static void onPlayerDisconnect(UUID player, MinecraftServer server) {
        EKTeam team = getTeamOfPlayer(player);
        if (team == null) return; // if player has no team

        if (team.startTime != -1) {
            // Timer started : keep the disconnected player in the team, all teammates prevented
            for (UUID memberId : team.members) {
                if (memberId.equals(player)) continue; // skip disconnected player
                ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberId);
                if (member != null) {
                    member.sendMessage(
                            Text.translatable("ek.team.member_disconnected").formatted(Formatting.YELLOW),
                            false
                    );
                }
            }
        } else {
            // Timer non started : remove the player
            removePlayer(player);
        }
    }

    public static void onPlayerReconnect(ServerPlayerEntity player, MinecraftServer server) {
        EKTeam team = getTeamOfPlayer(player.getUuid());
        if (team == null) return;          // if no team
        if (team.startTime == -1) return;  // timer not started

        player.sendMessage(
                Text.translatable("ek.team.reconnected", team.name).formatted(Formatting.GREEN),
                false
        );
    }

    public static void initialize() {

        ServerTickEvents.END_SERVER_TICK.register(EKTeamManager::tickTimers);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            EKTeamManager.onPlayerDisconnect(handler.player.getUuid(), server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            EKTeamManager.onPlayerReconnect(handler.player, server);
        });

    }

}
