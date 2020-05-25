package com.nablarch.example.action;

import com.jayway.jsonassert.JsonAsserter;
import com.jayway.jsonpath.JsonPath;
import com.nablarch.example.entity.Project;
import com.nablarch.example.form.ProjectForm;
import com.nablarch.example.form.ProjectUpdateForm;
import nablarch.core.beans.BeanUtil;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.RestMockHttpRequestBuilder;
import nablarch.test.core.http.RestTestSupport;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ProjectActionTest extends RestTestSupport {
    @Test
    public void プロジェクト一覧が取得できること() {
        RestMockHttpRequestBuilder builder = getHttpRequestBuilder();

        String message = "プロジェクト一覧取得";
        HttpResponse response = sendRequest(builder.get("/projects"));
        assertStatusCode(message, HttpResponse.Status.OK.getStatusCode(), response);

        assertThat(response.getBodyString(), hasJsonPath("$", hasSize(10)));
        // JSONファイルとのアサート。ファイル名を省略するとテストメソッド名と同名ファイルを検索する。
        assertJsonEquals(message, response);

        with(response.getBodyString())
                .assertThat("$", hasSize(10))
                .assertThat("$", hasItems(
                        hasEntry("projectId", 1)
                        , hasEntry("projectId", 2)
                        , hasEntry("projectId", 3)
                        , hasEntry("projectId", 4)
                        , hasEntry("projectId", 5)
                        , hasEntry("projectId", 6)
                        , hasEntry("projectId", 7)
                        , hasEntry("projectId", 8)
                        , hasEntry("projectId", 9)
                        , hasEntry("projectId", 10)
                        )
                );
    }

    @Test
    public void プロジェクトを新規登録できること() {
        RestMockHttpRequestBuilder builder = getHttpRequestBuilder();

        // プロジェクトが1件も登録されていないこと
        String message1 = "プロジェクト一覧取得(登録前)";
        HttpResponse beforeRegisterResponse = sendRequest(builder.get("/projects"));
        assertStatusCode(message1, HttpResponse.Status.OK.getStatusCode(), beforeRegisterResponse);
        assertThat(beforeRegisterResponse.getBodyString(), hasJsonPath("$", empty()));

        // 新規登録
        String message2 = "プロジェクト新規登録";
        ProjectForm projectForm = createInsertProject();
        HttpResponse registerResponse = sendRequest(builder.post("/projects").setBody(projectForm));
        assertStatusCode(message2, HttpResponse.Status.CREATED.getStatusCode(), registerResponse);

        // 登録したプロジェクトが取得できること
        String message3 = "プロジェクト一覧取得(登録後)";
        HttpResponse afterRegisterResponse = sendRequest(builder.get("/projects"));
        assertStatusCode(message3, HttpResponse.Status.OK.getStatusCode(), afterRegisterResponse);
        assertProjectEquals(BeanUtil.createAndCopy(Project.class, projectForm), afterRegisterResponse);
    }

    @Test
    public void プロジェクトを更新できること() throws IOException {
        RestMockHttpRequestBuilder builder = getHttpRequestBuilder();


        String project001Uri = "/projects?projectName=プロジェクト００１";
        String project888Uri = "/projects?projectName=プロジェクト８８８";

        String message1 = "変更前に変更しようとするプロジェクト名に一致するデータが存在しないこと";
        HttpResponse projectNameNotFoundResponse = sendRequest(builder.get(project888Uri));
        assertStatusCode(message1, HttpResponse.Status.OK.getStatusCode(), projectNameNotFoundResponse);
        assertThat(projectNameNotFoundResponse.getBodyString(), hasJsonPath("$", empty()));

        String message2 = "変更対象取得";
        HttpResponse getTargetProjectResponse = sendRequest(builder.get(project001Uri));
        assertStatusCode(message2, HttpResponse.Status.OK.getStatusCode(), getTargetProjectResponse);
        assertThat(getTargetProjectResponse.getBodyString(), isJson(allOf(
                withJsonPath("$", hasSize(1))
                , withJsonPath("$[0]", hasEntry("projectName", "プロジェクト００１")))));

        int projectId = JsonPath.read(getTargetProjectResponse.getBodyStream(), "$[0].projectId");
        ProjectUpdateForm updateForm = setUpdateProject(String.valueOf(projectId));

        String message3 = "プロジェクト更新";
        HttpResponse updateResponse = sendRequest(builder.put("/projects").setBody(updateForm));
        assertStatusCode(message3, HttpResponse.Status.OK.getStatusCode(), updateResponse);

        String message4 = "変更前のプロジェクト名に一致するデータが存在しないこと";
        HttpResponse previousNameNotFoundResponse = sendRequest(builder.get(project001Uri));
        assertStatusCode(message4, HttpResponse.Status.OK.getStatusCode(), previousNameNotFoundResponse);
        assertThat(previousNameNotFoundResponse.getBodyString(), hasJsonPath("$", empty()));

        String message5 = "取得したプロジェクトが変更した内容と一致すること";
        HttpResponse projectNameFoundResponse = sendRequest(builder.get(project888Uri));
        assertStatusCode(message5, HttpResponse.Status.OK.getStatusCode(), projectNameFoundResponse);
        // JSONファイルを読み込んでアサートする
        assertJsonEquals(message5, "プロジェクト８８８.json", projectNameFoundResponse);
        // Projectとのアサート
        assertProjectEquals(BeanUtil.createAndCopy(Project.class, updateForm), projectNameFoundResponse);
    }


    private void assertProjectEquals(Project expected, HttpResponse response) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        assertThat(response.getBodyString(), isJson(allOf(
                withJsonPath("$", hasSize(1))
                , withJsonPath("$[0].projectName", equalTo(expected.getProjectName()))
                , withJsonPath("$[0].projectType", equalTo(expected.getProjectType()))
                , withJsonPath("$[0].projectClass", equalTo(expected.getProjectClass()))
                , withJsonPath("$[0].projectStartDate", equalTo(dateFormat.format(expected.getProjectStartDate())))
                , withJsonPath("$[0].projectEndDate", equalTo(dateFormat.format(expected.getProjectEndDate())))
                , withJsonPath("$[0].clientId", equalTo(expected.getClientId()))
                , withJsonPath("$[0].projectManager", equalTo(expected.getProjectManager()))
                , withJsonPath("$[0].projectLeader", equalTo(expected.getProjectLeader()))
                , withJsonPath("$[0].note", equalTo(expected.getNote()))
                , withJsonPath("$[0].sales", equalTo(expected.getSales()))
                , withJsonPath("$[0].costOfGoodsSold", equalTo(expected.getCostOfGoodsSold()))
                , withJsonPath("$[0].sga", equalTo(expected.getSga()))
                , withJsonPath("$[0].allocationOfCorpExpenses", equalTo(expected.getAllocationOfCorpExpenses()))
                ))
        );
        with(response.getBodyString())
                .assertNotDefined("$[0].hoge")
                .assertThat("$", hasSize(1))
                .assertThat("$[0]", hasEntry("projectName", expected.getProjectName()))
                .assertThat("$[0]", hasEntry("projectType", expected.getProjectType()))
                .assertThat("$[0]", hasEntry("projectClass", expected.getProjectClass()))
                .assertThat("$[0]", hasEntry("projectStartDate", dateFormat.format(expected.getProjectStartDate())))
                .assertThat("$[0]", hasEntry("projectEndDate", dateFormat.format(expected.getProjectEndDate())))
                .assertThat("$[0]", hasEntry("clientId", expected.getClientId()))
                .assertThat("$[0]", hasEntry("projectManager", expected.getProjectManager()))
                .assertThat("$[0]", hasEntry("projectLeader", expected.getProjectLeader()))
                .assertThat("$[0]", hasEntry("note", expected.getNote()))
                .assertThat("$[0]", hasEntry("sales", expected.getSales()))
                .assertThat("$[0]", hasEntry("costOfGoodsSold", expected.getCostOfGoodsSold()))
                .assertThat("$[0]", hasEntry("sga", expected.getSga()))
                .assertThat("$[0]", hasEntry("allocationOfCorpExpenses", expected.getAllocationOfCorpExpenses()));

        final Map<String, Object> expectedValues = BeanUtil.createMapAndCopy(expected);
        final JsonAsserter asserter = with(response.getBodyString());
        expectedValues.entrySet().stream()
                .filter(e -> !(e.getKey().endsWith("Date") || "projectId".equals(e.getKey())))
                .forEach(e -> asserter.assertThat("$[0]", hasEntry(e.getKey(), e.getValue())));
    }

    /**
     * 登録用プロジェクト情報生成
     *
     * @return 登録用プロジェクト情報
     */
    private static ProjectForm createInsertProject() {
        ProjectForm form = new ProjectForm();
        form.setProjectName("プロジェクト９９９");
        form.setProjectType("development");
        form.setProjectClass("s");
        form.setProjectStartDate("20160101");
        form.setProjectEndDate("20160331");
        form.setClientId("10");
        form.setProjectManager("鈴木");
        form.setProjectLeader("佐藤");
        form.setNote("備考９９９");
        form.setSales("10000");
        form.setCostOfGoodsSold("1000");
        form.setSga("2000");
        form.setAllocationOfCorpExpenses("3000");
        return form;
    }

    /**
     * 更新用プロジェクト情報設定
     *
     * @param projectId 更新対象プロジェクトID
     * @return 更新用プロジェクト情報
     */
    private static ProjectUpdateForm setUpdateProject(String projectId) {
        ProjectUpdateForm form = new ProjectUpdateForm();
        form.setProjectId(projectId);
        form.setProjectName("プロジェクト８８８");
        form.setProjectType("development");
        form.setProjectClass("a");
        form.setProjectStartDate("20150101");
        form.setProjectEndDate("20150331");
        form.setClientId("1");
        form.setProjectManager("佐藤");
        form.setProjectLeader("鈴木");
        form.setNote("備考８８８");
        form.setSales("20000");
        form.setCostOfGoodsSold("2000");
        form.setSga("3000");
        form.setAllocationOfCorpExpenses("4000");
        return form;
    }
}
