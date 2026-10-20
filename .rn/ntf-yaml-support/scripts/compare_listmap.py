"""LIST_MAP 全行比較スクリプト（Task #3 のサンプリング比較の補強用）

目的: XlsDump.java が出力した Excel ダンプ（POI 直読み）と、converter が生成した
      YAML の list_maps を全行・全カラムで突き合わせ、変換の取りこぼし・値化けを
      機械的に検出する。サンプリング（各ファイル3件以上）の目視比較を補強する。

使い方: python3 -I compare_listmap.py <dump.txt> <yaml_dir>
  <yaml_dir> はシート名.yaml が並ぶディレクトリ（例: .../ProjectFormTest/）

比較ルール（NTF の Excel 解釈に合わせた正規化）:
  - `[No]` などの `[...]` マーカー列は比較対象外（converter も出力しない）
  - ヘッダ範囲外の列（`// emptyNG` 等のコメント）は比較対象外
  - Excel の空セルは "" とみなす
  - Excel 値が `"..."` で囲まれている場合は外側のダブルクォートを除去（QuotationTrimmer 相当）
  - Excel 値 `null` は YAML の null と対応
  - 空行または次のディレクティブ行でブロック終端
"""
import os
import re
import sys

import yaml


def load_dump(path):
    sheets = {}
    cur = None
    for line in open(path, encoding="utf-8"):
        line = line.rstrip("\n")
        m = re.match(r"### SHEET\[\d+\] name=(.*) rows=\d+$", line)
        if m:
            cur = {}
            sheets[m.group(1)] = cur
            continue
        pos, _type, val = line.split("\t", 2)
        r, c = map(int, re.match(r"R(\d+)C(\d+)", pos).groups())
        cur.setdefault(r, {})[c] = val.replace("\\n", "\n")
    return sheets


def excel_list_maps(grid):
    result = {}
    rows = sorted(grid)
    for r in rows:
        v = grid[r].get(0, "")
        if not v.startswith("LIST_MAP="):
            continue
        block_id = v.split("=", 1)[1]
        header = grid.get(r + 1, {})
        cols = {c: name for c, name in header.items() if not re.fullmatch(r"\[.*\]", name)}
        data = []
        rr = r + 2
        while rr in grid and any(grid[rr].get(c, "") != "" for c in header):
            first = grid[rr].get(0, "")
            if re.match(r"^[A-Z_]+=", first):
                break
            row = {}
            for c, name in cols.items():
                val = grid[rr].get(c, "")
                if len(val) >= 2 and val.startswith('"') and val.endswith('"'):
                    val = val[1:-1]
                row[name] = None if val == "null" else val
            data.append(row)
            rr += 1
        result[block_id] = data
    return result


def main():
    dump, ydir = sys.argv[1], sys.argv[2]
    ng = 0
    for sheet, grid in load_dump(dump).items():
        yfile = os.path.join(ydir, sheet + ".yaml")
        doc = yaml.safe_load(open(yfile, encoding="utf-8")) or {}
        ymaps = {m["id"]: m.get("rows") or [] for m in doc.get("list_maps", [])}
        for block_id, xrows in excel_list_maps(grid).items():
            yrows = ymaps.get(block_id)
            if yrows is None:
                print(f"NG {sheet}/{block_id}: YAML にブロックなし")
                ng += 1
                continue
            if len(xrows) != len(yrows):
                print(f"NG {sheet}/{block_id}: 行数 Excel={len(xrows)} YAML={len(yrows)}")
                ng += 1
            for i, (x, y) in enumerate(zip(xrows, yrows), 1):
                y = {k: (None if v is None else str(v)) for k, v in y.items()}
                if x != y:
                    print(f"NG {sheet}/{block_id} row{i}: Excel={x} YAML={y}")
                    ng += 1
            print(f"{sheet}/{block_id}: Excel={len(xrows)}行 YAML={len(yrows)}行")
    print("RESULT:", "OK" if ng == 0 else f"NG({ng})")
    return 1 if ng else 0


if __name__ == "__main__":
    sys.exit(main())
