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

**Completion criteria**:

- `mvn test` が BUILD SUCCESS で終了する

### #2: pom.xml に nablarch-testing-yaml / nablarch-testing-converter の依存を追加する

**Purpose**: `pom.xml` に `nablarch-testing-yaml` と `nablarch-testing-converter` をtest依存として追加する。

**Prerequisites**: #1

**Steps**:

- [ ] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` をtest scopeで追加する
- [ ] `pom.xml` に `nablarch-testing-converter:1.0.0-SNAPSHOT` をpluginとして追加する
- [ ] `mvn dependency:resolve` で依存が解決できることを確認する
- [ ] self-check (OK/NG per completion criterion, record in checks/task-2.md)

**Completion criteria**:

- `pom.xml` に `nablarch-testing-yaml` と `nablarch-testing-converter` の設定が追加されている
- `mvn dependency:resolve` が成功する

### #3: ExcelテストデータをYAMLに変換してリポジトリに配置する

**Purpose**: `nablarch-testing-converter` を使って5つのExcelテストデータをYAMLに変換し、各テストのソースツリー内に配置する。

**Prerequisites**: #2

**Steps**:

- [ ] `ProjectActionTest.xlsx` をYAMLに変換する
- [ ] `ProjectFormTest.xls` をYAMLに変換する
- [ ] `ProjectRenameFormTest.xls` をYAMLに変換する
- [ ] `ProjectSearchFormTest.xls` をYAMLに変換する
- [ ] `ProjectUpdateFormTest.xls` をYAMLに変換する
- [ ] 変換済みのYAMLファイルを各テストクラスと同じディレクトリに配置する
- [ ] 変換済みYAMLファイルをgitに追加してコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-3.md)

**Completion criteria**:

- 5つの変換済みYAMLが `src/test/java/com/nablarch/example/` 配下に存在する
- 変換済みYAMLがgitでtracked filesとして存在する

### #4: ExcelとYAMLの内容一致をサンプリング確認して xlsx/xls を削除する

**Purpose**: 変換済みYAMLがExcelの内容を正しく再現していることをサンプリングで確認し、xlsx/xls を削除してコミットする。

**Prerequisites**: #3

**Steps**:

- [ ] 各 xlsx/xls について、代表的なシート・行をいくつかピックアップしてYAMLと突き合わせ、値・型・行数が一致することを確認する（全件でなくサンプリングで可）
- [ ] 不一致があれば報告して止まる
- [ ] 全 `.xlsx`/`.xls` ファイルを削除する
- [ ] 削除をコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-4.md)

**Completion criteria**:

- サンプリングした全行でExcelとYAMLの値・行数が一致している
- 全 `.xlsx`/`.xls` ファイルが削除されている
- 削除がコミットされている

### #5: unit-test.xml に YamlTestDataParser を設定してYAMLテストデータで全テストをパスさせる

**Purpose**: `unit-test.xml` に `YamlTestDataParser` を設定し、xlsx/xls なしで全テストがパスすることを確認する。

**Prerequisites**: #4

**Steps**:

- [ ] `src/test/resources/unit-test.xml` に `YamlTestDataParser` の設定を追加する（`nablarch-example-web` の `ntf-yaml-support` ブランチの変更を参照）
- [ ] `mvn test` が BUILD SUCCESS になることを確認する
- [ ] self-check (OK/NG per completion criterion, record in checks/task-5.md)

**Completion criteria**:

- `src/test/resources/unit-test.xml` に `testDataParser` として `YamlTestDataParser` が定義されている
- `mvn test` が BUILD SUCCESS で終了する（全テストパス、xlsx/xls なし）

# State

- **Status**: in_progress
- **Date**: 2026-09-18
- **Last completed**: #1 ベースライン確認（Tests run: 79, Failures: 0）
- **Next**: #2 pom.xml に依存追加
