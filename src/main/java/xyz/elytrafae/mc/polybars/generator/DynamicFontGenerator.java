package xyz.elytrafae.mc.polybars.generator;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.elytrafae.mc.polybars.PolyBars;
import xyz.elytrafae.mc.polybars.api.*;
import xyz.elytrafae.mc.polybars.font.GlyphWidthRegistry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DynamicFontGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger("PolyBarsFontGenerator");
    private static final ExecutorService ASSET_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "PolyBars-FontGenerator-Thread");
        thread.setDaemon(true);
        return thread;
    });

    public static void register() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(builder -> {
            LOGGER.info("Polymer resource pack creation triggered. Starting dynamic font generation on separate thread...");
            CompletableFuture.runAsync(() -> generateFontResources(builder), ASSET_EXECUTOR).join();
        });
    }

    public static void generateFontResources(ResourcePackBuilder builder) {
        //builder.addStringData("assets/polybars/font/space.json", buildSpaceFontJson());

        Map<Integer, List<PolyBarHolder>> rowsMap = PolyBarRegistry.getHoldersByRowOrdered();
        if (rowsMap.isEmpty()) {
            LOGGER.info("No custom PolyBarHolders registered. Skipping dynamic font generation.");
            return;
        }

        char nextUnicodeChar = '\uE100';

        for (Map.Entry<Integer, List<PolyBarHolder>> entry : rowsMap.entrySet()) {
            int rowIndex = entry.getKey();
            List<PolyBarHolder> holdersInRow = entry.getValue();

            List<PolyBar> barsInRow = new ArrayList<>();
            for (PolyBarHolder holder : holdersInRow) {
                barsInRow.addAll(holder.getBars());
            }

            LOGGER.info("Generating per-bar provider font for Row {} with {} registered holder(s) and {} bar(s)...",
                    rowIndex, holdersInRow.size(), barsInRow.size());

            Identifier rowFontId = Identifier.fromNamespaceAndPath("polybars", "row_" + rowIndex);
            int baseAscent = -8;
            int ascent = baseAscent + (rowIndex * 10);
            List<Map<String, Object>> providerJsonList = new ArrayList<>();
            int providerIndex = 0;

            for (PolyBar bar : barsInRow) {
                int targetHeight = bar.getTargetHeight() > 0 ? bar.getTargetHeight() : 9;
                List<BufferedImage> barSlices = new ArrayList<>();
                StringBuilder barCharsBuilder = new StringBuilder();
                int maxSliceRawHeight = 9;

                List<List<Character>> barGlyphsPerTexture = new ArrayList<>();

                for (PolyBarTexture barTex : bar.getTextures()) {
                    BufferedImage rawImage = loadBarTexture(builder, bar, barTex);
                    List<BufferedImage> slices = sliceBarTexture(barTex, rawImage);

                    List<Character> texChars = new ArrayList<>();
                    for (BufferedImage slice : slices) {
                        barSlices.add(slice);
                        char unicodeChar = nextUnicodeChar++;
                        barCharsBuilder.append(unicodeChar);
                        texChars.add(unicodeChar);

                        int guiWidth = slice.getWidth();
                        if (slice.getHeight() > 0 && targetHeight > 0) {
                            guiWidth = Math.max(1, (int) Math.round((double) slice.getWidth() * (double) targetHeight / (double) slice.getHeight()));
                        }

                        GlyphWidthRegistry.registerWidth(rowFontId, unicodeChar, guiWidth);
                        if (slice.getHeight() > maxSliceRawHeight) {
                            maxSliceRawHeight = slice.getHeight();
                        }
                    }
                    barGlyphsPerTexture.add(texChars);
                }

                bar.onGlyphsAssigned(barGlyphsPerTexture);

                if (barSlices.isEmpty()) {
                    continue;
                }

                int canvasRawHeight = maxSliceRawHeight * 5;
                int topPaddingRaw = maxSliceRawHeight * 2;

                int totalWidth = barSlices.stream().mapToInt(BufferedImage::getWidth).sum();
                BufferedImage atlasImage = new BufferedImage(totalWidth, canvasRawHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = atlasImage.createGraphics();

                int currentX = 0;
                for (BufferedImage slice : barSlices) {
                    g2d.drawImage(slice, currentX, topPaddingRaw, null);
                    currentX += slice.getWidth();
                }

                // Add 1-pixel high line at top and bottom of canvas with 1/255 opacity so
                // MC and Optifine does not trim transparent margins
                g2d.setColor(new Color(255, 255, 255, 1));
                g2d.fillRect(0, 0, totalWidth, 1);
                g2d.fillRect(0, canvasRawHeight - 1, totalWidth, 1);

                g2d.dispose();

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(atlasImage, "png", baos);
                    byte[] pngBytes = baos.toByteArray();

                    String pngPath = "assets/polybars/textures/font/row_" + rowIndex + "_p" + providerIndex + ".png";
                    builder.addData(pngPath, pngBytes);

                    int fontHeight = targetHeight * 5;

                    Map<String, Object> providerSpec = new LinkedHashMap<>();
                    providerSpec.put("file", "polybars:font/row_" + rowIndex + "_p" + providerIndex + ".png");
                    providerSpec.put("ascent", ascent);
                    providerSpec.put("height", fontHeight);
                    providerSpec.put("chars", barCharsBuilder.toString());
                    providerJsonList.add(providerSpec);

                    LOGGER.info("Generated Atlas Provider p{} for PolyBar '{}' in Row {} (Target Height: {}px, Canvas: {}x{}px, Ascent: {})",
                            providerIndex, bar.getId(), rowIndex, targetHeight, totalWidth, canvasRawHeight, ascent);

                    providerIndex++;
                } catch (Exception e) {
                    LOGGER.error("Failed to generate atlas provider p{} for PolyBar '{}' in Row {}", providerIndex, bar.getId(), rowIndex, e);
                }
            }

            if (providerJsonList.isEmpty()) {
                continue;
            }

            try {
                String fontJson = buildMultiProviderFontJson(providerJsonList);
                String jsonPath = "assets/polybars/font/row_" + rowIndex + ".json";
                builder.addStringData(jsonPath, fontJson);
                LOGGER.info("Successfully registered font 'polybars:row_{}' with {} per-bar provider(s)",
                        rowIndex, providerJsonList.size());
            } catch (Exception e) {
                LOGGER.error("Failed to register font JSON for Row {}", rowIndex, e);
            }
        }
    }

    private static BufferedImage loadBarTexture(ResourcePackBuilder builder, PolyBar bar, PolyBarTexture barTex) {
        Identifier tex = barTex.textureId();
        String pathStr = tex.getPath();

        if (!pathStr.startsWith("textures/")) {
            pathStr = "textures/" + pathStr;
        }
        if (!pathStr.endsWith(".png")) {
            pathStr = pathStr + ".png";
        }

        String fullAssetPath = "assets/" + tex.getNamespace() + "/" + pathStr;

        byte[] data = builder.getDataOrSource(fullAssetPath);
        if (data != null && data.length > 0) {
            try {
                return ImageIO.read(new ByteArrayInputStream(data));
            } catch (Exception ignored) {}
        }

        try (InputStream is = PolyBars.class.getClassLoader().getResourceAsStream(fullAssetPath)) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception ignored) {}

        LOGGER.warn("Could not load texture asset '{}' for PolyBar '{}'. Generating fallback texture.", fullAssetPath, bar.getId());
        return createFallbackTexture(barTex.slicesCount() * 8, 8);
    }

    private static List<BufferedImage> sliceBarTexture(PolyBarTexture barTex, BufferedImage image) {
        int count = barTex.slicesCount();
        List<BufferedImage> slices = new ArrayList<>();
        int h = image.getHeight();
        int sliceWidth = image.getWidth() / count;

        for (int i=0; i < count; i++) {
            int x = barTex.mode().equals(PolyTextureSliceMode.INDIVIDUAL) ? i * sliceWidth : 0;
            int w = barTex.mode().equals(PolyTextureSliceMode.INDIVIDUAL) ? sliceWidth : sliceWidth * (i+1);
            slices.add(image.getSubimage(x, 0, w, h));
        }

        return slices;
    }

    public static String buildSpaceFontJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"providers\": [\n");
        json.append("    {\n");
        json.append("      \"type\": \"space\",\n");
        json.append("      \"advances\": {\n");
        json.append("        \"\\uF801\": -1, \"\\uF802\": -2, \"\\uF804\": -4, \"\\uF808\": -8, \"\\uF810\": -16, \"\\uF820\": -32, \"\\uF840\": -64, \"\\uF880\": -128,\n");
        json.append("        \"\\uF80A\": 1, \"\\uF80B\": 2, \"\\uF80C\": 4, \"\\uF80D\": 8, \"\\uF80E\": 16, \"\\uF80F\": 32, \"\\uF81A\": 64, \"\\uF81B\": 128\n");
        json.append("      }\n");
        json.append("    }\n");
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private static String buildMultiProviderFontJson(List<Map<String, Object>> providers) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"providers\": [\n");
        for (int i = 0; i < providers.size(); i++) {
            Map<String, Object> provider = providers.get(i);
            json.append("    {\n");
            json.append("      \"type\": \"bitmap\",\n");
            json.append("      \"file\": \"").append(provider.get("file")).append("\",\n");
            json.append("      \"ascent\": ").append(provider.get("ascent")).append(",\n");
            json.append("      \"height\": ").append(provider.get("height")).append(",\n");
            json.append("      \"chars\": [\n");
            json.append("        \"").append(escapeJson((String) provider.get("chars"))).append("\"\n");
            json.append("      ]\n");
            json.append("    }").append(i < providers.size() - 1 ? "," : "").append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private static String escapeJson(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '"' || c == '\\') {
                sb.append('\\').append(c);
            } else if (c >= 32 && c <= 126) {
                sb.append(c);
            } else {
                sb.append(String.format("\\u%04X", (int) c));
            }
        }
        return sb.toString();
    }

    private static BufferedImage createFallbackTexture(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return img;
    }


}
