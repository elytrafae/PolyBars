package xyz.elytrafae.mc.polybars;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.elytrafae.mc.polybars.api.PolyBarHolder;
import xyz.elytrafae.mc.polybars.api.PolyBarSide;
import xyz.elytrafae.mc.polybars.api.PolyBarsApi;
import xyz.elytrafae.mc.polybars.defaultbars.DefaultAirBar;
import xyz.elytrafae.mc.polybars.defaultbars.DefaultArmorBar;
import xyz.elytrafae.mc.polybars.defaultbars.DefaultHealthBar;
import xyz.elytrafae.mc.polybars.defaultbars.DefaultHungerBar;
import xyz.elytrafae.mc.polybars.emulator.PredictionEmulationHandler;
import xyz.elytrafae.mc.polybars.generator.DynamicFontGenerator;
import xyz.elytrafae.mc.polybars.multiplexer.ActionBarMultiplexer;

import xyz.elytrafae.mc.polybars.defaultbars.DefaultVehicleHealthBar;

public class PolyBars implements ModInitializer {

    public static final String MODID = "polybars";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Action Bar Multiplexer & Polymer Resource Pack...");

        PolymerResourcePackUtils.addModAssets(MODID);
        PolymerResourcePackUtils.markAsRequired();

        DynamicFontGenerator.register();

        PredictionEmulationHandler.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ActionBarMultiplexer.getInstance().tick(server);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.player != null) {
                ActionBarMultiplexer.getInstance().removeSession(handler.player.getUUID());
            }
        });

        LOGGER.info("Action Bar Multiplexer initialized successfully.");

        Identifier healthHolderId = Identifier.fromNamespaceAndPath(MODID, "health");
        PolyBarsApi.registerBarHolder(healthHolderId, PolyBarSide.LEFT, 100);

        PolyBarsApi.registerBar(healthHolderId, new DefaultHealthBar(
                Identifier.fromNamespaceAndPath(MODID, "health"),
                Identifier.fromNamespaceAndPath(MODID, "textures/bars/heart/"),
                10
        ));


        Identifier foodHolderId = Identifier.fromNamespaceAndPath(MODID, "food");
        PolyBarsApi.registerBarHolder(foodHolderId, PolyBarSide.RIGHT, 100);
        PolyBarsApi.registerBar(foodHolderId, new DefaultHungerBar(
                Identifier.fromNamespaceAndPath(MODID, "food"),
                Identifier.fromNamespaceAndPath(MODID, "textures/bars/food"),
                10
        ));
        PolyBarsApi.registerBar(foodHolderId, new DefaultVehicleHealthBar(
                Identifier.fromNamespaceAndPath(MODID, "vehicle_health"),
                Identifier.fromNamespaceAndPath(MODID, "textures/bars/heart/vehicle"),
                20
        ));


        Identifier armorHolderId = Identifier.fromNamespaceAndPath(MODID, "armor");
        PolyBarsApi.registerBarHolder(armorHolderId, PolyBarSide.LEFT, 90);
        PolyBarsApi.registerBar(armorHolderId, new DefaultArmorBar(
                Identifier.fromNamespaceAndPath(MODID, "armor"),
                Identifier.fromNamespaceAndPath(MODID, "textures/bars/armor"),
                10
        ));

        Identifier airHolderId = Identifier.fromNamespaceAndPath(MODID, "air");
        PolyBarsApi.registerBarHolder(airHolderId, PolyBarSide.RIGHT, 90);
        PolyBarsApi.registerBar(airHolderId, new DefaultAirBar(
                Identifier.fromNamespaceAndPath(MODID, "air"),
                Identifier.fromNamespaceAndPath(MODID, "textures/bars/air"),
                10
        ));

        LOGGER.info("Initialized default bars");

    }
}
