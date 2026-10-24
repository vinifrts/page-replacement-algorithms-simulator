"""
Gera os gráficos do relatório a partir dos CSVs produzidos pelo simulador Java.

Uso (na raiz do projeto):
    python scripts/generate_charts.py             # barras usando results/results.csv
    python scripts/generate_charts.py --frames 4  # barras usando a linha "4 quadros" do batch

Nenhum número é definido neste script: tudo vem dos arquivos CSV.
"""

import argparse
import csv
import sys
from pathlib import Path

import matplotlib

matplotlib.use("Agg")  # só salva arquivos PNG; não precisa de janela gráfica
import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator

ROOT = Path(__file__).resolve().parent.parent
RESULTS_FILE = ROOT / "results" / "results.csv"
BATCH_FILE = ROOT / "results" / "batch_results.csv"
CHARTS_DIR = ROOT / "charts"

ALGORITHMS = ["FIFO", "LRU", "NFU", "Optimal"]
REQUIRED_COLUMNS = {"algorithm", "frames", "page_faults", "total_references"}


def fail(message):
    print(f"Error: {message}", file=sys.stderr)
    sys.exit(1)


def load_rows(path):
    """Lê um CSV de resultados e converte os campos numéricos."""
    if not path.exists():
        fail(f"File not found: {path}\n"
             "Run the Java simulator first (see README) to generate the results.")

    with open(path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        if reader.fieldnames is None or not REQUIRED_COLUMNS.issubset(reader.fieldnames):
            fail(f"{path} must contain the columns: {', '.join(sorted(REQUIRED_COLUMNS))}")
        rows = []
        for line_number, row in enumerate(reader, start=2):
            try:
                rows.append({
                    "algorithm": row["algorithm"].strip(),
                    "frames": int(row["frames"]),
                    "page_faults": int(row["page_faults"]),
                    "total_references": int(row["total_references"]),
                })
            except (ValueError, TypeError):
                fail(f"Invalid numeric value in {path}, line {line_number}.")

    if not rows:
        fail(f"{path} has no data rows.")
    return rows


def bar_chart(rows):
    """Barras: faltas de página de cada algoritmo para uma quantidade de quadros."""
    frames = rows[0]["frames"]
    total = rows[0]["total_references"]
    faults = {r["algorithm"]: r["page_faults"] for r in rows if r["frames"] == frames}

    missing = [a for a in ALGORITHMS if a not in faults]
    if missing:
        fail(f"Missing algorithms in the data: {', '.join(missing)}")

    values = [faults[a] for a in ALGORITHMS]

    fig, ax = plt.subplots(figsize=(7, 5))
    bars = ax.bar(ALGORITHMS, values, color=["#4C72B0", "#55A868", "#C44E52", "#8172B2"])
    ax.bar_label(bars, padding=3)
    ax.set_title(f"Page Faults by Algorithm ({frames} frames, {total} references)")
    ax.set_xlabel("Algorithm")
    ax.set_ylabel("Page faults")
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))
    ax.set_ylim(0, max(values) * 1.15 if max(values) > 0 else 1)
    ax.grid(axis="y", linestyle="--", alpha=0.5)
    ax.set_axisbelow(True)
    fig.tight_layout()

    output = CHARTS_DIR / "page_faults_bar.png"
    fig.savefig(output, dpi=150)
    plt.close(fig)
    print(f"Saved: {output}")


def line_chart(rows):
    """Linhas: faltas de página x quantidade de quadros, uma linha por algoritmo."""
    fig, ax = plt.subplots(figsize=(8, 5))
    all_frames = sorted({r["frames"] for r in rows})

    for algorithm in ALGORITHMS:
        points = sorted((r["frames"], r["page_faults"]) for r in rows if r["algorithm"] == algorithm)
        if not points:
            fail(f"Algorithm '{algorithm}' not found in {BATCH_FILE}")
        ax.plot([p[0] for p in points], [p[1] for p in points], marker="o", label=algorithm)

    ax.set_title("Page Faults vs. Number of Frames")
    ax.set_xlabel("Number of frames")
    ax.set_ylabel("Page faults")
    ax.set_xticks(all_frames)
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))
    ax.grid(True, linestyle="--", alpha=0.5)
    ax.legend(title="Algorithm")
    fig.tight_layout()

    output = CHARTS_DIR / "page_faults_line.png"
    fig.savefig(output, dpi=150)
    plt.close(fig)
    print(f"Saved: {output}")


def main():
    parser = argparse.ArgumentParser(description="Generate charts from simulator results.")
    parser.add_argument("--frames", type=int,
                        help="frame count for the bar chart (taken from batch_results.csv). "
                             "Default: use results/results.csv")
    args = parser.parse_args()

    batch_rows = load_rows(BATCH_FILE)

    if args.frames is None:
        bar_rows = load_rows(RESULTS_FILE)
    else:
        bar_rows = [r for r in batch_rows if r["frames"] == args.frames]
        if not bar_rows:
            available = sorted({r["frames"] for r in batch_rows})
            fail(f"No data for {args.frames} frames in {BATCH_FILE}. Available: {available}")

    CHARTS_DIR.mkdir(exist_ok=True)
    bar_chart(bar_rows)
    line_chart(batch_rows)


if __name__ == "__main__":
    main()