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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

                List<PolyBar> activeLeftBars = new ArrayList<>();
                for (PolyBarHolder holder : holdersInRow) {
                    if (holder.getAssignedSide() == PolyBarSide.LEFT) {
                        holder.getActiveBar(player).ifPresent(activeLeftBars::add);
                    }
                }

                List<PolyBar> activeRightBars = new ArrayList<>();
                for (PolyBarHolder holder : holdersInRow) {
                    if (holder.getAssignedSide() == PolyBarSide.RIGHT) {
                        holder.getActiveBar(player).ifPresent(activeRightBars::add);
                    }
                }

                if (activeLeftBars.isEmpty() && activeRightBars.isEmpty()) {
                    continue;
                }

                Identifier fontId = Identifier.fromNamespaceAndPath("polybars", "row_" + rowIndex);

                MutableComponent leftComp = Component.empty();
                for (PolyBar leftBar : activeLeftBars) {
                    leftComp.append(leftBar.getBarComponent(player));
                }
                int effectiveLeftWidth = ComponentWidthCalculator.calculateWidth(leftComp, false);

                MutableComponent rightComp = Component.empty();
                for (PolyBar rightBar : activeRightBars) {
                    rightComp.append(rightBar.getBarComponent(player));
                }
                int effectiveRightWidth = ComponentWidthCalculator.calculateWidth(rightComp, false);

                MutableComponent rowComp = Component.empty();

                rowComp.append(SpaceBuilder.getNegativeSpaceComponent(CENTER_TO_HOTBAR_DISTANCE));
                rowComp.append(leftComp);
                int bridge = CENTER_TO_HOTBAR_DISTANCE * 2 - effectiveLeftWidth - effectiveRightWidth;
                if (bridge < 0) {
                    rowComp.append(SpaceBuilder.getNegativeSpaceComponent(-bridge));
                } else if (bridge > 0) {
                    rowComp.append(SpaceBuilder.getPositiveSpaceComponent(bridge));
                }
                rowComp.append(rightComp);
                rowComp.append(SpaceBuilder.getNegativeSpaceComponent(CENTER_TO_HOTBAR_DISTANCE));


                int netAdvance = ComponentWidthCalculator.calculateWidth(rowComp);
                if (netAdvance > 0) {
                    rowComp.append(SpaceBuilder.getNegativeSpaceComponent(netAdvance));
                } else if (netAdvance < 0) {
                    rowComp.append(SpaceBuilder.getPositiveSpaceComponent(-netAdvance));
                }


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
