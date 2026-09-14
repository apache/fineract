#!/usr/bin/env python3
"""Insert/refresh CodeIntent's live-governance-view block in a PR description.

GitHub has no "pin comment to top" API for PRs/issues (only Discussions
support pinning) and comment order is fixed by creation time, so a separate
issue comment can't be guaranteed to stay above whatever a human posts next.
The PR description, on the other hand, always renders above every comment --
so this block lives there instead, delimited by HTML comment markers and
always re-inserted at the very top on every update.

Run history has to live inside the block itself: a stateless Actions job has
nowhere else to keep it, so each `start` call parses the existing table out
of the current body, prepends a new pending row for this run, and each
`finish` call (once the Checks card verdict is known) fills in that row's
result.
"""
import argparse
import json
import re
import subprocess
from datetime import datetime, timezone

MARK_START = "<!-- codeintent-live-status:start -->"
MARK_END = "<!-- codeintent-live-status:end -->"

ROW_RE = re.compile(
    r"^\|\s*(?P<when>[^|]+?)\s*\|\s*`(?P<sha>[^`]+)`\s*\|\s*(?P<result>[^|]+?)\s*\|\s*"
    r"\[live\]\((?P<live>[^)]+)\)\s*·\s*\[run\]\((?P<run>[^)]+)\)\s*\|\s*$"
)


def gh_api(*args: str) -> str:
    return subprocess.run(["gh", "api", *args], check=True, capture_output=True, text=True).stdout


def get_body(repo: str, pr: str) -> str:
    return json.loads(gh_api(f"repos/{repo}/pulls/{pr}")).get("body") or ""


def set_body(repo: str, pr: str, body: str) -> None:
    subprocess.run(["gh", "api", "-X", "PATCH", f"repos/{repo}/pulls/{pr}", "-f", f"body={body}"], check=True)


def parse_rows(body: str) -> list[dict]:
    match = re.search(re.escape(MARK_START) + r"(.*)" + re.escape(MARK_END), body, re.DOTALL)
    if not match:
        return []
    rows = []
    for line in match.group(1).splitlines():
        row = ROW_RE.match(line.strip())
        if row:
            rows.append(row.groupdict())
    return rows


def strip_block(body: str) -> str:
    stripped = re.sub(re.escape(MARK_START) + r".*?" + re.escape(MARK_END) + r"\n*", "", body, flags=re.DOTALL)
    return stripped.lstrip("\n")


def render(rows: list[dict]) -> str:
    top = rows[0]
    lines = [
        MARK_START,
        "### 🛰️ CodeIntent — live governance view",
        "",
        f"**▶ [Open the live status page for the current run ↗]({top['live']})**  ",
        f"_Commit `{top['sha']}` · {top['when']} · [workflow run ↗]({top['run']})_",
        "",
        f"<details><summary>Run history ({len(rows)})</summary>",
        "",
        "| When (UTC) | Commit | Result | Links |",
        "| --- | --- | --- | --- |",
    ]
    for row in rows:
        lines.append(f"| {row['when']} | `{row['sha']}` | {row['result']} | [live]({row['live']}) · [run]({row['run']}) |")
    lines += [
        "",
        "</details>",
        "",
        "_Fills in as the gate runs (assemble → analyze → evaluate). The verdict also posts to this PR's Checks card._",
        MARK_END,
    ]
    return "\n".join(lines)


def new_row(args: argparse.Namespace, result: str) -> dict:
    return {
        "when": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC"),
        "sha": args.sha[:7],
        "result": result,
        "live": args.live_url,
        "run": args.run_url,
    }


def cmd_start(args: argparse.Namespace) -> None:
    body = get_body(args.repo, args.pr)
    rows = parse_rows(body)
    rows.insert(0, new_row(args, "⏳ pending"))
    rows = rows[: args.limit]
    set_body(args.repo, args.pr, render(rows) + "\n\n" + strip_block(body))


def cmd_finish(args: argparse.Namespace) -> None:
    body = get_body(args.repo, args.pr)
    rows = parse_rows(body)
    for row in rows:
        if row["sha"] == args.sha[:7]:
            row["result"] = args.result
            break
    else:
        # The `start` row for this run isn't there (e.g. that step failed, or
        # the same commit re-triggered enough runs to push it out of the
        # history window) -- add a result-only row rather than drop the verdict.
        rows.insert(0, new_row(args, args.result))
        rows = rows[: args.limit]
    set_body(args.repo, args.pr, render(rows) + "\n\n" + strip_block(body))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("command", choices=["start", "finish"])
    parser.add_argument("--repo", required=True)
    parser.add_argument("--pr", required=True)
    parser.add_argument("--sha", required=True)
    parser.add_argument("--live-url", required=True, dest="live_url")
    parser.add_argument("--run-url", required=True, dest="run_url")
    parser.add_argument("--limit", type=int, default=10)
    parser.add_argument("--result", default="⏳ pending")
    args = parser.parse_args()
    {"start": cmd_start, "finish": cmd_finish}[args.command](args)


if __name__ == "__main__":
    main()
