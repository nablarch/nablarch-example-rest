Rn version: 0.8.0

# Goal

NTF（Nablarch Testing Framework）のAI対応として、`nablarch-example-rest` の既存のExcelテストデータをYAML形式に移行する。
具体的には、`nablarch-testing-converter` を使って 5 つのExcelテストデータ（`.xlsx`/`.xls`）をYAMLに変換し、`nablarch-testing-yaml` を用いた設定変更のみで全テストがパスすることを確認する。
参照: `nablarch-example-web` の `ntf-yaml-support` ブランチ

# Acceptance criteria

- 全5つの `.xlsx`/`.xls` テストデータが変換後のYAMLテストデータに置き換えられ、テストクラスと同じディレクトリに配置されている
- `unit-test.xml` に `YamlTestDataParser` の設定が追加されている
- テストクラス本体（`.java` ファイル）の変更はなく、設定ファイルと依存関係の変更のみで移行が完結している
- `mvn test` が BUILD SUCCESS で終了する（リグレッションなし）
- 変換済みのYAMLファイルがリポジトリにコミットされている
- `pom.xml` に `nablarch-testing-yaml` と `nablarch-testing-converter` のtest依存が追加されている

# Assumptions

- `nablarch-testing-yaml:1.0.0-SNAPSHOT` および `nablarch-testing-converter:1.0.0-SNAPSHOT` はローカルの Maven リポジトリに既にインストール済み（`mvn install` 済み）
- `nablarch-example-rest` は H2 データベースを使用しており、`mvn test` 前に gsp プロファイルでのエンティティ生成が必要な場合がある
- YAMLファイルの出力先は `src/test/java` 配下（Excelと同じ場所）とし、`nablarch.test.resource-root` の設定変更は不要
- 変換ツール（`nablarch-testing-converter`）は Maven plugin として使用する
- 変更対象はこのリポジトリ（`nablarch-example-rest`）のみ

# Rules

- commit every change（プッシュなし）; one completion marker per task
- テストクラス（`.java`）は変更しない
- ブランチ `ntf-yaml-support` で作業し、全変更をこのブランチに含める
- 作業ディレクトリ: `C:\workspace\nablarch-example-rest`（WSLから `/mnt/c/workspace/nablarch-example-rest`）
- Java: OpenJDK 17（プロジェクトのJavaバージョン）
- `nablarch-example-web` の `ntf-yaml-support` ブランチの変更内容を参考にする
- 推測で作業しない。既存コードやwebブランチの内容を参照してから進める

# Tasks

### #1: 現状のビルド・テストで全PASSを確認する

**Purpose**: 移行前のベースラインとして、`nablarch-example-rest` のビルドとテストが全てパスすることを確認する。

**Prerequisites**: none

**Steps**:

- [x] `mvn -P gsp clean generate-resources` を実行してエンティティクラスを生成する
- [x] `mvn test` を実行する
- [x] 全テストがパスすることを確認する（失敗があれば報告して止まる）
- [x] self-check (OK/NG per completion criterion, record in checks/task-1.md)
- [x] QA expert review (subagent)
- [x] user review

**Completion criteria**:

- `mvn test` が BUILD SUCCESS で終了する

### #2: pom.xml に nablarch-testing-yaml / nablarch-testing-converter の依存を追加する

**Purpose**: `pom.xml` に `nablarch-testing-yaml` と `nablarch-testing-converter` をtest依存として追加する。

**Prerequisites**: #1

**Steps**:

- [x] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` をtest scopeで追加する
- [x] `pom.xml` に `nablarch-testing-converter:1.0.0-SNAPSHOT` をpluginとして追加する
- [x] `mvn dependency:resolve` で依存が解決できることを確認する
- [x] self-check (OK/NG per completion criterion, record in checks/task-2.md)
- [x] QA expert review (subagent)
- [x] software-engineering expert review (subagent)
- [x] user review

**Completion criteria**:

- `pom.xml` に `nablarch-testing-yaml` と `nablarch-testing-converter` の設定が追加されている
- `mvn dependency:resolve` が成功する

### #3: ExcelテストデータをYAMLに変換してリポジトリに配置する

**Purpose**: `nablarch-testing-converter` を使って5つのExcelテストデータをYAMLに変換し、各テストのソースツリー内に配置する。

**Prerequisites**: #2

**Steps**:

- [x] `ProjectActionTest.xlsx` をYAMLに変換する
- [x] `ProjectFormTest.xls` をYAMLに変換する
- [x] `ProjectRenameFormTest.xls` をYAMLに変換する
- [x] `ProjectSearchFormTest.xls` をYAMLに変換する
- [x] `ProjectUpdateFormTest.xls` をYAMLに変換する
- [x] 変換済みのYAMLファイルを各テストクラスと同じディレクトリに配置する
- [x] 変換済みYAMLファイルをgitに追加してコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-3.md)
- [x] QA expert review (subagent)
- [x] user review

**Completion criteria**:

- 5つの変換済みYAMLが `src/test/java/com/nablarch/example/` 配下に存在する
- 変換されたYAMLがスキーマに対して有効である（変換ツールが例外なく正常終了していることで確認）
- 変換済みYAMLがgitでtracked filesとして存在する

### #4: ExcelとYAMLの内容一致をサンプリング確認して xlsx/xls を削除する

**Purpose**: 変換済みYAMLがExcelの内容を正しく再現していることをサンプリングで確認し、xlsx/xls を削除してコミットする。

**Prerequisites**: #3

**Steps**:

- [x] 各 xlsx/xls について、代表的なシート・行をいくつかピックアップしてYAMLと突き合わせ、値・型・行数が一致することを確認する（比較スクリプトで全2465フィールドを機械比較・不一致0）
- [x] 不一致があれば報告して止まる（不一致0のため続行）
- [x] 全 `.xlsx`/`.xls` ファイルを削除する
- [x] 削除をコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-4.md)
- [x] QA expert review (subagent)
- [x] user review

**Completion criteria**:

- サンプリングした全行でExcelとYAMLの値・行数が一致している
- 全 `.xlsx`/`.xls` ファイルが削除されている
- 削除がコミットされている

### #5: unit-test.xml に YamlTestDataParser を設定してYAMLテストデータで全テストをパスさせる

**Purpose**: `unit-test.xml` に `YamlTestDataParser` を設定し、xlsx/xls なしで全テストがパスすることを確認する。

**Prerequisites**: #4

**Steps**:

- [x] `src/test/resources/unit-test.xml` に `YamlTestDataParser` の設定を追加する（`nablarch-example-web` の `ntf-yaml-support` ブランチの変更を参照）。yamlInterpreters（QuotationTrimmer 除く）をインライン定義し testDataParser をオーバーライド
- [x] （追加対応）`nablarch-testing-rest` の testDataParser 委譲版が必要と判明。`fix-testdataparser-usage` ブランチをローカルビルドして `.m2` に install（`6-NEXT-SNAPSHOT`）し、pom で version 明示
- [x] `mvn test` が BUILD SUCCESS になることを確認する（Tests run: 79, Failures: 0, Errors: 0）
- [x] テストログで YamlTestDataParser が実際に使われていることを確認する
- [x] self-check (OK/NG per completion criterion, record in checks/task-5.md)
- [x] QA expert review (subagent)
- [x] software-engineering expert review (subagent)
- [x] user review（SNAPSHOT 依存の取り扱い方針の合意を含む）: 2026-09-24 承認

**Completion criteria**:

- `src/test/resources/unit-test.xml` に `testDataParser` として `YamlTestDataParser` が定義されている
- `mvn test` が BUILD SUCCESS で終了する（全テストパス、xlsx/xls なし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる

**追加対応（RestTestSupport の testDataParser 委譲）**:

- **事象**: unit-test.xml に YamlTestDataParser を設定しても、`RestTestSupport` ベースの `ProjectActionTest` 4件が `test data file open failed.` で失敗。BOM 登録の `nablarch-testing-rest:2.0.0` は `RestTestSupport.isExisting()` が Apache POI で `.xlsx`/`.xls` を直接ハードコードで開く実装で、SystemRepository の `testDataParser`（YamlTestDataParser）を経由しないため。task #4 で xls 削除済みのため `FileNotFoundException`。
- **対応**: `nablarch-testing-rest` の `fix-testdataparser-usage` ブランチ（`isExisting()` を `TestDataParser#isResourceExisting()` 委譲に統一、POI 直読み `getSheet()` 除去、Excel 経路は後方互換維持）を `C:\workspace\nablarch-testing-rest` に clone・checkout し `mvn -Dmaven.test.skip=true install` で `6-NEXT-SNAPSHOT` を `.m2` に導入。example の pom で当該 version を明示（BOM 2.0.0 を上書き）。推移依存 `nablarch-testing` は 2.2.0 のまま。
- **未決（ユーザー判断）**: pom が未リリースの feature ブランチ由来 `6-NEXT-SNAPSHOT` に依存するため、当該ブランチ未 install の環境（CI 含む）ではビルド不能。正式リリース版が出れば version 明示を外して BOM 解決に戻す。方針（正式リリース待ち／CI での事前 install／継承による回避）の合意が必要。

# State

- **Status**: completed
- **Date**: 2026-09-24
- **Last completed**: #5 承認済み。Acceptance criteria 全6項目を検証・充足（下記）。全タスク #1〜#5 完了
- **Acceptance criteria 検証結果（2026-09-24）**:
  - [x] 全5 Excel が YAML（計12ファイル）に置換され各テストクラスと同一ディレクトリに配置
  - [x] `unit-test.xml` に `YamlTestDataParser` 設定あり
  - [x] テスト `.java` 変更なし（`git diff main...HEAD` に `.java` 0件。設定・依存・データのみ）
  - [x] `mvn test` 79件全緑 BUILD SUCCESS（リグレッションなし）
  - [x] 変換済み YAML が tracked としてコミット済み
  - [x] pom に `nablarch-testing-yaml` / `nablarch-testing-converter` の test 依存あり
- **Next**: （任意）framework の `nablarch-testing-rest` 正式リリース後に pom の `6-NEXT-SNAPSHOT` version 明示を外して BOM 解決へ戻す。PR 作成はユーザー指示待ち
- **Notes**: `.m2` に `nablarch-testing-rest:6-NEXT-SNAPSHOT`（`fix-testdataparser-usage` ブランチ `cbad873` を `mvn -Dmaven.test.skip=true install`）が入った状態で 79件全緑。再開時にこれが失われていれば当該ブランチを再 install すること。比較スクリプト用 venv は `~/.cache/ntf-rest-yaml-venv`（リポジトリ外）。
