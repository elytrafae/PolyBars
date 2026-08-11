# Creating Bars

To add custom HUD elements with PolyBars, you'll need to create a bar class. Your bar tells PolyBars what textures to use, how values map to visual components, and when to show up on screen.

---

## Class Hierarchy

PolyBars comes with a few base classes to make creating bars easier, depending on what kind of bar you're building:

```
PolyBar (Interface)
 └── AbstractPolyBar (Abstract Class)
      ├── HealthStylePolyBar (Icon-based bars, like hunger or armor)
      ├── LayeredHealthStylePolyBar (Complex icon bars, like player health with absorption)
      └── ExperienceStylePolyBar (Continuous fill bars, like XP or mana)
```

---

## Base Classes

### 1. `PolyBar` (Interface)

This is the main interface for all bars in PolyBars. Every bar must implement these methods:

- `getId()`: Returns the unique identifier for the bar (for example `mymod:mana_bar`).
- `getHolder()`: Returns the parent `PolyBarHolder` containing this bar.
- `getTextures()`: Returns the list of `PolyBarTexture` assets and slice counts used by the bar.
- `getPriority()`: Priority for picking which bar to show inside a holder. Higher priority wins. (Default: `0`).
- `getTargetHeight()`: Height of the bar in GUI pixels. (Default: `9`).
- `shouldDraw(ServerPlayer player)`: Returns `true` if the bar should render for the player right now.
- `getBarComponent(ServerPlayer player)`: Builds and returns the Minecraft text `Component` to display on the action bar.
- `onGlyphsAssigned(List<List<Character>> glyphsPerTexture)`: Called automatically when PolyBars assigns unicode font glyphs to your texture slices.

---

### 2. `AbstractPolyBar`

This is the standard starting point if you want to write custom bar logic from scratch. It handles texture setup, glyph assignments, and holder references for you.

- Automatically hides the bar for creative and spectator players in `shouldDraw(player)`.
- Gives you helper methods to grab font glyphs for rendering:
  - `getSliceCharacter(textureIndex, sliceIndex)`
  - `getSliceComponent(textureIndex, sliceIndex)`: Returns a text component formatted with the correct font for that bar's row.

Use `AbstractPolyBar` if none of the pre-made style classes below fit what you need.

---

### 3. `ExperienceStylePolyBar`

Great for continuous progress or fill bars (like XP, mana, or stamina bars).

You pass it two textures:
1. **Background texture**: The empty bar frame.
2. **Fill texture**: The filled bar texture.

Make sure the width of the image divided by the slice count gives an integer result! Recommendation: 81 width, 27 slices (this makes every slice 3 pixels wide)

#### Methods to override
- `getMaxValue(ServerPlayer player)`: The maximum value of the bar.
- `getValue(ServerPlayer player)`: The player's current value.
- `getColor(ServerPlayer player)`: Optional RGB color tint (defaults to white, `0xFFFFFF`).

#### Example
```java
public class ManaBar extends ExperienceStylePolyBar {

    public ManaBar(Identifier id, Identifier bgTexture, Identifier fillTexture, int fillSlices, int priority) {
        super(id, bgTexture, fillTexture, fillSlices, priority);
    }

    @Override
    public double getMaxValue(ServerPlayer player) {
        return 100.0;
    }

    @Override
    public double getValue(ServerPlayer player) {
        return MyManaComponent.get(player).getMana();
    }

    @Override
    public int getColor(ServerPlayer player) {
        return 0x00AAFF; // Cyan tint
    }
}
```

---

### 4. `HealthStylePolyBar`

Great for simple icon bars, usually made of 10 slots (like hearts, armor badges, hunger shanks, or air bubbles).

It expects three textures:
1. Full icon texture
2. Half icon texture
3. Empty icon texture

#### Methods to override
- `getMaxValue(ServerPlayer player)`: Maximum capacity (for example `20.0` for 10 full icons).
- `getValue(ServerPlayer player)`: Current value.
- `getIconCount(ServerPlayer player)`: How many icon slots to draw (defaults to `10`).
- `getIconSpacing(ServerPlayer player)`: Pixel spacing between icons (defaults to `-2` to make icons overlap slightly).
- `getShownIconSliceIndex(ServerPlayer player)`: Which slice to draw (useful for variant states like normal vs poisoned hunger).
- `isOrderReversed(ServerPlayer player)`: Set to `true` to draw right-to-left (like hunger). Defaults to `false` (left-to-right).
- `getColor(ServerPlayer player)`: Optional RGB tint color (defaults to white, `0xFFFFFF`).

#### Example
```java
public class ShieldBar extends HealthStylePolyBar {

    public ShieldBar(Identifier id, Identifier baseTexture, int priority) {
        super(
            id,
            baseTexture.withSuffix("_full"),
            baseTexture.withSuffix("_half"),
            baseTexture.withSuffix("_empty"),
            1, // 1 slice per texture
            priority
        );
    }

    @Override
    public double getMaxValue(ServerPlayer player) {
        return 20.0;
    }

    @Override
    public double getValue(ServerPlayer player) {
        return MyShieldComponent.get(player).getShield();
    }
}
```

---

### 5. `LayeredHealthStylePolyBar`

Designed for multi-layered icon bars, like player health with absorption overlays or extra status effect colors.

#### Key methods to override
- `getLayerCount(ServerPlayer player)`: How many layers to draw.
- `getValue(ServerPlayer player, int layer)`: Current value for a given layer.
- `getMaxValue(ServerPlayer player, int layer)`: Maximum value for a given layer.
- `getFullTextureIndex(ServerPlayer player, int layer)`: Texture index for full icons on this layer.
- `getHalfTextureIndex(ServerPlayer player, int layer)`: Texture index for half icons on this layer.
- `getContainerTextureIndex(ServerPlayer player, int layer)`: Texture index for container background.
- `isTieredOverflowEnabled(ServerPlayer player, int layer)`: Whether extra health changes bar color instead of extending off screen.

---

## Defining Textures with `PolyBarTexture`

You tell PolyBars about your bar's textures using `PolyBarTexture`:

```java
PolyBarTexture texture = new PolyBarTexture(Identifier.fromNamespaceAndPath("mymod", "textures/gui/bar.png"), 10);
```

- **`textureId`**: Identifier pointing to your PNG image.
- **`slicesCount`**: How many horizontal slices to cut the texture into.

> Check [Limitations](Limitations.md) for rules on texture heights, widths, and transparency.

---

## Registering Your Bar

After making your bar class, register it to a holder using `PolyBarsApi`:

```java
PolyBar myBar = new ManaBar(...);
PolyBarsApi.registerBar(holderId, myBar);
```

For more info on holders, priorities, and overriding vanilla bars, check out [Bar and Holder Registration](Registration.md).
