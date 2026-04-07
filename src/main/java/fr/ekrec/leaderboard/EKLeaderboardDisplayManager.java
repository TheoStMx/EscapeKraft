package fr.ekrec.leaderboard;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.ekrec.EscapeKraft;
import fr.ekrec.scores.EKScore;
import fr.ekrec.scores.EKScoreManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Optional;

public final class EKLeaderboardDisplayManager {

    private static final String BASE_TAG = "ek_leaderboard";
    private static final int TOP_LIMIT = 10;
    private static final double LINE_SPACING = 0.3D;
    private static final double TITLE_OFFSET_Y = 2.0D;

    private EKLeaderboardDisplayManager() {}

    public static void setDisplay(MinecraftServer server, String escapeName, ServerWorld world, Vec3d position) throws CommandSyntaxException {
        EKLeaderboardDisplayState.get(server).setDisplay(
                new EKLeaderboardDisplayConfig(
                        escapeName,
                        world.getRegistryKey().getValue().toString(),
                        position.x,
                        position.y,
                        position.z
                )
        );
        refreshDisplay(server, escapeName);
    }

    public static boolean refreshDisplay(MinecraftServer server, String escapeName) throws CommandSyntaxException {
        Optional<EKLeaderboardDisplayConfig> config = EKLeaderboardDisplayState.get(server).getDisplay(escapeName);
        if (config.isEmpty()) {
            return false;
        }

        ServerWorld world = getWorld(server, config.get().worldId());
        if (world == null) {
            return false;
        }

        removeTaggedEntities(world, escapeName);

        Vec3d origin = new Vec3d(config.get().x(), config.get().y(), config.get().z());
        summonTextDisplay(world, origin.add(0.0D, TITLE_OFFSET_Y, 0.0D), titleText(escapeName), escapeName, true);

        List<EKScore> scores = EKScoreManager.getTopScores(server, escapeName, TOP_LIMIT);
        if (scores.isEmpty()) {
            summonTextDisplay(world, origin.add(0.0D, TITLE_OFFSET_Y - LINE_SPACING, 0.0D), "No score recorded yet", escapeName, false);
            return true;
        }

        for (int i = 0; i < scores.size(); i++) {
            EKScore score = scores.get(i);
            double lineY = origin.y + TITLE_OFFSET_Y - ((i + 1) * LINE_SPACING);
            String line = (i + 1) + ". " + score.teamName() + " - " + score.getFormattedTime();
            summonTextDisplay(world, new Vec3d(origin.x, lineY, origin.z), line, escapeName, false);
        }

        return true;
    }

    public static boolean removeDisplay(MinecraftServer server, String escapeName) {
        EKLeaderboardDisplayState state = EKLeaderboardDisplayState.get(server);
        Optional<EKLeaderboardDisplayConfig> config = state.getDisplay(escapeName);
        if (config.isEmpty()) {
            return false;
        }

        ServerWorld world = getWorld(server, config.get().worldId());
        if (world != null) {
            removeTaggedEntities(world, escapeName);
        }

        return state.removeDisplay(escapeName);
    }

    public static boolean hasDisplay(MinecraftServer server, String escapeName) {
        return EKLeaderboardDisplayState.get(server).hasDisplay(escapeName);
    }

    private static ServerWorld getWorld(MinecraftServer server, String worldId) {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldId)));
    }

    private static void removeTaggedEntities(ServerWorld world, String escapeName) {
        String escapeTag = getEscapeTag(escapeName);
        List<? extends Entity> entities = world.getEntitiesByType(
                TypeFilter.instanceOf(Entity.class),
                entity -> entity.getCommandTags().contains(BASE_TAG) && entity.getCommandTags().contains(escapeTag)
        );

        for (Entity entity : entities) {
            entity.discard();
        }
    }

    private static void summonTextDisplay(ServerWorld world, Vec3d position, String line, String escapeName, boolean title) throws CommandSyntaxException {
        NbtCompound nbt = StringNbtReader.readCompound(
                buildDisplayNbt(position, line, escapeName, title)
        );
        Entity entity = EntityType.loadEntityWithPassengers(
                nbt,
                world,
                SpawnReason.COMMAND,
                loadedEntity -> loadedEntity
        );

        if (entity == null) {
            EscapeKraft.LOGGER.error("Failed to create leaderboard display entity for escape '{}'", escapeName);
            return;
        }

        if (!world.spawnNewEntityAndPassengers(entity)) {
            EscapeKraft.LOGGER.error("Failed to spawn leaderboard display entity for escape '{}'", escapeName);
        }
    }

    private static String buildDisplayNbt(Vec3d position, String line, String escapeName, boolean title) {
        return "{"
                + "id:\"minecraft:text_display\","
                + "Pos:[" + position.x + "d," + position.y + "d," + position.z + "d],"
                + "text:" + buildTextComponent(line, title) + ","
                + "billboard:\"center\","
                + "see_through:1b,"
                + "shadow:1b,"
                + "background:1073741824,"
                + "line_width:220,"
                + "Tags:[\"" + BASE_TAG + "\",\"" + getEscapeTag(escapeName) + "\"" + (title ? ",\"ek_leaderboard_title\"" : ",\"ek_leaderboard_line\"") + "]"
                + "}";
    }

    private static String buildTextComponent(String line, boolean title) {
        if (title) {
            return "{"
                    + "\"text\":\"" + escapeJson(line) + "\","
                    + "\"color\":\"gold\","
                    + "\"bold\":true"
                    + "}";
        }

        int separatorIndex = line.indexOf(" - ");
        if (separatorIndex < 0) {
            return "{\"text\":\"" + escapeJson(line) + "\"}";
        }

        String leftPart = line.substring(0, separatorIndex);
        String timePart = line.substring(separatorIndex + 3);

        int teamSeparatorIndex = leftPart.indexOf(". ");
        if (teamSeparatorIndex < 0) {
            return "{\"text\":\"" + escapeJson(line) + "\"}";
        }

        String rankPart = leftPart.substring(0, teamSeparatorIndex + 2);
        String teamPart = leftPart.substring(teamSeparatorIndex + 2);

        return "{"
                + "\"text\":\"" + escapeJson(rankPart) + "\","
                + "\"color\":\"yellow\","
                + "\"extra\":["
                + "{"
                + "\"text\":\"" + escapeJson(teamPart) + "\","
                + "\"color\":\"aqua\""
                + "},"
                + "{"
                + "\"text\":\" - \","
                + "\"color\":\"gray\""
                + "},"
                + "{"
                + "\"text\":\"" + escapeJson(timePart) + "\","
                + "\"color\":\"green\""
                + "}"
                + "]"
                + "}";
    }

    private static String getEscapeTag(String escapeName) {
        return "ek_escape_" + escapeName.replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase();
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String titleText(String escapeName) {
        return escapeName;
    }
}
