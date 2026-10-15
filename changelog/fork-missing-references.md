# Fork: missing placeholders / triggers don't drop popups

A placeholder or trigger class that another plugin registers only on some servers used to throw,
so the whole popup failed to load and its fonts/images never reached the resource pack.
Every glyph allocated after it shifted, so each server produced a different pack.

Now an unknown placeholder resolves to an empty stand-in (`0` / `""` / `false`, typed by how it is used;
in a condition it takes the other side's type) and an unknown trigger class never fires.
Each one is logged once per reload: `This placeholder not found, using an empty stand-in: <name>`.

A malformed literal such as `''` still fails as before (the string-literal pattern requires at least one character).
