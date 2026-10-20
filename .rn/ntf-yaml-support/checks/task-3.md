# task-3 Completion Check

変換: `mvn -o com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT:convert`（pom の `<configuration>`: from=xls / to=yaml / input=output=`src/test/java` / overwrite=true）→ `Converted 5 file(s).` / BUILD SUCCESS、重複カラム WARN なし。
検証用スクリプト（`.rn/ntf-yaml-support/scripts/`）:
- `XlsDump.java` … POI(3.8) で元 Excel の全セルを「行/列/セル型/生値」でダンプ（変換器と別経路で Excel を読むため）
- `compare_listmap.py` … XlsDump の出力と YAML の `list_maps` を全行・全カラム比較（サンプリング比較の補強。YAML を1か所改変したコピーで NG 検出・exit 1 を確認済み）

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 変換対象の全 Excel ファイルに対応する YAML ファイルが生成されている | OK | Excel 5件（.xls×4 + .xlsx×1、計12シート）→ YAML 12件（`<テストクラス名>/<シート名>.yaml`）。シート名と YAML ファイル名が1:1対応 | OK | xls はダンプのシート一覧、xlsx は workbook.xml 直読みでシートを列挙し、12シートすべてが YAML と1:1対応。同名 .xls/.xlsx の併存なし |
| サンプリング比較で Excel と YAML の内容が一致している | OK | 全12ファイルで各3件以上（3件未満は全件）を照合し一致（下記 Sampling Comparison）。form 4ファイルは `compare_listmap.py` で LIST_MAP 全行一致。数値セルの `N.0` 表記は NTF の Excel 読み込み値と同一で差異なし（下記注記） | OK | 独自パーサで form 全10ブロックの先頭・末尾＋ランダム3行を照合し差異0。xlsx は sheet XML / sharedStrings を直接パースし setUpDb 10行×15列を全セル照合して差異0 |
| 変換後の YAML ファイルがコミットされている | OK | YAML 12件と検証スクリプトを本タスクのコミットに含める（Excel は未削除） | OK | 未追跡 YAML 12件は gitignore 対象外でコミット対象として揃っている。Excel 5件は tracked のまま |

## Sampling Comparison（task-3 のみ）

| YAML ファイル | 比較件数 | レコードキー/行 | Excel 値 | YAML 値 | 一致 |
| --- | --- | --- | --- | --- | --- |
| action/ProjectActionTest/setUpDb.yaml | 3 | PROJECT row1（R4） | 1.0(NUM) / プロジェクト００１ / 20100918(STR) / 鈴木 / 100.0(NUM) / 備考欄 / 10000.0(NUM) | "1.0" / "プロジェクト００１" / "20100918" / "鈴木" / "100.0" / "備考欄" / "10000.0" | OK |
| 〃 | 〃 | PROJECT row2（R5） | 2.0 / プロジェクト００２ / maintenance / b / 20141110 / NOTE〜ALLOCATION=null | "2.0" / "プロジェクト００２" / "maintenance" / "b" / "20141110" / null×5 | OK |
| 〃 | 〃 | PROJECT row10（R13） | 10.0 / プロジェクト０１０ / c / 20120622 / 20130401 / 山田 / 備考欄４ / 3000.0 | "10.0" / "プロジェクト０１０" / "c" / "20120622" / "20130401" / "山田" / "備考欄４" / "3000.0" | OK |
| action/ProjectActionTest/プロジェクトを新規登録できること.yaml | 2（SETUP 0行・EXPECTED 1行のため全件） | SETUP_TABLE=PROJECT | ヘッダ行のみ（データ行なし） | `rows: []` | OK |
| 〃 | 〃 | EXPECTED_TABLE=PROJECT row1 | プロジェクト９９９ / 2016-01-01 / 2016-03-31 / 10(STR) / USER_ID=null / 備考９９９ / 10000(STR) | "プロジェクト９９９" / "2016-01-01" / "2016-03-31" / "10" / null / "備考９９９" / "10000" | OK |
| form/ProjectFormTest/testCharsetAndLength.yaml | 4（全件） | projectName / projectManager / projectLeader / note | allowEmpty=x,o,o,o / max=64 / 改行=x,x,x,o | allowEmpty="x","o","o","o" / max="64" / 改行="x","x","x","o" | OK |
| form/ProjectFormTest/testSingleValidation.yaml | 3（全137行は compare_listmap.py で一致） | row1 / row69 / row137 | projectType,空,{Required.message} / projectEndDate,`${全角数字, 1}`,{DateFormat.message} / allocationOfCorpExpenses,`${改行, 1}`,{NumberRange.money.range.message},min=0,max=999999999 | 同左 | OK |
| form/ProjectFormTest/testBeanValidation.yaml | 4（testShots 2 + params 2、全10行は compare_listmap.py で一致） | testShots row1 / row5、params row1 / row5 | 正常系-1,validProjectPeriod / 異常系-1,「プロジェクト終了日は…入力して下さい。」/ ss,`${半角数字,9}`,20230817,20230818 / c,20230818,20230817 | 同左（`[No]` 列は除外） | OK |
| form/ProjectRenameFormTest/testCharsetAndLength.yaml | 1（全件） | projectName | x / max=64 / {SystemChar.message} / 全角英字=o / 改行=x | "x" / "64" / 同左 / "o" / "x" | OK |
| form/ProjectRenameFormTest/testSingleValidation.yaml | 4（全件） | row1〜row4 | projectId: 空,{Required} / `${半角数字,9}`,空 / `1${半角数字,9}`,{Digits.integer} / `"-1"`,{NumberRange.min},min,0 | 同左（`"-1"` → `"-1"` の外側クォート除去後 `-1`。`// emptyNG` 列は除外） | OK |
| form/ProjectSearchFormTest/testCharsetAndLength.yaml | 1（全件） | projectName | allowEmpty=o / max=64 / 改行=x | "o" / "64" / "x" | OK |
| form/ProjectSearchFormTest/testSingleValidation.yaml | 4（全件） | row1〜row4 | clientId: 空,空 / `${半角数字,9}` / `1${半角数字,9}`,{Digits.integer} / `"-1"`,{NumberRange.min},min,0 | 同左（`// emptyOK` 列は除外） | OK |
| form/ProjectUpdateFormTest/testCharsetAndLength.yaml | 4（全件） | projectName / projectManager / projectLeader / note | allowEmpty=x,o,o,o / max=64 / 改行=x,x,x,o | 同左 | OK |
| form/ProjectUpdateFormTest/testSingleValidation.yaml | 3（全141行は compare_listmap.py で一致） | row1 / row71 / row141 | projectId,空,{Required} / projectEndDate,`${半角カナ, 1}`,{DateFormat.message} / allocationOfCorpExpenses,`${改行, 1}`,{NumberRange.money.range.message} | 同左 | OK |
| form/ProjectUpdateFormTest/testBeanValidation.yaml | 4（testShots 2 + params 2、全10行は compare_listmap.py で一致） | testShots row1 / row5、params row1 / row5 | 正常系-1 / 異常系-1 / projectId=`${半角数字,9}`,ss,20230817 / c,20230818,20230817 | 同左 | OK |

注記（数値セルの `N.0` 表記）: `setUpDb.yaml` の PROJECT_ID / CLIENT_ID / USER_ID / SALES 等は `"1.0"` 形式。元セルは NUMERIC 型（生値 1.0、書式 General）で、NTF の Excel 読み込み `PoiXlsReader`（`cell.toString()`）も同じく `"1.0"` を返すため、Excel 運用時に NTF が使っていた値と同一。値の差異ではなく Excel 画面上の表示形式との違いのみであり、停止条件（差異）には該当しないと判断（QA も同見解）。form の xls 4件に NUMERIC セルはない。実行時の妥当性は Task #5 の `mvn test` で確認する。

生成 YAML 中の空マッピング行 `- {}` は 0件。

## .xls/.xlsx 併存チェック（該当時のみ）

- 併存の有無: なし（拡張子を除いた同名ファイルの重複 0件）
- 差分確認結果の保存先: 該当なし
- 未使用側（通常 `.xlsx`）の削除: 該当なし

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | OK | 作業者の POI ダンプ＋全行比較に加え、QA が別経路（xlsx は生 XML 直読み、xls はダンプを独自パーサで解析）で照合し差異なし。残余リスク: xls の生 BIFF を POI 以外では読んでいない |
| Edge case coverage | OK | null 変換・日付テキスト・数値文字列・カラム定義のみの SETUP（`rows: []`）・クォート除去・`[No]` 列・コメント列/行・空セル・`- {}` なし・N.0 表記を確認。YAML 読み込み時の実行時挙動は Task #5 で確認 |

## Expert Reviews (code changes only)

N/A — no code changes in this task.

## Overall Verdict

- Self-check: OK
- QA: OK
- Language expert: N/A
- Software-engineering expert: N/A
- Ready for user review: Yes（N.0 表記の扱いは NTF 読み込み値と同一のため一致と判断。user review 時に確認を依頼）
