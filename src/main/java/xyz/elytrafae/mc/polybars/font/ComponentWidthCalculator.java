package xyz.elytrafae.mc.polybars.font;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ComponentWidthCalculator {

    private record CharWithStyle(char ch, Identifier fontId, boolean isBold) {}

    public static int calculateWidth(Component component) {
        return calculateWidth(component, true);
    }

    public static int calculateWidth(Component component, boolean removeTrailingSpace) {
        if (component == null) return 0;

        List<CharWithStyle> chars = new ArrayList<>();
        collectChars(component, null, chars);
        if (chars.isEmpty()) return 0;

        int totalWidth = 0;
        boolean lastWasNonSpace = false;

        for (CharWithStyle cf : chars) {
            int spaceAdv = getSpaceAdvance(cf.ch);
            if (spaceAdv != 0) {
                totalWidth += spaceAdv;
                lastWasNonSpace = false;
            } else {
                int baseWidth = GlyphWidthRegistry.getWidth(cf.fontId(), cf.ch());
                if (cf.isBold()) {
                    baseWidth += 1;
                }
                totalWidth += baseWidth + 1;
                lastWasNonSpace = true;
            }
        }

        if (lastWasNonSpace && removeTrailingSpace) {
            totalWidth -= 1;
        }

        return totalWidth;
    }

    private static void collectChars(Component comp, Style parentStyle, List<CharWithStyle> list) {
        if (comp == null) return;

        Style compStyle = comp.getStyle();
        Style currentStyle = parentStyle == null ? compStyle : (compStyle != null ? compStyle.applyTo(parentStyle) : parentStyle);

        Identifier currentFontId = null;
        if (currentStyle != null && currentStyle.getFont() instanceof FontDescription.Resource resFont) {
            currentFontId = resFont.id();
        }
        boolean isBold = currentStyle != null && currentStyle.isBold();

        String text = getLiteralText(comp);
        if (text != null && !text.isEmpty()) {
            for (char ch : text.toCharArray()) {
                list.add(new CharWithStyle(ch, currentFontId, isBold));
            }
        }

        for (Component sibling : comp.getSiblings()) {
            collectChars(sibling, currentStyle, list);
        }
    }

    public static int getSpaceAdvance(char ch) {
        switch (ch) {
            case '\uF880': return -128;
            case '\uF840': return -64;
            case '\uF820': return -32;
            case '\uF810': return -16;
            case '\uF808': return -8;
            case '\uF804': return -4;
            case '\uF802': return -2;
            case '\uF801': return -1;

            case '\uF81B': return 128;
            case '\uF81A': return 64;
            case '\uF80F': return 32;
            case '\uF80E': return 16;
            case '\uF80D': return 8;
            case '\uF80C': return 4;
            case '\uF80B': return 2;
            case '\uF80A': return 1;

            default: return 0;
        }
    }

    public static boolean endsWithNonSpace(Component component) {
        if (component == null) return false;
        List<CharWithStyle> chars = new ArrayList<>();
        collectChars(component, null, chars);
        if (chars.isEmpty()) return false;
        return getSpaceAdvance(chars.get(chars.size() - 1).ch()) == 0;
    }

    private static String getLiteralText(Component comp) {
        if (comp.getContents() instanceof net.minecraft.network.chat.contents.PlainTextContents plain) {
            return plain.text();
        }
        return comp.getString();
    }
}
