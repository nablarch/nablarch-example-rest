# nablarch-example-rest NTF YAML 対応 steering

## Overview

- 目的: NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式のExcelテストデータをYAML形式に移行する（背景資料: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211）。
- 対象プロジェクト: `nablarch-example-rest`
- 指示書: `/home/k10949/instructions/ntf-yaml-support-instructions_rest_V1.6.md`
- 再実施: ブランチには過去の移行コミット（`7860eb5`〜`f034958`）が既に積まれているが、指示書に従い履歴を維持したまま、その上にベースラインコミット（develop 一致ツリー）→ task-1〜5 を新規に積み上げて再実施する。
- Acceptance criteria（要約）:
  - 既存 `ntf-yaml-support` ブランチ上で履歴を維持し作業（新規ブランチ作成・rebase・amend・hard reset なし）
  - ブランチ先端の上に develop 一致ツリーのベースラインコミットを1つ積み、その状態で全テストPASSを確認
  - `pom.xml`: `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）、`nablarch-testing-converter:1.0.0-SNAPSHOT`（`<build><plugins>`、plugin直下 `<configuration>`、CLI で `convert`）
  - `YamlTestDataParser`（`testDataParser`）を `testDataParser` が参照される全フェーズで有効になる設定ファイルへ追加。`interpreters` は `dateTimeInterpreter` → `compositeInterpreter` の2つのみを `<list>` で直接記載、`dbInfo` 指定
  - 全対象Excel → YAML 変換（サンプリング一致確認済み）、Excel 削除、Excel なしで全テストPASS

変換対象のExcelテストデータ（全5件、`src/test/java` 配下）:
- `com/nablarch/example/action/ProjectActionTest.xlsx`
- `com/nablarch/example/form/ProjectFormTest.xls`
- `com/nablarch/example/form/ProjectRenameFormTest.xls`
- `com/nablarch/example/form/ProjectSearchFormTest.xls`
- `com/nablarch/example/form/ProjectUpdateFormTest.xls`

## Build / Test

README（`README.md`）で確認した手順:

- DBセットアップ・エンティティ生成: `mvn generate-resources`
  - gsp プラグインはライフサイクルにバインド済みのため `-P gsp` プロファイル指定は不要（README「データベースのセットアップ及びエンティティクラスの作成」）。
  - Java 17+ 用 JVM オプションは `.mvn/jvm.config` で指定済み。
- テスト実行: `mvn test`（README にテスト手順の記載はないため Maven 標準の `test` フェーズを使用）
- 本作業での実行コマンド: `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8 mvn clean generate-resources` → `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8 mvn test`
  - **ロケール**: メッセージ解決に日本語ロケールが必要（`C.UTF-8` だとテストが失敗する。前回作業で判明）。
- 環境: OpenJDK 21.0.2 / Apache Maven 3.9.16
- 変換ツール: `/home/k10949/workspace/nablarch-testing-converter/README.md` の手順に従う

## Rules（指示書より抜粋）

- commit every change; one completion marker per task。push は行わない。
- テストクラス（`.java`）は変更しない。
- 過去のコミット履歴は維持する（rebase / amend / reset --hard / force push 禁止）。
- steering.md は各タスクで更新し当該タスクのコミットに含める。
- YAML 変換後はサンプリング比較してから Excel を削除。差異があれば修正せずユーザーに報告して停止。
- テストは Excel なしで最終パスを確認する。
- 順調なら task 毎の user review は行わず全 task 完了後にまとめて実施。NG / テスト失敗 / 想定外事象 / サンプリング差異があればその時点で停止して報告。
- 各エキスパートレビューは Agent（general-purpose / opus）で実施。

## Scripts

（`.rn/ntf-yaml-support/scripts/` 配下。作成時に目的を記載する）

## Tasks

### #1: ベースライン化（developと同一状態）し、全PASSを確認する

**Purpose**: 既存の `ntf-yaml-support` ブランチ（履歴維持）の先端の上に、ワーキングツリー全体を `develop` と同一内容へ揃えるコミットを1つ新規に積み、その状態で全テストがパスすることを確認する。

**Prerequisites**: none

**Steps**:

- [x] 既存の `ntf-yaml-support` ブランチへ切り替える（新規作成しない）
- [x] 過去の移行コミットを巻き戻さず、現在の先端（`f034958`）の上に処理を積む
- [x] `develop` の存在を確認し、`git read-tree -u --reset develop` → `git commit` でベースラインコミット `d7b6ee1` を積む（`git diff --quiet develop HEAD` で一致確認）
- [x] README を確認し、ビルド・テスト手順を把握する
- [x] steering.md と scripts の初版を作成する（README 手順を明記）
- [x] `mvn clean generate-resources` でエンティティクラスを生成する
- [x] `mvn test` で全テストがパスすることを確認する（79件 / BUILD SUCCESS）
- [x] 結果を `checks/task-1.md` に記録する
- [x] 変更ファイル（check ファイルおよび steering.md）をコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-1.md)
- [x] QA expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）

**Completion criteria**:

- 既存の `ntf-yaml-support` ブランチ上で作業しており、コミット履歴が維持されている
- ブランチの既存先端の上に、ワーキングツリーが `develop` と同一内容になる新規コミットが1つ積まれている
- `mvn test` が全テストパスで終了している（BUILD SUCCESS）

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加

**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）と `nablarch-testing-converter`（ビルドプラグイン）を追加し、`YamlTestDataParser` を `testDataParser` が参照される全フェーズで有効になる設定ファイルへ追加する（interpreters は testdata.html の設定例どおり）。

**Prerequisites**: #1

**Steps**:

- [ ] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加する
- [ ] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` を追加する（`<configuration>` に from / to / input / output / overwrite）
- [ ] 同期送信系（応答電文を実行中に読む）構成かを判定する
- [ ] 判定結果に応じて `YamlTestDataParser` を配置する（`dateTimeInterpreter` → `compositeInterpreter`）
- [ ] `mvn dependency:resolve` / `mvn help:describe` で yaml 依存と converter の `convert` ゴールが解決できることを確認する
- [ ] 変更したファイルをコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-2.md)
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] language expert review (Agent: general-purpose / Opus)
- [ ] software-engineering expert review (Agent: general-purpose / Opus)
- [ ] user review

**Completion criteria**:

- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている
- `YamlTestDataParser` が `testDataParser` が参照される全フェーズで有効になる設定ファイルに追加され、`interpreters` に `dateTimeInterpreter` → `compositeInterpreter` の2つのみが記載されている
- `mvn dependency:resolve` が成功し、converter の `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換

**Purpose**: `nablarch-testing-converter` で Excel テストデータを YAML に変換し、ソースツリー内に配置して、サンプリングで内容を確認する。

**Prerequisites**: #2

**Steps**:

- [ ] 対象 Excel ファイルの所在を確認する
- [ ] 同名 `.xls` / `.xlsx` の併存を確認する
- [ ] converter README の手順に従い、全対象 Excel を YAML 変換する
- [ ] 各 YAML について Excel とサンプリング比較する（各ファイル最低3件、3件未満は全件）
- [ ] 比較結果を `checks/task-3.md` の Sampling Comparison に記録する
- [ ] 変換した YAML をコミットする（Excel はまだ削除しない）
- [ ] self-check (OK/NG per completion criterion, record in checks/task-3.md)
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] user review

**Completion criteria**:

- 変換対象の全 Excel ファイルに対応する YAML ファイルが生成されている
- サンプリング比較で Excel と YAML の内容が一致している
- 変換後の YAML ファイルがコミットされている

### #4: Excelを削除する

**Purpose**: 変換対象だった Excel を削除してコミットする。

**Prerequisites**: #3

**Steps**:

- [ ] 変換対象となった Excel ファイルを削除する
- [ ] 削除結果（残存 Excel 0件）を `checks/task-4.md` に記録する
- [ ] Excel ファイル削除をコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-4.md)
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] user review

**Completion criteria**:

- 変換対象だった全 Excel ファイルが削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる

**Purpose**: Excel なしで全テストがパスすることを確認する。

**Prerequisites**: #4

**Steps**:

- [ ] `mvn test` が BUILD SUCCESS になることを確認する
- [ ] テストログで YamlTestDataParser が実際に使われていることを確認する
- [ ] self-check (OK/NG per completion criterion, record in checks/task-5.md)
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] language expert review (Agent: general-purpose / Opus)
- [ ] software-engineering expert review (Agent: general-purpose / Opus)
- [ ] user review

**Completion criteria**:

- `mvn test` が BUILD SUCCESS で終了する（全テストパス、Excel ファイルなし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる

## State

- **Status**: Task #1 完了（user review は全 task 完了後にまとめて実施）
- **Last completed**: #1 — ベースラインコミット `d7b6ee1`（develop 一致）、79件全パス
- **Next**: #2
