package xyz.elytrafae.mc.polybars.font;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class SpaceBuilder {

    public static String getNegativeSpace(int pixels) {
        if (pixels <= 0) return "";
        StringBuilder sb = new StringBuilder();

        while (pixels >= 128) { sb.append('\uF880'); pixels -= 128; }
        while (pixels >= 64)  { sb.append('\uF840'); pixels -= 64;  }
        while (pixels >= 32)  { sb.append('\uF820'); pixels -= 32;  }
        while (pixels >= 16)  { sb.append('\uF810'); pixels -= 16;  }
        while (pixels >= 8)   { sb.append('\uF808'); pixels -= 8;   }
        while (pixels >= 4)   { sb.append('\uF804'); pixels -= 4;   }
        while (pixels >= 2)   { sb.append('\uF802'); pixels -= 2;   }
        while (pixels >= 1)   { sb.append('\uF801'); pixels -= 1;   }

        return sb.toString();
    }

    public static String getPositiveSpace(int pixels) {
        if (pixels <= 0) return "";
        StringBuilder sb = new StringBuilder();

        while (pixels >= 128) { sb.append('\uF81B'); pixels -= 128; }
        while (pixels >= 64)  { sb.append('\uF81A'); pixels -= 64;  }
        while (pixels >= 32)  { sb.append('\uF80F'); pixels -= 32;  }
        while (pixels >= 16)  { sb.append('\uF80E'); pixels -= 16;  }
        while (pixels >= 8)   { sb.append('\uF80D'); pixels -= 8;   }
        while (pixels >= 4)   { sb.append('\uF80C'); pixels -= 4;   }
        while (pixels >= 2)   { sb.append('\uF80B'); pixels -= 2;   }
        while (pixels >= 1)   { sb.append('\uF80A'); pixels -= 1;   }

        return sb.toString();
    }

    public static final Identifier SPACE_FONT = Identifier.fromNamespaceAndPath("polybars", "space");

    public static MutableComponent getNegativeSpaceComponent(int pixels) {
        String str = getNegativeSpace(pixels);
        if (str.isEmpty()) return Component.empty();
        MutableComponent comp = Component.literal(str);
        comp.withStyle(style -> style.withoutShadow().withFont(new FontDescription.Resource(SPACE_FONT)));
        return comp;
    }

    public static MutableComponent getPositiveSpaceComponent(int pixels) {
        String str = getPositiveSpace(pixels);
        if (str.isEmpty()) return Component.empty();
        MutableComponent comp = Component.literal(str);
        comp.withStyle(style -> style.withoutShadow().withFont(new FontDescription.Resource(SPACE_FONT)));
        return comp;
    }

    public static MutableComponent getSpaceComponent(int pixels) {
        if (pixels > 0) {
            return getPositiveSpaceComponent(pixels);
        }
        if (pixels < 0) {
            return getNegativeSpaceComponent(-pixels);
        }
        return Component.empty();
    }
}
