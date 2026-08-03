package xyz.elytrafae.mc.polybars.font;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlyphWidthRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("PolyBarsGlyphRegistry");
    private static final Map<Identifier, Map<Character, Integer>> FONT_GLYPH_WIDTHS = new ConcurrentHashMap<>();

    public static final Identifier DEFAULT_FONT = Identifier.fromNamespaceAndPath("minecraft", "default");

    static {
        Map<Character, Integer> defaultMap = FONT_GLYPH_WIDTHS.computeIfAbsent(DEFAULT_FONT, f -> new ConcurrentHashMap<>());
        for (char i = 32; i < 127; i++) {
            defaultMap.put(i, 5);
        }

        defaultMap.put(' ', 3);
        defaultMap.put('!', 1);
        defaultMap.put('"', 3);
        defaultMap.put('\'', 1);
        defaultMap.put('(', 3);
        defaultMap.put(')', 3);
        defaultMap.put('*', 3);
        defaultMap.put(',', 1);
        defaultMap.put('.', 1);
        defaultMap.put(':', 1);
        defaultMap.put(';', 1);
        defaultMap.put('<', 4);
        defaultMap.put('>', 4);
        defaultMap.put('@', 6);
        defaultMap.put('I', 3);
        defaultMap.put('[', 3);
        defaultMap.put(']', 3);
        defaultMap.put('`', 2);
        defaultMap.put('f', 4);
        defaultMap.put('i', 1);
        defaultMap.put('k', 4);
        defaultMap.put('l', 2);
        defaultMap.put('t', 3);
        defaultMap.put('{', 4);
        defaultMap.put('|', 1);
        defaultMap.put('}', 4);
        defaultMap.put('~', 6);
    }

    public static void registerWidth(Identifier fontId, char character, int width) {
        if (fontId == null) return;
        FONT_GLYPH_WIDTHS.computeIfAbsent(fontId, f -> new ConcurrentHashMap<>()).put(character, width);
    }

    public static int getWidth(Identifier fontId, char character) {
        if (fontId != null) {
            Map<Character, Integer> fontMap = FONT_GLYPH_WIDTHS.get(fontId);
            if (fontMap != null && fontMap.containsKey(character)) {
                return fontMap.get(character);
            }
        }

        Map<Character, Integer> defaultMap = FONT_GLYPH_WIDTHS.get(DEFAULT_FONT);
        if (defaultMap != null && defaultMap.containsKey(character)) {
            return defaultMap.get(character);
        }

        for (Map<Character, Integer> fontMap : FONT_GLYPH_WIDTHS.values()) {
            if (fontMap.containsKey(character)) {
                return fontMap.get(character);
            }
        }

        if (character >= '\uE000' && character <= '\uF8FF') {
            return 9;
        }

        return 5;
    }

    public static void clear() {
        FONT_GLYPH_WIDTHS.clear();
    }
}
