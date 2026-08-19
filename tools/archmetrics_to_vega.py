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
"""Turn the java-architecture-metrics JSON report into Vega-Lite specs.

The plugin reports, per component: classCount, abstractTypeCount,
afferentCoupling (Ca), efferentCoupling (Ce), abstractness (A),
instability (I), distance (D), plus the directed incoming/outgoingComponents
sets. Everything below is derived from that one file.

    python3 tools/archmetrics_to_vega.py <report.json> [--outdir DIR] [--exclude external,platform]
"""
import argparse, json, pathlib, sys

MAIN_SEQUENCE = "main-sequence.vl.json"
DISTANCE_RANK = "distance-ranking.vl.json"
COUPLING_MTX = "cross-feature-matrix.vl.json"


def load(path, exclude):
    report = json.loads(pathlib.Path(path).read_text())
    rows = []
    for c in report.get("components", []):
        if c.get("component") in exclude:
            continue
        out = [d for d in c.get("outgoingComponents", []) if d not in exclude]
        inc = [d for d in c.get("incomingComponents", []) if d not in exclude]
        rows.append({
            "component": c.get("component"),
            "classCount": c.get("classCount", 0),
            "abstractTypeCount": c.get("abstractTypeCount", 0),
            "Ca": c.get("afferentCoupling", 0),
            "Ce": c.get("efferentCoupling", 0),
            "A": round(c.get("abstractness", 0.0), 4),
            "I": round(c.get("instability", 0.0), 4),
            "D": round(c.get("distance", 0.0), 4),
            "CFV": len(out),
            "outgoing": sorted(out),
            "incoming": sorted(inc),
        })
    return sorted(rows, key=lambda r: -r["CFV"])


def main_sequence(rows):
    return {
        "$schema": "https://vega.github.io/schema/vega-lite/v5.json",
        "title": "Abstractness vs Instability (distance from the main sequence)",
        "width": 520, "height": 420,
        "data": {"values": [{k: r[k] for k in ("component", "A", "I", "D", "classCount")} for r in rows]},
        "layer": [
            {   # the main sequence itself: A + I = 1
                "data": {"values": [{"I": 0, "A": 1}, {"I": 1, "A": 0}]},
                "mark": {"type": "line", "strokeDash": [6, 4], "color": "#888"},
                "encoding": {
                    "x": {"field": "I", "type": "quantitative"},
                    "y": {"field": "A", "type": "quantitative"},
                },
            },
            {
                "mark": {"type": "circle", "opacity": 0.8},
                "encoding": {
                    "x": {"field": "I", "type": "quantitative", "title": "Instability (I = Ce / (Ca + Ce))",
                          "scale": {"domain": [0, 1]}},
                    "y": {"field": "A", "type": "quantitative", "title": "Abstractness (A)",
                          "scale": {"domain": [0, 1]}},
                    "size": {"field": "classCount", "type": "quantitative", "title": "classes"},
                    "color": {"field": "D", "type": "quantitative", "title": "Distance",
                              "scale": {"scheme": "orangered"}},
                    "tooltip": [{"field": c, "type": t} for c, t in
                                (("component", "nominal"), ("A", "quantitative"), ("I", "quantitative"),
                                 ("D", "quantitative"), ("classCount", "quantitative"))],
                },
            },
            {
                "mark": {"type": "text", "dy": -10, "fontSize": 9},
                "encoding": {
                    "x": {"field": "I", "type": "quantitative"},
                    "y": {"field": "A", "type": "quantitative"},
                    "text": {"field": "component", "type": "nominal"},
                },
            },
        ],
    }


def distance_ranking(rows):
    return {
        "$schema": "https://vega.github.io/schema/vega-lite/v5.json",
        "title": "Distance from the main sequence, by component",
        "width": 520, "height": {"step": 14},
        "data": {"values": [{k: r[k] for k in ("component", "D", "Ce", "Ca")} for r in rows]},
        "mark": "bar",
        "encoding": {
            "y": {"field": "component", "type": "nominal", "sort": "-x", "title": None},
            "x": {"field": "D", "type": "quantitative", "title": "Distance (D = |A + I - 1|)"},
            "color": {"field": "D", "type": "quantitative", "legend": None,
                      "scale": {"scheme": "orangered"}},
            "tooltip": [{"field": c, "type": t} for c, t in
                        (("component", "nominal"), ("D", "quantitative"),
                         ("Ce", "quantitative"), ("Ca", "quantitative"))],
        },
    }


def coupling_matrix(rows):
    cells = [{"from": r["component"], "to": t, "v": 1} for r in rows for t in r["outgoing"]]
    return {
        "$schema": "https://vega.github.io/schema/vega-lite/v5.json",
        "title": "Cross-feature dependencies (row depends on column)",
        "width": 520, "height": 520,
        "data": {"values": cells},
        "mark": "rect",
        "encoding": {
            "y": {"field": "from", "type": "nominal", "title": "depends on ->"},
            "x": {"field": "to", "type": "nominal", "title": None,
                  "axis": {"labelAngle": -45, "orient": "top"}},
            "color": {"value": "#d1495b"},
            "tooltip": [{"field": "from", "type": "nominal"}, {"field": "to", "type": "nominal"}],
        },
    }


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("report", type=pathlib.Path)
    ap.add_argument("--outdir", type=pathlib.Path,
                    default=pathlib.Path("fineract/fineract-doc/src/docs/en/diagrams"))
    ap.add_argument("--exclude", default="external",
                    help="comma-separated components to drop (default: external)")
    a = ap.parse_args()
    if not a.report.is_file():
        raise SystemExit(f"no such report: {a.report}")
    exclude = {s.strip() for s in a.exclude.split(",") if s.strip()}
    rows = load(a.report, exclude)
    a.outdir.mkdir(parents=True, exist_ok=True)
    for name, spec in ((MAIN_SEQUENCE, main_sequence(rows)),
                       (DISTANCE_RANK, distance_ranking(rows)),
                       (COUPLING_MTX, coupling_matrix(rows))):
        (a.outdir / name).write_text(json.dumps(spec, indent=2) + "\n")
        print(f"wrote {a.outdir / name}")

    pairs = sum(r["CFV"] for r in rows)
    clean = [r["component"] for r in rows if r["CFV"] == 0]
    print(f"\n{len(rows)} components, {pairs} cross-component pairs, {len(clean)} with no outgoing dependency")
    print(f"{'component':28} {'CFV':>4} {'Ce':>5} {'Ca':>5} {'I':>6} {'A':>6} {'D':>6} {'classes':>8}")
    for r in rows:
        print(f"{r['component']:28} {r['CFV']:>4} {r['Ce']:>5} {r['Ca']:>5} {r['I']:>6.2f} {r['A']:>6.2f} {r['D']:>6.2f} {r['classCount']:>8}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
