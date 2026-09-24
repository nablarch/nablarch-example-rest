# task-5 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| `src/test/resources/unit-test.xml` に `testDataParser` として `YamlTestDataParser` が定義されている | OK | import 群直後に `yamlInterpreters` リスト（QuotationTrimmer 除く）と `<component name="testDataParser" class="nablarch.test.core.reader.YamlTestDataParser" autowireType="None">`（dbInfo/interpreters/defaultValues=BasicDefaultValues）を追加 | OK | 行36-43に定義を確認。起動時オーバーライドログも確認 |
| `mvn test` が BUILD SUCCESS で終了する（全テストパス、xlsx/xls なし） | OK | `Tests run: 79, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`。ProjectActionTest 4件・全 Form テスト 75件パス。xlsx/xls は task #4 で削除済み | OK | BUILD SUCCESS + 79件全緑の証跡を確認。xlsx/xls は task #4 で除去済みのため再確認不要 |
| テストログで YamlTestDataParser が使われていることが確認できる | OK | `override component classname was not matched. replace all component configuration. component name = testDataParser ... override component definition classname = nablarch.test.core.reader.YamlTestDataParser` を出力。加えて実行時に `nablarch.test.core.reader.YamlTestDataParser` の DEBUG ログ（`Skipping table data initialization because preparation data is not found. resource=[ProjectActionTest/プロジェクト一覧が取得できること]`）を確認 | OK | オーバーライドログ + YamlTestDataParser 自身の DEBUG ログの2段階で実使用を確認（Excel時代には出ないメッセージ） |

## 追加で必要になったフレームワーク側修正（RestTestSupport の testDataParser 委譲）

### 事象
`unit-test.xml` に YamlTestDataParser を設定しただけでは `ProjectActionTest`（`RestTestSupport` ベース）4件が
`java.lang.RuntimeException: test data file open failed.` で失敗した。原因は `nablarch-testing-rest:2.0.0`（BOM 登録の
リリース版）の `RestTestSupport.isExisting()` がシート存在確認に Apache POI (`WorkbookFactory`) で `.xlsx`/`.xls` を
**直接ハードコードで開く**実装で、SystemRepository の `testDataParser`（YamlTestDataParser）を経由しないため。
task #4 で xls を削除済みのため `FileNotFoundException`（`ProjectActionTest.xls`）となっていた。
Form/Entity 系テスト（DbAccessTestSupport 経由）は testDataParser を使うため YAML で 75件パスしていた。

### 対応
`nablarch-testing-rest` の `fix-testdataparser-usage` ブランチ（`RestTestSupport.isExisting()` を
`TestDataParser#isResourceExisting()` 委譲に統一し、POI 直読み `getSheet()` を除去。Excel 経路は後方互換で維持）を
`C:\workspace\nablarch-testing-rest` に clone・checkout し、`mvn -Dmaven.test.skip=true install` で
`nablarch-testing-rest:6-NEXT-SNAPSHOT` をローカル `.m2` に導入。example の `pom.xml` で当該依存の version を
`6-NEXT-SNAPSHOT` に明示（BOM 2.0.0 を上書き）。推移依存 `nablarch-testing` は `2.2.0` のまま（ドリフトなし）。

### 検証
- 導入 jar の `RestTestSupport.class` に `WorkbookFactory` 参照が無いことをバイト列で確認（修正版であること）
- `mvn dependency:tree`: `nablarch-testing-rest:jar:6-NEXT-SNAPSHOT:test` / `nablarch-testing:jar:2.2.0:test`
- `mvn test`: 79件全緑 BUILD SUCCESS

### 未決（ユーザーレビュー対象）
example の `pom.xml` が **未リリースの feature ブランチ由来 `6-NEXT-SNAPSHOT`** に依存するため、当該ブランチを
`.m2` に install していない環境では `ProjectActionTest` がビルド不能。正式リリース版（testDataParser 委譲を含む
`nablarch-testing-rest`）が出れば version 明示を外して BOM 解決へ戻すべき。この扱いはユーザー判断。
web 側も同様にローカル install した framework SNAPSHOT に依存して検証していた（web steering task #6/#7 の Notes 参照）。

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | OK | BUILD SUCCESS + 79件全緑。YamlTestDataParser 実使用を2段階のログで確認。xlsx/xls は task #4 で除去済み |
| Edge case coverage | OK | QuotationTrimmer 除外は YAML 1.2 の二重strip回避で妥当（web と一貫）。systemTimeProvider/setUpDateTime は import 経路で解決。ProjectActionTest の YAML 無しメソッドは graceful skip をログで実証。孤立 YAML なし（YAML↔メソッド突合済み）。**cosmetic 注記**: `setUpDb.yaml` の数値カラムが `"1.0"` 形式（Excel float 由来）で expected 側の整数形式と不揃い。機能影響なし・スコープ外だが将来リファクタ候補 |

## Expert Reviews (code changes only)

### Language Expert

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Best practices | N/A | Java/GWT コード変更なし（XML 設定 + pom 依存 version 明示のみ） |

### Software-engineering Expert

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Separation of concerns | OK | yamlInterpreters は test-data-interpreter.xml の named component に依存せず自己完結。testDataParser 上書きで Excel 経路（BasicTestDataParser）は orphaned となり干渉なし |
| System integrity | OK | autowireType="None" は全プロパティ明示のため適切。testDataParser→dbInfo の前方参照は DI が全定義収集後に解決するため動作問題なし（79件全緑）。systemTimeProvider/setUpDateTime は import で解決。6-NEXT-SNAPSHOT×testing 2.2.0 のバイナリ非互換なし（fixブランチは testing 2.2.0 依存でビルド） |
| Maintainability | OK（改善反映済み） | pom コメントを「BOM 2.0.0 の意図的上書き」「正式リリース後に version 明示を外して BOM 解決へ戻す TODO」を含む記述に更新。SNAPSHOT 明示パターンと一貫 |

### Software-engineering Expert が挙げた High 項目（6-NEXT-SNAPSHOT 再現性リスク）

`fix-testdataparser-usage`（未マージ feature ブランチ）由来の `6-NEXT-SNAPSHOT` に依存するため、当該ブランチを
`.m2` に install していない環境（CI 含む）では `ProjectActionTest` がビルド不能。動作面は問題ないが、
**メインブランチへのマージ前に「framework の正式リリース待ち／CI での事前 install／継承による回避」いずれの方針を採るか
ユーザー合意が必要**。→ 下記「未決」およびユーザーレビューで判断を仰ぐ。

## Overall Verdict

- Self-check: OK
- QA: OK
- Language expert: N/A
- Software-engineering expert: 条件付き OK（技術面 OK、6-NEXT-SNAPSHOT 再現性は方針合意が必要）
- Ready for user review: Yes（未決の SNAPSHOT 取り扱い方針をユーザー判断に付す）
