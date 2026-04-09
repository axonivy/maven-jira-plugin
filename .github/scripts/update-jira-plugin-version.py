#!/usr/bin/env python3
"""Update ch.ivyteam.maven:jira-plugin version in Maven pom.xml files."""
import re
import sys

version = sys.argv[1]
pattern = re.compile(
    r'(<groupId>ch[.]ivyteam[.]maven</groupId>\s*<artifactId>jira-plugin</artifactId>\s*<version>)[^<]*(</version>)',
    re.DOTALL
)
for fname in sys.argv[2:]:
    with open(fname) as f:
        content = f.read()
    new_content = pattern.sub(lambda m: m.group(1) + version + m.group(2), content)
    with open(fname, 'w') as f:
        f.write(new_content)
