"""Subset and trim Inter down to what Cockpit actually renders.

The shipped Inter variable font is ~856 KB and was ~31% of the release APK's
compressed payload. Cockpit is an English-only launcher whose dynamic text (app
labels, track titles, device names) is overwhelmingly ASCII, and Android falls
back to the platform font for any glyph this family lacks, so a Latin subset is
safe: other scripts simply render in the system font.

Two trims, both invisible at runtime:

* glyphs - only the characters the UI can draw (see ``charset``).
* axes   - ``opsz`` is pinned to its default (nothing in the app sets it) and
           ``wght`` is limited to 400..800, exactly the weights ui/Typography.kt
           registers, so every instantiated weight is unchanged.

Run from the repository root:

    python tools/subset_font.py                 # rewrites res/font/inter.ttf
    python tools/subset_font.py preview.ttf     # writes elsewhere, for review

It reports the saving and fails loudly if a needed glyph, weight or layout
feature would be lost. Restore the original with:

    git show HEAD:app/src/main/res/font/inter.ttf > app/src/main/res/font/inter.ttf
"""

from pathlib import Path
import sys
import xml.etree.ElementTree as ET

from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

ROOT = Path(__file__).resolve().parent.parent
FONT = ROOT / "app/src/main/res/font/inter.ttf"
STRINGS = ROOT / "app/src/main/res/values/strings.xml"

# Weights registered by ui/Typography.kt; the retained axis must cover them all.
WEIGHTS = (400, 500, 600, 700, 800)

# Keep only what the UI asks for: tnum (tabular figures for the clock) and cv11 (the
# single-storey "a"), plus the features text shaping needs. Everything else — aalt,
# ss01-08, cv01-13, salt, zero, fractions — would just be bytes.
REQUIRED_FEATURES = ("tnum", "cv11")
KEPT_FEATURES = ["tnum", "cv11", "kern", "liga", "clig", "calt", "ccmp", "locl", "mark", "mkmk"]

# Axes pinned to their own default, so rendering cannot change.
PINNED_AXES = ("opsz",)


def charset() -> set[str]:
    chars = {chr(code) for code in range(0x20, 0x7F)}    # ASCII printable
    chars |= {chr(code) for code in range(0xA0, 0x100)}  # Latin-1: é ü ñ · °
    chars |= set("€–—‘’“”•…")                             # common metadata punctuation
    for node in ET.fromstring(STRINGS.read_text(encoding="utf-8")).iter("string"):
        chars |= set("".join(node.itertext()))
    return chars


def features(font: TTFont) -> set[str]:
    if "GSUB" not in font:
        return set()
    return {record.FeatureTag for record in font["GSUB"].table.FeatureList.FeatureRecord}


def main() -> None:
    output = Path(sys.argv[1]) if len(sys.argv) > 1 else FONT
    before = FONT.stat().st_size
    font = TTFont(FONT)

    # Ask only for glyphs this font actually has; anything else already relies
    # on the platform fallback and must not abort the run.
    available = {chr(code) for code in font.getBestCmap()}
    wanted = charset() & available
    absent = sorted(charset() - available)
    before_features = features(font)

    options = subset.Options()
    options.layout_features = KEPT_FEATURES
    options.name_IDs = ["*"]         # keep licence + family metadata
    options.notdef_outline = True
    options.recalc_bounds = True

    subsetter = subset.Subsetter(options=options)
    subsetter.populate(text="".join(sorted(wanted)))
    subsetter.subset(font)

    kept_features = features(font)
    missing_required = [tag for tag in REQUIRED_FEATURES if tag not in kept_features]
    assert not missing_required, f"theme features missing: {missing_required}"
    # The subsetter prunes features that no retained glyph uses; that cannot
    # change how a retained glyph renders, so it is reported rather than fatal.
    dropped = sorted(before_features - kept_features)

    # Trim the variable axes without changing any value the app can request.
    axes = {axis.axisTag: axis for axis in font["fvar"].axes}
    pinned = {tag: axes[tag].defaultValue for tag in PINNED_AXES if tag in axes}
    pinned["wght"] = (float(min(WEIGHTS)), float(max(WEIGHTS)))
    instancer.instantiateVariableFont(font, pinned, inplace=True)

    remaining = {axis.axisTag: axis for axis in font["fvar"].axes}
    low, high = remaining["wght"].minValue, remaining["wght"].maxValue
    assert low <= min(WEIGHTS) and high >= max(WEIGHTS), f"wght narrowed past a used weight: {low}..{high}"
    assert all(tag not in remaining for tag in pinned if tag != "wght"), "pinned axis still present"

    encoded = {chr(code) for code in font.getBestCmap()}
    missing = sorted(wanted - encoded)
    assert not missing, f"glyphs missing after trimming: {missing}"

    font.save(output)

    after = output.stat().st_size
    print(f"glyphs:   {len(encoded)} codepoints, {font['maxp'].numGlyphs} glyphs")
    print(f"axes:     wght {low:.0f}..{high:.0f}, others pinned")
    print(f"features: {' '.join(sorted(kept_features))}")
    if dropped:
        print(f"pruned:   {' '.join(dropped)} (unused by any kept glyph)")
    if absent:
        print(f"fallback: {len(absent)} requested chars not in Inter (system font)")
    print(f"size:     {before:,} -> {after:,} bytes ({(1 - after / before) * 100:.1f}% smaller)")
    print(f"wrote:    {output.relative_to(ROOT) if output.is_relative_to(ROOT) else output}")


if __name__ == "__main__":
    main()
