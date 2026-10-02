# Goal

NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式のExcelテストデータをYAML形式に移行する。
対象プロジェクト: `nablarch-example-rest`
参照: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211

変換対象のExcelテストデータ（全5件）:
- `src/test/java/com/nablarch/example/form/ProjectUpdateFormTest.xls`
- `src/test/java/com/nablarch/example/form/ProjectSearchFormTest.xls`
- `src/test/java/com/nablarch/example/form/ProjectRenameFormTest.xls`
- `src/test/java/com/nablarch/example/form/ProjectFormTest.xls`
- `src/test/java/com/nablarch/example/action/ProjectActionTest.xlsx`

# Acceptance criteria

- `develop` ブランチから作成した `ntf-yaml-support` ブランチで全作業が行われている
- 作業前に既存の全テストがパスすることを確認済みである
- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープの依存として追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている（test 依存ではない）
- `src/test/resources/unit-test.xml` に `YamlTestDataParser` コンポーネントと `yamlInterpreters` リストが追加されている
- 変換対象の全Excelファイルが対応するYAMLファイルに変換されている（サンプリングで内容一致を確認済み）
- 変換後、変換対象だったExcelファイルが全て削除されている
- YAMLテストデータを使った状態でExcelなしで全テストがパスしている

# Assumptions

- `nablarch-testing-yaml:1.0.0-SNAPSHOT` と `nablarch-testing-converter:1.0.0-SNAPSHOT` はローカルMavenリポジトリ（`~/.m2`）に存在する
- テスト実行にはH2インメモリDBを使用し、エンティティ生成（`mvn generate-resources`）後にテストを実行する
- JDK 21 / Maven 3.9 / H2 などビルド・テスト実行環境は整備済みである
- YAMLファイルの出力先は `src/test/java` 配下（Excelと同じ場所）とし、`nablarch.test.resource-root` の設定変更は不要
- 変換ツール（`nablarch-testing-converter`）の実行手順は `/home/k10949/workspace/nablarch-testing-converter/README.md` に従う

# Build / Test commands（README準拠）

対象プロジェクトの README（`README.md`）で確認した手順:

- エンティティ生成・DBセットアップ: `mvn generate-resources`
  - gspプラグインはライフサイクルにバインド済みのため `-P gsp` プロファイル指定は不要（README L50）。
  - Java 17+ 実行用のJVMオプションは `.mvn/jvm.config` で指定済み（README L52）。
  - `mvn compile` でも generate-resources 相当が実行される（README L60）。
- テスト実行: `mvn test`
- **ロケール**: NTFのテストはメッセージ解決に日本語ロケールを要する。`LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8` を設定して実行すること（`C.UTF-8` だとメッセージ解決に失敗しテストが落ちる）。本環境では `ja_JP.utf8` が利用可能。

# Rules

- commit every change; one completion marker per task
- push は行わない（コミットのみ実施し、リモートへの push は最後にユーザーが行う）
- テストクラス（`.java`）は変更しない
- `develop` から新規作成した `ntf-yaml-support` ブランチで作業する
- steering.md は各タスクで更新し、当該タスクのコミットに含める
- git add は明示的なパス指定で行う
- YAML変換後はサンプリングでExcelとYAMLの内容を比較してから削除する
- サンプリング比較で差異が見つかった場合は修正せずユーザーに報告して停止する
- テストはExcelファイルが存在しない状態で最終パスを確認する

# Tasks

### #1: 現状のビルド・テストで全PASSを確認する

**Purpose**: `develop` から `ntf-yaml-support` ブランチを作成し、変更前の状態で全テストがパスすることを確認する。

**Prerequisites**: none

**Steps**:

- [ ] `develop` ブランチの存在を確認し、そこから `ntf-yaml-support` ブランチを新規作成する
- [ ] 対象プロジェクトの README を確認し、ビルド・テストの実行手順を把握する
- [ ] 隣接プロジェクトの `.rn/` 配下MDを参照し、steering.md と scripts の初版を作成する
- [ ] `mvn generate-resources` でエンティティクラスを生成する
- [ ] `mvn test`（`LANG=ja_JP.UTF-8`）で全テストがパスすることを確認する
- [ ] 結果を `checks/task-1.md` に記録する（self-check列のみ）
- [ ] 変更ファイル（check ファイルおよび steering.md）をコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-1.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- `ntf-yaml-support` ブランチが `develop` ブランチから作成されている
- `mvn test` が全テストパスで終了している（BUILD SUCCESS）

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加

**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）を追加し、`nablarch-testing-converter` を `<build><plugins>` のビルドプラグインとして追加し、`unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser` コンポーネントを追加する。設定は隣接プロジェクト（web / batch）のYAML対応を参考にする。

**Prerequisites**: #1

**Steps**:

- [ ] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加する
- [ ] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` をビルドプラグインとして追加する
- [ ] `unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser` コンポーネントを追加する（web を参考）
- [ ] `mvn dependency:resolve` と `mvn help:describe` で yaml 依存と converter プラグインの `convert` ゴールが解決できることを確認する
- [ ] 変更したファイルをコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-2.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] language expert review (Agent: general-purpose / Opus 4.8)
- [ ] software-engineering expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている
- `unit-test.xml` に `yamlInterpreters` リストと `testDataParser` コンポーネント（`YamlTestDataParser`）が追加されている
- `mvn dependency:resolve` が成功し、converter プラグインの `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換

**Purpose**: `nablarch-testing-converter` を使って ExcelのテストデータをYAML形式に変換し、各テストのソースツリー内に配置して、変換内容をサンプリングで確認する。

**Prerequisites**: #2

**Steps**:

- [ ] 対象 Excel ファイルの所在を確認する
- [ ] 同名の `.xls` と `.xlsx` が併存していないか確認する（本プロジェクトは併存なし）
- [ ] converter の README 手順に従い、全対象ExcelファイルをYAML変換する
- [ ] 変換後、各YAMLファイルについてExcelの内容とサンプリング比較し一致を確認する（各ファイル最低3件、元データが3件未満の場合は元データ件数分）
- [ ] サンプリング比較結果を `checks/task-3.md` に記録する（差異があればユーザーに報告し修正せず止める）
- [ ] 変換したYAMLファイルをコミットする（Excelはまだ削除しない）
- [ ] self-check (OK/NG per completion criterion, record in checks/task-3.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- 変換対象の全Excelファイルに対応するYAMLファイルが生成されている
- サンプリング比較でExcelとYAMLの内容が一致している
- 変換後のYAMLファイルがコミットされている

### #4: Excelを削除する

**Purpose**: 変換対象だった Excel を削除してコミットする。

**Prerequisites**: #3

**Steps**:

- [ ] 変換対象となった Excel ファイルを削除する
- [ ] 削除結果（残存Excelが0件であること）を `checks/task-4.md` に記録する
- [ ] Excelファイル削除をコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-4.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- 変換対象だった全 Excel ファイルが削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる

**Purpose**: Excelなしで全テストがパスすることを確認する。

**Prerequisites**: #4

**Steps**:

- [ ] `mvn test` が BUILD SUCCESS になることを確認する
- [ ] テストログで YamlTestDataParser が実際に使われていることを確認する
- [ ] self-check (OK/NG per completion criterion, record in checks/task-5.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] language expert review (Agent: general-purpose / Opus 4.8)
- [ ] software-engineering expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- `mvn test` が BUILD SUCCESS で終了する（全テストパス、Excel ファイルなし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる

**Note（web プロジェクトでの既知事象）**: REST のリクエストテスト（`requestParams`）では、空マッピング `- {}` 行が `nablarch-testing-yaml` の空行スキップにより0行扱いとなり `IllegalArgumentException: Request parameter is not defined ...` を送出する事例が web であった。本プロジェクトで同様の失敗が出た場合は、解説書どおりマーカーカラムだけの行（例 `- "[no]": "1"`）へ直すこと。発生時はユーザーに報告のうえ対応する。

# Decisions

# State

- **Status**: in progress
- **Date**: 2026-10-02
- **Last completed**: #1 ベースライン確認 — `mvn test` 79件全PASS（Failures/Errors/Skipped: 0）。self-check OK / QA OK。ユーザーレビュー待ち。
- **Next**: #1 のユーザーレビュー → #2 設定変更（pom.xml / unit-test.xml）
- **Notes**: ブランチ `ntf-yaml-support` を `develop`（`f333474`）から作成。push は行わない方針（最後にユーザーがpush）。
