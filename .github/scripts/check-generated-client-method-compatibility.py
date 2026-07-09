#!/usr/bin/env python3
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
"""Check generated Fineract client API method-name compatibility."""

import argparse
import os
import re
import sys
from collections import OrderedDict
from pathlib import Path


CLIENT_API_DIRECTORIES = OrderedDict(
    [
        (
            "fineract-client",
            Path("fineract-client/build/generated/java/src/main/java/org/apache/fineract/client/services"),
        ),
        (
            "fineract-client-feign",
            Path("fineract-client-feign/build/generated/java/src/main/java/org/apache/fineract/client/feign/services"),
        ),
    ]
)

INTERFACE_PATTERN = re.compile(r"\binterface\s+([A-Za-z_$][\w$]*)\b")
METHOD_PATTERN = re.compile(r"\b([A-Za-z_$][\w$]*)\s*\(")
TYPE_DECLARATION_PATTERN = re.compile(r"\b(class|interface|enum|record)\b")


def strip_comments_and_literals(source):
    result = []
    i = 0
    while i < len(source):
        if source.startswith("//", i):
            i += 2
            while i < len(source) and source[i] != "\n":
                i += 1
            if i < len(source):
                result.append("\n")
                i += 1
            continue

        if source.startswith("/*", i):
            i += 2
            while i < len(source) and not source.startswith("*/", i):
                if source[i] == "\n":
                    result.append("\n")
                i += 1
            i += 2
            continue

        if source.startswith('"""', i):
            result.append('""')
            i += 3
            while i < len(source) and not source.startswith('"""', i):
                if source[i] == "\n":
                    result.append("\n")
                i += 1
            i += 3
            continue

        if source[i] == '"':
            result.append('""')
            i += 1
            escaped = False
            while i < len(source):
                char = source[i]
                i += 1
                if escaped:
                    escaped = False
                elif char == "\\":
                    escaped = True
                elif char == '"':
                    break
            continue

        if source[i] == "'":
            result.append("''")
            i += 1
            escaped = False
            while i < len(source):
                char = source[i]
                i += 1
                if escaped:
                    escaped = False
                elif char == "\\":
                    escaped = True
                elif char == "'":
                    break
            continue

        result.append(source[i])
        i += 1

    return "".join(result)


def strip_annotations(source):
    result = []
    i = 0
    while i < len(source):
        if source[i] != "@":
            result.append(source[i])
            i += 1
            continue

        i += 1
        while i < len(source) and (source[i].isalnum() or source[i] in "_$."):
            i += 1
        while i < len(source) and source[i].isspace():
            i += 1

        if i < len(source) and source[i] == "(":
            depth = 1
            i += 1
            while i < len(source) and depth > 0:
                if source[i] == "(":
                    depth += 1
                elif source[i] == ")":
                    depth -= 1
                i += 1

        result.append(" ")

    return "".join(result)


def method_name_from_member(member):
    compact = " ".join(member.split())
    if not compact or "(" not in compact:
        return None
    if TYPE_DECLARATION_PATTERN.search(compact):
        return None

    match = METHOD_PATTERN.search(compact)
    if not match:
        return None
    return match.group(1)


def extract_api_methods(java_file):
    source = java_file.read_text(encoding="utf-8")
    source = strip_annotations(strip_comments_and_literals(source))

    interface_match = INTERFACE_PATTERN.search(source)
    if not interface_match:
        return set()

    body_start = source.find("{", interface_match.end())
    if body_start < 0:
        return set()

    methods = set()
    depth = 1
    member = []
    i = body_start + 1
    while i < len(source) and depth > 0:
        char = source[i]
        if depth == 1:
            if char == "{":
                name = method_name_from_member("".join(member))
                if name:
                    methods.add(name)
                member = []
                depth += 1
            elif char == ";":
                name = method_name_from_member("".join(member))
                if name:
                    methods.add(name)
                member = []
            elif char == "}":
                depth -= 1
                member = []
            else:
                member.append(char)
        else:
            if char == "{":
                depth += 1
            elif char == "}":
                depth -= 1
        i += 1

    return methods


def collect_api_surface(source_root):
    if not source_root.is_dir():
        raise FileNotFoundError(f"Generated API directory does not exist: {source_root}")

    surface = OrderedDict()
    for java_file in sorted(source_root.rglob("*Api.java")):
        relative_path = java_file.relative_to(source_root).as_posix()
        methods = extract_api_methods(java_file)
        if methods:
            surface[relative_path] = methods
    return surface


def compare_surfaces(baseline_root, current_root):
    violations = []
    for client_name, api_directory in CLIENT_API_DIRECTORIES.items():
        baseline_surface = collect_api_surface(baseline_root / api_directory)
        current_surface = collect_api_surface(current_root / api_directory)

        for api_file, baseline_methods in baseline_surface.items():
            current_methods = current_surface.get(api_file, set())
            missing_methods = sorted(baseline_methods - current_methods)
            if missing_methods:
                violations.append((client_name, api_file, missing_methods))

    return violations


def markdown_methods(methods):
    rendered = ", ".join(f"`{method}`" for method in methods[:20])
    if len(methods) > 20:
        rendered += f" +{len(methods) - 20} more"
    return rendered


def build_report(violations):
    lines = ["## Generated Client API Method Compatibility", ""]
    if not violations:
        lines.append("No generated API method names were removed from `fineract-client` or `fineract-client-feign`.")
        return "\n".join(lines) + "\n"

    lines.extend(
        [
            "The generated client API method names are not backward compatible.",
            "",
            "Existing generated method names that disappear should be preserved with `alternativeOperationId`.",
            "",
            "| Client | API | Removed method names |",
            "|--------|-----|----------------------|",
        ]
    )

    for client_name, api_file, missing_methods in violations:
        lines.append(f"| `{client_name}` | `{api_file}` | {markdown_methods(missing_methods)} |")

    lines.extend(
        [
            "",
            f"**Total: {sum(len(methods) for _, _, methods in violations)} removed method names across {len(violations)} API files.**",
        ]
    )
    return "\n".join(lines) + "\n"


def append_step_summary(report):
    summary_file = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_file:
        with open(summary_file, "a", encoding="utf-8") as output:
            output.write(report)


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--baseline-root", required=True, type=Path)
    parser.add_argument("--current-root", required=True, type=Path)
    parser.add_argument("--report-file", required=True, type=Path)
    return parser.parse_args()


def main():
    args = parse_args()
    violations = compare_surfaces(args.baseline_root, args.current_root)
    report = build_report(violations)

    args.report_file.write_text(report, encoding="utf-8")
    append_step_summary(report)
    print(report)

    if violations:
        print(
            "::error::Generated client API method names were removed. "
            "Add alternativeOperationId entries to preserve backward-compatible methods."
        )
        return 1

    return 0


if __name__ == "__main__":
    sys.exit(main())
