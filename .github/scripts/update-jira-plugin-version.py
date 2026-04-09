#!/usr/bin/env python3
"""Update ch.ivyteam.maven:jira-plugin version in Maven pom.xml files."""
import re
import sys

if len(sys.argv) < 3:
    print(f"Usage: {sys.argv[0]} <version> <pom.xml> [<pom.xml> ...]", file=sys.stderr)
    sys.exit(1)

version = sys.argv[1]
pattern = re.compile(
    r'(<groupId>ch[.]ivyteam[.]maven</groupId>\s*<artifactId>jira-plugin</artifactId>\s*<version>)[^<]*(</version>)',
    re.DOTALL
)
for fname in sys.argv[2:]:
    try:
        with open(fname) as f:
            content = f.read()
        new_content = pattern.sub(lambda m: m.group(1) + version + m.group(2), content)
        with open(fname, 'w') as f:
            f.write(new_content)
    except (FileNotFoundError, PermissionError) as e:
        print(f"Error processing {fname}: {e}", file=sys.stderr)
        sys.exit(1)
