#!/usr/bin/env python3
"""ExcelテストデータとNTF変換後YAMLの内容一致を検証する（task #4）。

各Excel（.xlsx/.xls）を走査し、SETUP_TABLE= / EXPECTED_TABLE= / LIST_MAP= の
各ブロックを抽出して、同じテストクラスディレクトリ配下のYAML（setup_tables /
expected_tables / list_maps）と、行数・値を突き合わせる。

数値セルはコンバーターが cell.toString() で "1.0" のような float 文字列に変換する
ため、数値として等価な表現（1 と "1.0" と "1"）は一致とみなす（値の同一性を確認する
のが目的で、文字列型表現の差はH2のカラム型への暗黙変換で吸収されるため実害なし）。

依存: openpyxl（.xlsx）, xlrd（.xls）, pyyaml（requirements.txt 参照）
セットアップと実行（Excel は task #4 で削除済みのため、再実行時は git から復元すること）:
    uv venv ~/.cache/ntf-rest-yaml-venv --python 3.14
    uv pip install --python ~/.cache/ntf-rest-yaml-venv -r .rn/ntf-yaml-support/scripts/requirements.txt
    ~/.cache/ntf-rest-yaml-venv/bin/python3 .rn/ntf-yaml-support/scripts/compare_excel_yaml.py
"""
import glob
import os
import sys

import openpyxl
import xlrd
import yaml

REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
TEST_ROOT = os.path.join(REPO, "src", "test", "java", "com", "nablarch", "example")

# Excelマーカー → YAMLトップレベルキー / エントリ識別キー
MARKERS = {
    "SETUP_TABLE": ("setup_tables", "table"),
    "EXPECTED_TABLE": ("expected_tables", "table"),
    "LIST_MAP": ("list_maps", "id"),
}


NULL = object()  # YAML null / NTF "null" マーカーを表すセンチネル


def norm(v, from_excel=False):
    """比較用に値を正規化する。

    - 数値の float 文字列と整数表現を揃える（1 / "1.0" / "1" を同一視）。
    - Excel側のNTF記法を解釈: 前後のダブルクォートは「文字列強制マーカー」なので剥がし、
      "null" は null値マーカーなのでYAMLのnull（None）と同一視する。
    """
    if v is None:
        return NULL if not from_excel else ""
    s = str(v).strip()
    if s == "":
        return ""
    if from_excel:
        # NTF記法: 前後ダブルクォートは文字列強制指定（コンバーターが剥がす）
        if len(s) >= 2 and s.startswith('"') and s.endswith('"'):
            s = s[1:-1]
        if s == "null":
            return NULL
    try:
        f = float(s)
        if f != f:  # NaN
            return s
        if f == int(f):
            return str(int(f))
        return repr(f)
    except (ValueError, OverflowError):
        return s


def is_marker_column(colname):
    """[No] のような角括弧で囲まれたマーカーカラムか。実データではないため比較対象外。"""
    c = colname.strip()
    return c.startswith("[") and c.endswith("]")


def is_comment(cell):
    return isinstance(cell, str) and cell.startswith("//")


def read_sheet_rows(cells):
    """2次元リスト（行×列）からExcelブロックを抽出する。

    戻り値: [(marker_type, name, [row_dict, ...]), ...]
    """
    blocks = []
    i = 0
    n = len(cells)
    while i < n:
        row = cells[i]
        first = row[0] if row else None
        if isinstance(first, str) and "=" in first:
            key, _, name = first.partition("=")
            key = key.strip()
            if key in MARKERS:
                # 次のヘッダー行を探す（コメント/空をスキップ）
                j = i + 1
                while j < n:
                    hrow = cells[j]
                    hfirst = hrow[0] if hrow else None
                    if hfirst is None or hfirst == "":
                        j += 1
                        continue
                    if is_comment(hfirst):
                        j += 1
                        continue
                    break
                if j >= n:
                    i += 1
                    continue
                header = [str(c).strip() for c in cells[j] if c is not None and str(c).strip() != ""]
                # データ行を収集
                data_rows = []
                k = j + 1
                while k < n:
                    drow = cells[k]
                    dfirst = drow[0] if drow else None
                    if dfirst is None or dfirst == "":
                        break  # 空行でブロック終了
                    if isinstance(dfirst, str) and "=" in dfirst and dfirst.split("=")[0].strip() in MARKERS:
                        break  # 次マーカーでブロック終了
                    if is_comment(dfirst):
                        k += 1
                        continue
                    rowdict = {}
                    for ci, colname in enumerate(header):
                        rowdict[colname] = drow[ci] if ci < len(drow) else None
                    data_rows.append(rowdict)
                    k += 1
                blocks.append((key, name.strip(), header, data_rows))
                i = k
                continue
        i += 1
    return blocks


def load_xlsx(path):
    wb = openpyxl.load_workbook(path, data_only=True)
    sheets = {}
    for name in wb.sheetnames:
        sh = wb[name]
        cells = []
        for r in range(1, sh.max_row + 1):
            cells.append([sh.cell(r, c).value for c in range(1, sh.max_column + 1)])
        sheets[name] = cells
    return sheets


def load_xls(path):
    wb = xlrd.open_workbook(path)
    sheets = {}
    for sh in wb.sheets():
        cells = []
        for r in range(sh.nrows):
            row = []
            for c in range(sh.ncols):
                v = sh.cell_value(r, c)
                # xlrdは数値をfloatで返す。整数値はintに寄せておく
                if isinstance(v, float) and v == int(v):
                    v = int(v)
                if v == "":
                    v = None
                row.append(v)
            cells.append(row)
        sheets[name := sh.name] = cells
    return sheets


def yaml_entries(ydata, top_key, id_key):
    result = {}
    for entry in (ydata.get(top_key) or []):
        result[str(entry.get(id_key))] = entry.get("rows") or []
    return result


def main():
    excels = sorted(glob.glob(os.path.join(TEST_ROOT, "**", "*.xlsx"), recursive=True) +
                    glob.glob(os.path.join(TEST_ROOT, "**", "*.xls"), recursive=True))
    total_checks = 0
    mismatches = []
    summary = []

    for xpath in excels:
        base = xpath.rsplit(".", 1)[0]  # 同名ディレクトリ
        rel = os.path.relpath(xpath, REPO)
        sheets = load_xlsx(xpath) if xpath.endswith(".xlsx") else load_xls(xpath)

        for sheet_name, cells in sheets.items():
            blocks = read_sheet_rows(cells)
            if not blocks:
                continue
            yaml_path = os.path.join(base, sheet_name + ".yaml")
            if not os.path.exists(yaml_path):
                mismatches.append(f"[{rel}] sheet '{sheet_name}': 対応YAMLが存在しない ({yaml_path})")
                continue
            with open(yaml_path, encoding="utf-8") as f:
                ydata = yaml.safe_load(f) or {}

            for marker_type, name, header, data_rows in blocks:
                top_key, id_key = MARKERS[marker_type]
                yentries = yaml_entries(ydata, top_key, id_key)
                yrows = yentries.get(name)
                if yrows is None:
                    mismatches.append(f"[{rel}] {sheet_name}/{marker_type}={name}: YAMLに {top_key}[{id_key}={name}] が無い")
                    continue
                if len(yrows) != len(data_rows):
                    mismatches.append(
                        f"[{rel}] {sheet_name}/{marker_type}={name}: 行数不一致 Excel={len(data_rows)} YAML={len(yrows)}")
                    continue
                for ri, (xrow, yrow) in enumerate(zip(data_rows, yrows)):
                    for col in header:
                        if is_marker_column(col):
                            continue  # マーカーカラムは実データでないため比較対象外
                        xv = norm(xrow.get(col), from_excel=True)
                        yv = norm(yrow.get(col), from_excel=False)
                        total_checks += 1
                        if xv != yv:
                            mismatches.append(
                                f"[{rel}] {sheet_name}/{marker_type}={name} row{ri} col'{col}': "
                                f"Excel={xrow.get(col)!r}({xv!r}) != YAML={yrow.get(col)!r}({yv!r})")
            summary.append(f"  {rel} :: {sheet_name} — {len(blocks)}ブロック")

    print(f"検証Excel数: {len(excels)}")
    for line in summary:
        print(line)
    print(f"\n総チェック数（フィールドレベル）: {total_checks}")
    print(f"不一致: {len(mismatches)}")
    for m in mismatches:
        print("  NG:", m)
    if not mismatches:
        print("\n全ブロックでExcelとYAMLの値・行数が一致（数値のfloat文字列は正規化して等価判定）")
        return 0
    return 1


if __name__ == "__main__":
    sys.exit(main())
