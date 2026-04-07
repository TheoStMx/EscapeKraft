package fr.ekrec.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.ekrec.leaderboard.EKLeaderboardDisplayManager;
import fr.ekrec.scores.EKScore;
import fr.ekrec.scores.EKScoreManager;
import fr.ekrec.teams.EKTeam;
import fr.ekrec.teams.EKTeamManager;
import fr.ekrec.text.EKTextFormatter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.UUID;

public class EKCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("ek")
                        .then(CommandManager.literal("create")
                                .executes(EKCommands::executeCreateWithPlayerName)
                                .then(CommandManager.argument("teamName", StringArgumentType.word())
                                        .executes(EKCommands::executeCreate)
                                )
                        )
                        .then(CommandManager.literal("join")
                                .then(CommandManager.argument("teamName", StringArgumentType.word())
                                        .executes(EKCommands::executeJoin)
                                )
                        )
                        .then(CommandManager.literal("start")
                                .requires(EKCommands::canUseAdminCommands)
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(EKCommands::executeStart)
                                )
                        )
                        .then(CommandManager.literal("stop")
                                .requires(EKCommands::canUseAdminCommands)
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .then(CommandManager.argument("escapeName", StringArgumentType.word())
                                                .executes(EKCommands::executeStop)
                                        )
                                )
                        )
                        .then(CommandManager.literal("leaderboard")
                                .requires(EKCommands::canUseAdminCommands)
                                .then(CommandManager.literal("display")
                                        .then(CommandManager.literal("set")
                                                .then(CommandManager.argument("escapeName", StringArgumentType.word())
                                                        .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                                                                .executes(EKCommands::executeLeaderboardDisplaySet)
                                                        )
                                                )
                                        )
                                        .then(CommandManager.literal("refresh")
                                                .then(CommandManager.argument("escapeName", StringArgumentType.word())
                                                        .executes(EKCommands::executeLeaderboardDisplayRefresh)
                                                )
                                        )
                                        .then(CommandManager.literal("remove")
                                                .then(CommandManager.argument("escapeName", StringArgumentType.word())
                                                        .executes(EKCommands::executeLeaderboardDisplayRemove)
                                                )
                                        )
                                )
                                .then(CommandManager.literal("remove")
                                        .then(CommandManager.argument("escapeName", StringArgumentType.word())
                                                .executes(EKCommands::executeLeaderboardRemoveEscape)
                                                .then(CommandManager.argument("teamName", StringArgumentType.word())
                                                        .executes(EKCommands::executeLeaderboardRemoveScore)
                                                )
                                        )
                                )
                                .then(CommandManager.literal("setms")
                                        .then(CommandManager.argument("escapeName", StringArgumentType.word())
                                                .then(CommandManager.argument("teamName", StringArgumentType.word())
                                                        .then(CommandManager.argument("elapsedMs", LongArgumentType.longArg(0))
                                                                .executes(EKCommands::executeLeaderboardSetMs)
                                                        )
                                                )
                                        )
                                )
                                .then(CommandManager.argument("escapeName", StringArgumentType.word())
                                        .executes(EKCommands::executeLeaderboard)
                                )
                        )
        );
    }

    private static boolean canUseAdminCommands(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return true;
        }

        return source.getServer()
                .getPlayerManager()
                .isOperator(new PlayerConfigEntry(player.getGameProfile()));
    }

    private static int executeCreate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String teamName = StringArgumentType.getString(ctx, "teamName");
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        return createTeam(ctx, player, teamName);
    }

    private static int executeCreateWithPlayerName(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
        String teamName = player.getNameForScoreboard();
        return createTeam(ctx, player, teamName);
    }

    private static int createTeam(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, String teamName) {
        boolean created = EKTeamManager.createTeam(teamName, player.getUuid());

        if (!created) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.team.already_exists", teamName).formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.team.created", teamName, teamName).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    private static int executeJoin(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String teamName = StringArgumentType.getString(ctx, "teamName");
        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

        EKTeam previousTeam = EKTeamManager.getTeamOfPlayer(player.getUuid());
        if (previousTeam != null) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.team.left", previousTeam.getName()).formatted(Formatting.YELLOW),
                    false
            );
        }

        boolean joined = EKTeamManager.joinTeam(teamName, player.getUuid());
        if (!joined) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.team.not_found", teamName).formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.team.joined", teamName).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    private static int executeStart(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");

        boolean started = EKTeamManager.startTimer(target.getUuid());
        if (!started) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.timer.start_failed").formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        EKTeam team = EKTeamManager.getTeamOfPlayer(target.getUuid());
        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.timer.started", team.getName()).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    private static int executeStop(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        MinecraftServer server = ctx.getSource().getServer();

        EKTeam team = EKTeamManager.getTeamOfPlayer(target.getUuid());
        if (team == null) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.timer.stop_failed").formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        long elapsed = EKTeamManager.stopTimer(target.getUuid());
        if (elapsed == -1) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.timer.stop_failed").formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        EKScoreManager.saveScore(server, escapeName, team.getName(), elapsed);
        if (EKLeaderboardDisplayManager.hasDisplay(server, escapeName)) {
            EKLeaderboardDisplayManager.refreshDisplay(server, escapeName);
        }

        EKScore score = new EKScore(team.getName(), elapsed);
        for (UUID memberId : team.getMembers()) {
            ServerPlayerEntity member = server.getPlayerManager().getPlayer(memberId);
            if (member != null) {
                member.sendMessage(
                        Text.translatable("ek.timer.stopped", team.getName(), score.getFormattedTime())
                                .formatted(Formatting.GREEN),
                        false
                );
            }
        }

        return 1;
    }

    private static int executeLeaderboard(CommandContext<ServerCommandSource> ctx) {
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        MinecraftServer server = ctx.getSource().getServer();
        List<EKScore> scores = EKScoreManager.getTopScores(server, escapeName, 10);

        if (scores.isEmpty()) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.leaderboard.empty", escapeName).formatted(Formatting.YELLOW),
                    false
            );
            return 0;
        }

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.leaderboard.title", escapeName).formatted(Formatting.GOLD, Formatting.BOLD),
                false
        );

        for (int i = 0; i < scores.size(); i++) {
            EKScore score = scores.get(i);
            int rank = i + 1;

            ctx.getSource().sendFeedback(
                    () -> EKTextFormatter.buildLeaderboardLine(rank, score.teamName(), score.elapsedMs()),
                    false
            );
        }

        return scores.size();
    }

    private static int executeLeaderboardDisplaySet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        Vec3d position = Vec3ArgumentType.getVec3(ctx, "position");

        EKLeaderboardDisplayManager.setDisplay(
                ctx.getSource().getServer(),
                escapeName,
                ctx.getSource().getWorld(),
                position
        );

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.leaderboard.display.set", escapeName).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    private static int executeLeaderboardDisplayRefresh(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        boolean refreshed = EKLeaderboardDisplayManager.refreshDisplay(ctx.getSource().getServer(), escapeName);

        if (!refreshed) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.leaderboard.display.not_configured", escapeName).formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.leaderboard.display.refreshed", escapeName).formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    private static int executeLeaderboardDisplayRemove(CommandContext<ServerCommandSource> ctx) {
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        boolean removed = EKLeaderboardDisplayManager.removeDisplay(ctx.getSource().getServer(), escapeName);

        if (!removed) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.leaderboard.display.not_configured", escapeName).formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.leaderboard.display.removed", escapeName).formatted(Formatting.YELLOW),
                false
        );
        return 1;
    }

    private static int executeLeaderboardRemoveEscape(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        MinecraftServer server = ctx.getSource().getServer();
        boolean removed = EKScoreManager.removeEscape(server, escapeName);

        if (!removed) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.leaderboard.remove_escape.not_found", escapeName).formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        refreshLeaderboardDisplayIfPresent(server, escapeName);

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.leaderboard.remove_escape.success", escapeName).formatted(Formatting.YELLOW),
                false
        );
        return 1;
    }

    private static int executeLeaderboardRemoveScore(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        String teamName = StringArgumentType.getString(ctx, "teamName");
        MinecraftServer server = ctx.getSource().getServer();
        boolean removed = EKScoreManager.removeScore(server, escapeName, teamName);

        if (!removed) {
            ctx.getSource().sendFeedback(
                    () -> Text.translatable("ek.leaderboard.remove_score.not_found", teamName, escapeName).formatted(Formatting.RED),
                    false
            );
            return 0;
        }

        refreshLeaderboardDisplayIfPresent(server, escapeName);

        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.leaderboard.remove_score.success", teamName, escapeName).formatted(Formatting.YELLOW),
                false
        );
        return 1;
    }

    private static int executeLeaderboardSetMs(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String escapeName = StringArgumentType.getString(ctx, "escapeName");
        String teamName = StringArgumentType.getString(ctx, "teamName");
        long elapsedMs = LongArgumentType.getLong(ctx, "elapsedMs");
        MinecraftServer server = ctx.getSource().getServer();

        EKScoreManager.setScore(server, escapeName, teamName, elapsedMs);
        refreshLeaderboardDisplayIfPresent(server, escapeName);

        EKScore score = new EKScore(teamName, elapsedMs);
        ctx.getSource().sendFeedback(
                () -> Text.translatable("ek.leaderboard.setms.success", teamName, escapeName, score.getFormattedTime())
                        .formatted(Formatting.GREEN),
                false
        );
        return 1;
    }

    private static void refreshLeaderboardDisplayIfPresent(MinecraftServer server, String escapeName) throws CommandSyntaxException {
        if (EKLeaderboardDisplayManager.hasDisplay(server, escapeName)) {
            EKLeaderboardDisplayManager.refreshDisplay(server, escapeName);
        }
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> EKCommands.register(dispatcher)
        );
    }
}
