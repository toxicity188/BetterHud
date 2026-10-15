# Fork: per-element animation engine

This fork replaces the layout-level `animations` block with a per-element animation engine.
Everything below works in both popups and HUDs.

## Where it goes

```yaml
my_layout:
  animations:            # layout-level default (optional, inherited by elements without their own)
    type: play_once
    ...
  images:
    panel:
      name: panel
      x: -120
      y: -70
      pivot: center      # center | top-left | top | top-right | left | right | bottom-left | bottom | bottom-right
      animations:        # element-level, overrides the layout default
        ...
```

## Two ways to describe a track

### keyframes (preferred)

```yaml
animations:
  type: play_once        # play_once | loop
  duration: 20           # optional; defaults to last keyframe t + 1
  opacity-step: 0.125    # quantization, see below
  scale-step: 0.05
  keyframes:
    - { t: 0,  visible: false }
    - { t: 6,  visible: true, x: -30, scale: 0.5, opacity: 0 }
    - { t: 12, x: 0, scale: 1.25, opacity: 1, easing: out_cubic }
    - { t: 16, scale: 1, easing: out_cubic }
```

Channels: `x`, `y`, `opacity`, `scale`, `visible`. Each channel interpolates independently between the
keyframes that set it; before the first it holds the first value, after the last it holds the last.
`easing` on a keyframe shapes the segment *arriving* at it: `linear`, `step`, `in_quad`, `out_quad`,
`in_out_quad`, `in_cubic`, `out_cubic`, `in_out_cubic`, `out_back`, `out_elastic`, `out_bounce`, `in_out_sine`.
`visible` is always a step.

### equations (legacy, still supported)

```yaml
animations:
  type: loop
  duration: 24
  x-equation: "0"
  y-equation: "0"
  opacity-equation: "0.5+0.5*sin(t*pi/12)"
  scale-equation: "1"          # new
  visible-equation: "1"        # new, >= 0.5 means visible
```

## Cost model (why quantization exists)

The HUD is rendered as font glyphs, so animation states are baked at pack time:

| channel | cost per distinct state |
|---|---|
| `x` | free (negative spaces at runtime) |
| `y` | image: one bitmap provider line; text: one full font json |
| `opacity` | one shader id (shared across elements with the same gui/layer/outline/opacity, hard limit 1023) plus the y cost |
| `scale` | baked into glyph height: image one provider line, text one full font json |
| `visible: false` | nothing is baked |

Frames are quantized with `opacity-step` (default 0.125) and `scale-step` (default 0.05), then identical frames
collapse into one baked state. Text scale steps should be coarse (`scale-step: 0.25`) unless the font is small.

After every reload the plugin logs:

```
Shader ids used: 36 / 1023
Resource pack: 2468 files, 10.06MB total, 177 font json (3.90MB)
```

## Internals

- `animation/AnimationTrack` — frames → distinct states → `Animated<T>` picked by `t` (`LOOP` wraps, `PLAY_ONCE` clamps).
- `renderer/bake/ElementBakery` — one baking path for images / texts / heads shared by `PopupImpl` and `HudImpl`
  (both implement `ElementHost`). Scale is baked into the font `height`, pivot offsets into ascent and x.
- `renderer/bake/AnimatedLayout` — one per layout instance; every element bakes only its own distinct states and
  chooses its own frame at render time.
- Unit tests: `dist/src/test/kotlin/kr/toxicity/hud/`.
