package xyz.elytrafae.mc.polybars.multiplexer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.elytrafae.mc.polybars.api.PolyBar;
import xyz.elytrafae.mc.polybars.api.PolyBarHolder;
import xyz.elytrafae.mc.polybars.api.PolyBarRegistry;
import xyz.elytrafae.mc.polybars.api.PolyBarSide;
import xyz.elytrafae.mc.polybars.font.ComponentWidthCalculator;
import xyz.elytrafae.mc.polybars.font.SpaceBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBarMultiplexer {

    private static final int CENTER_TO_HOTBAR_DISTANCE = 91;
    private static final ActionBarMultiplexer INSTANCE = new ActionBarMultiplexer();

    private final Map<UUID, PlayerHudSession> sessions = new ConcurrentHashMap<>();
    private final ThreadLocal<Boolean> sendingInternal = ThreadLocal.withInitial(() -> false);

    public static ActionBarMultiplexer getInstance() {
        return INSTANCE;
    }

    public void cacheVanillaActionBar(ServerPlayer player, Component text) {
        if (player == null) return;
        PlayerHudSession session = getSession(player.getUUID());
        session.setVanillaActionBar(text);
    }

    public boolean isSendingInternal() {
        return sendingInternal.get();
    }

    public PlayerHudSession getSession(UUID playerUuid) {
        return sessions.computeIfAbsent(playerUuid, u -> new PlayerHudSession());
    }

    public void removeSession(UUID playerUuid) {
        sessions.remove(playerUuid);
    }

    public void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlayerHudSession session = getSession(player.getUUID());
            session.tick();

            Component assembled = assembleHudComponent(player, session);
            sendDirectly(player, assembled);
            session.setLastSentComponent(assembled);
        }
    }

    public Component assembleHudComponent(ServerPlayer player, PlayerHudSession session) {
        MutableComponent hud = Component.empty();

        Map<Integer, List<PolyBarHolder>> rowMap = PolyBarRegistry.getHoldersByRowOrdered();

        if (!rowMap.isEmpty()) {
            for (Map.Entry<Integer, List<PolyBarHolder>> entry : rowMap.entrySet()) {
                int rowIndex = entry.getKey();
                List<PolyBarHolder> holdersInRow = entry.getValue();

                Optional<PolyBar> leftBar = Optional.empty();
                Optional<PolyBar> rightBar = Optional.empty();

                for (PolyBarHolder holder : holdersInRow) {
                    if (holder.getAssignedSide() == PolyBarSide.LEFT) {
                        leftBar = holder.getActiveBar(player);
                    } else if (holder.getAssignedSide() == PolyBarSide.RIGHT) {
                        rightBar = holder.getActiveBar(player);
                    }
                }

                if (leftBar.isEmpty() && rightBar.isEmpty()) {
                    continue;
                }

                Component leftComp = Component.empty();
                if (leftBar.isPresent()) {
                    leftComp = leftBar.get().getBarComponent(player);
                }

                Component rightComp = Component.empty();
                if (rightBar.isPresent()) {
                    rightComp = rightBar.get().getBarComponent(player);
                }

                int effectiveLeftWidth = ComponentWidthCalculator.calculateWidth(leftComp, false);
                int effectiveRightWidth = ComponentWidthCalculator.calculateWidth(rightComp, false);
                System.out.println("Effective width: " + effectiveLeftWidth);

                MutableComponent rowComp = Component.empty();

                rowComp.append(SpaceBuilder.getNegativeSpaceComponent(CENTER_TO_HOTBAR_DISTANCE));
                rowComp.append(leftComp);
                int bridge = CENTER_TO_HOTBAR_DISTANCE * 2 - effectiveLeftWidth - effectiveRightWidth;
                rowComp.append(SpaceBuilder.getSpaceComponent(bridge));
                rowComp.append(rightComp);
                rowComp.append(SpaceBuilder.getNegativeSpaceComponent(CENTER_TO_HOTBAR_DISTANCE));


                int netAdvance = ComponentWidthCalculator.calculateWidth(rowComp);
                rowComp.append(SpaceBuilder.getSpaceComponent(-netAdvance));


                hud.append(rowComp);
            }
        }

        Component vanillaMessage = session.getCachedVanillaActionBar();
        if (vanillaMessage != null) {
            hud.append(vanillaMessage);
        }

        return hud;
    }

    public void sendDirectly(ServerPlayer player, Component component) {
        if (player == null) return;
        sendingInternal.set(true);
        try {
            player.connection.send(new ClientboundSetActionBarTextPacket(component));
        } finally {
            sendingInternal.set(false);
        }
    }
}
