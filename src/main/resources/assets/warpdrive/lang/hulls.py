import re
import sys

from io import open


FORMAT_RE = re.compile("^# HULLS FORMAT: (.*?)$", re.MULTILINE)
TIER_RE = re.compile("^# HULLS TIER: ([a-zA-Z]+?) (.+?)$", re.MULTILINE)
COLOR_RE = re.compile("^# HULLS COLOR: ([a-zA-Z]+?) (.+?)$", re.MULTILINE)
SHAPE_RE = re.compile("^# HULLS SHAPE: ([a-zA-Z]+?) (.+?) ([^ ]*?)$", re.MULTILINE)


def main(lang):
    with open(f"{lang}.lang", "rt", encoding="utf-8") as fp:
        s = fp.read()
    begin = s[:s.find("# HULLS START\n")]
    end = s[s.find("# HULLS END\n"):]
    format = FORMAT_RE.search(s).group(1)
    with open(f"{lang}.lang", "wt", encoding="utf-8", newline='\n') as fp:
        fp.write(begin)
        fp.write("# HULLS START\n")
        fp.write("# WARNING! The content inside \"HULLS START\" and \"HULLS END\" is generated and should not be edited manually. Use hulls.py after editing the comments above for this.\n")
        for tier in TIER_RE.finditer(s):
            tier_name = tier.group(1)
            tier_translation = tier.group(2)
            for shape in SHAPE_RE.finditer(s):
                shape_name = shape.group(1)
                shape_translation = shape.group(2)
                shape_end = shape.group(3)
                for color in COLOR_RE.finditer(s):
                    color_name = color.group(1)
                    color_translation = color.group(2)
                    fp.write(f"tile.warpdrive.hull.{tier_name}.{shape_name}.{color_name}.name={format.format(color=color_translation, tier=tier_translation, shape=shape_translation, shape_end=shape_end)}\n")
                fp.write("\n")
            fp.write("\n")
        #fp.write("# HULLS END\n") # not needed, end already includes this
        fp.write(end)
            

if __name__ == "__main__":
    main(sys.argv[1])
