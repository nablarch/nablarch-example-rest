# task-3 Completion Check

変換ツール: `nablarch-testing-converter:1.0.0-SNAPSHOT:convert`（`from=xls`, `to=yaml`, input=output=`src/test/java`, overwrite=true）。出力 `Converted 5 file(s).` / BUILD SUCCESS、重複カラムWARNなし。
サンプリング比較は Apache POI(3.8) による独立ダンプ（`scripts/XlsDump.java`）と生成YAMLの突き合わせで実施。

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 変換対象の全ExcelファイルにYAMLが生成されている | OK | 5 Excel（.xls×4 + .xlsx×1）→ 12 YAML（シート単位）。全シート名とYAMLファイル名が1:1対応 | OK | `git ls-files` で12 YAML（日本語名含む）コミット済み・Excel5件保持・作業ツリーcleanを独立確認 |
| サンプリング比較でExcelとYAMLの内容が一致 | OK | 全12ファイルで各3件以上を独立ダンプと突合（下記詳細）。1点のみ数値セルの文字列表現差（`N.0`、値は同一・下記「差異」参照） | OK | POI独立ダンプ（セル型・raw値付き拡張版）で再検証。小ファイルは全行、大ファイル(137/141行)は行数一致＋先頭/中間/末尾照合。不一致ゼロ |
| 変換後YAMLがコミットされている | OK | コミット `497de6f`（YAMLのみ、Excel未削除） | OK | 497de6f に Excel/pom の変更が含まれないことを確認 |

### 生成ファイル（12 YAML / 5 Excel）

- `ProjectActionTest.xlsx`（2シート）→ `ProjectActionTest/setUpDb.yaml`, `ProjectActionTest/プロジェクトを新規登録できること.yaml`
- `ProjectFormTest.xls`（3）→ `ProjectFormTest/{testCharsetAndLength,testSingleValidation,testBeanValidation}.yaml`
- `ProjectRenameFormTest.xls`（2）→ `ProjectRenameFormTest/{testCharsetAndLength,testSingleValidation}.yaml`
- `ProjectSearchFormTest.xls`（2）→ `ProjectSearchFormTest/{testCharsetAndLength,testSingleValidation}.yaml`
- `ProjectUpdateFormTest.xls`（3）→ `ProjectUpdateFormTest/{testCharsetAndLength,testSingleValidation,testBeanValidation}.yaml`

### サンプリング比較詳細（各ファイル3件以上）

**ProjectActionTest / setUpDb.yaml**（SETUP_TABLE=PROJECT、Excel 10データ行 → YAML 10行）
- row1 PROJECT_NAME=プロジェクト００１ / PROJECT_START_DATE=`20100918`（テキスト保持）/ PROJECT_END_DATE=`20150409` → 一致 ✓
- row2 NOTE/SALES/COGS/SGA/ALLOCATION が Excel `null` → YAML `null` → 一致 ✓
- 数値セル PROJECT_ID/CLIENT_ID/USER_ID/SALES 等は YAML で `"1.0"`/`"100.0"`/`"10000.0"` 表現（下記「差異」参照、値は同一）

**ProjectActionTest / プロジェクトを新規登録できること.yaml**（SETUP空 / EXPECTED_TABLE=PROJECT 1行）
- SETUP_TABLE=PROJECT ヘッダのみ・データ行なし → YAML `rows: []` → 一致 ✓
- EXPECTED row: PROJECT_NAME=プロジェクト９９９ / PROJECT_START_DATE=`2016-01-01`（テキスト）/ CLIENT_ID=`10`（テキストセルのため `.0` 無し）→ 一致 ✓
- USER_ID=`null` → YAML `null` → 一致 ✓

**ProjectFormTest / testCharsetAndLength.yaml**（4行）
- projectName: allowEmpty=x, max=64, interpolateKey_1=max, {SystemChar.message}, 文字種マトリクス（半角英字=x…全角英字=o…改行=x）全21列 → 一致 ✓
- projectManager/projectLeader: allowEmpty=o、他同一 → 一致 ✓
- note: 半角系すべて o, 改行=o → 一致 ✓（空セル min → `""`）

**ProjectFormTest / testSingleValidation.yaml**（137行）
- projectType row1: input1空 / messageId={Required}（末尾 `// emptyNG` コメント列は非データとして除外）→ 一致 ✓
- projectType development/maintenance: messageId空 → 一致 ✓
- projectType hoge / `${半角英字, 1}`: messageId={EnumElement.message} → 一致 ✓

**ProjectFormTest / testBeanValidation.yaml**（testShots 5 + params 5 = 10行）
- `[No]` マーカー列は非データとして除外 → 一致 ✓
- testShots 異常系-1: expectedMessageId1=「プロジェクト終了日は…入力して下さい。」/ propertyName1=validProjectPeriod → 一致 ✓
- params row1: projectClass=ss, clientId=`${半角数字,9}`, projectStartDate=`20230817`（テキスト）→ 一致 ✓

**ProjectRenameFormTest / testCharsetAndLength.yaml**（1行）
- projectName: allowEmpty=x, max=64, {SystemChar.message}, 文字種マトリクス → 一致 ✓

**ProjectRenameFormTest / testSingleValidation.yaml**（4行）
- projectId row1: input1空 / {Required}（`// emptyNG` 除外）→ 一致 ✓
- projectId `1${半角数字,9}`: {Digits.integer.message} → 一致 ✓
- projectId `"-1"`（Excelはダブルクォート付き）→ YAML `-1`（QuotationTrimmer相当でクォート除去、値一致）/ {NumberRange.min.message}, interpolateKey_1=min, interpolateValue_1=0 → 一致 ✓

**ProjectSearchFormTest / testCharsetAndLength.yaml**（1行）
- projectName: allowEmpty=`o`（Rename/Form は x。Search 固有値を正しく反映）/ max=64 / 文字種マトリクス → 一致 ✓

**ProjectSearchFormTest / testSingleValidation.yaml**（4行）
- clientId row1: input1空 / messageId空（`// emptyOK`。Rename の emptyNG と異なる値を正しく反映）→ 一致 ✓
- clientId `${半角数字,9}` / `1${半角数字,9}`={Digits.integer} / `"-1"`→`-1`={NumberRange.min} → 一致 ✓

**ProjectUpdateFormTest / testCharsetAndLength.yaml**（4行）
- projectName: allowEmpty=x, max=64, {SystemChar.message}, 文字種マトリクス → 一致 ✓

**ProjectUpdateFormTest / testSingleValidation.yaml**（141行）
- projectId row1: input1空 / {Required}（`// emptyNG` 除外）→ 一致 ✓
- projectId `${半角数字,9}` / `1${半角数字,9}`={Digits.integer} / `"-1"`→`-1`={NumberRange.min} → 一致 ✓

**ProjectUpdateFormTest / testBeanValidation.yaml**（testShots 5 + params 5 = 10行）
- params は Form と異なり先頭に projectId 列を持つ（Update 固有の構造を正しく反映）→ 一致 ✓
- 日付 projectStartDate/projectEndDate はテキスト保持 → 一致 ✓

### 差異（原因調査済み・値は同一）

- **数値セルの文字列表現 `N.0`**: `ProjectActionTest/setUpDb.yaml` の PROJECT_ID / CLIENT_ID / USER_ID / SALES / COST_OF_GOODS_SOLD / SGA / ALLOCATION_OF_CORP_EXPENSES が `"1.0"` / `"100.0"` / `"10000.0"` 等になっている。
  - **原因**: 元 Excel の該当セルが数値（NUMERIC）型で、converter が数値セルを `cell.toString()` 相当で文字列化するため `.0` が付く（POI DataFormatter 表示では `1` だが実体は数値 `1.0`）。隣接 `nablarch-example-web` でも同一事象を確認済みで、「H2 が INTEGER カラムへ暗黙変換するためテストに影響なし」として受容済み（web steering.md Rules）。
  - **影響範囲**: `setUpDb`（SETUP_TABLE=INSERT用）のみ。EXPECTED_TABLE 比較に使う値ではない（request test の EXPECTED はテキストセルで `.0` 無し）。
  - **値の同一性**: 数値 1 と 1.0、10000 と 10000.0 は同値。日付カラムはすべてテキストで厳密一致（`20100918` 等、`.0` 付与なし）。
  - **方針判断はユーザーに委ねる**（修正せず報告）。最終的な妥当性は Task #5 の `mvn test` 全パスで確認する。

### 補足（Task #5 への先行確認）

- 生成YAML全体で `- {}`（空マッピング行）は 0 件（`grep -rn -- "- {}" src/test/java`）。web で発生した `requestParams` の空行スキップ問題は本プロジェクトでは該当なし（本プロジェクトの action テストに `requestParams` ブロックは存在しない）。

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | OK | 変換器と別経路（POI直読み・セル型/raw値付き）での独立突合。N.0 の真因（元セルがNUMERIC型、raw=1.0）を特定して評価している点が適切 |
| Edge case coverage | OK | N.0表現/日付テキスト保持/null変換/`"-1"`→`-1`/マーカー・コメント列除外/行数一致/`- {}`0件 を全て独立検証。不一致ゼロ |

QA の N.0 見解: 「許容可能な表現差であり停止すべき内容不一致ではない」。(a)元セル数値型で同値、(b)影響は setUpDb の INSERT 用 SETUP のみで EXPECTED 比較値でない、(c)H2 が INTEGER/NUMBER へ `"1.0"` を解釈。最終妥当性は Task #5 の mvn test で確認、という方針に同意。

## Expert Reviews (code changes only)

N/A — no code changes in this task.

## Overall Verdict

- Self-check: OK（数値セルの `N.0` 表現のみユーザー判断を仰ぐ。値は同一）
- QA: OK（`N.0` は許容可能な表現差との見解）
- Language expert: N/A
- Software-engineering expert: N/A
- Ready for user review: Yes（`N.0` 受容可否をユーザー確認）
