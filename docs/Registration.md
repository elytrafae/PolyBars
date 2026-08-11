# Bar and Holder Registration

## Short example

```java
PolyBar myBar = new MyCustomBar(...); // See the Bar page
Identifier myBarHolderId = Identifier.fromNamespaceAndPath(MODID, "mybar");
PolyBarsApi.registerBarHolder(myBarHolderId, PolyBarSide.RIGHT, 95);
PolyBarsApi.registerBar(myBarHolderId, myBar);
```

---

## Longer explanations

### Holders vs Bars
In short, a Holder is an abstract holder of bars that defines the position of bars inside it.

A holder can have several bars, but only the highest priority one that should be visible will be drawn.

In short, holder defines bar position, bar defines the look.

Holder Priority: How close to the bottom of the screen will it be?

Bar Priority: Bars with lower priority will not be drawn if your bars says it should be drawn.

---

### Preferred Side VS Assigned Side
When creating a Bar Holder, you tell it if you want it to be on the left or right. However, what happens if too many mod developers keep adding bars to the same side?

PolyBars has a failsafe for this, where it shifts the lowest priority bars on the unstable side to the other side at initialization time.

If you want to add code that needs to know which side the bar is on, check the ASSIGNED side, not PREFERRED side!

---

### Overriding vanilla bars
If you wish to override vanilla bars, you can just register a bar with a higher priority in its bar holder (aka, last line of the example is enough, you don't need to register a holder).

#### Vanilla Holders
- Health: `polybars:health` (Priority: 100)
- Food/Mount Health: `polybars:food` (Priority: 100)
- Armor: `polybars:armor` (Priority: 90)
- Air: `polybars:air` (Priority: 90)

#### Vanilla Bars
- Health: `polybars:health` (Priority: 10)
- Food: `polybars:food` (Priority: 10)
- Mount Health: `polybars:vehicle_health` (Priority: 20)
- Armor: `polybars:armor` (Priority: 10)
- Air: `polybars:air` (Priority: 10)

---

### Note on Cross-Mod Holders
You can register a duplicate holder with no issues. It will show a warning, but that's about it. You can just use a registerBarHolder call to make sure a holder with a given ID exists.