package com.nablarch.example.action;

import com.jayway.jsonpath.JsonPath;
import com.nablarch.example.entity.Project;
import com.nablarch.example.form.ProjectForm;
import com.nablarch.example.form.ProjectUpdateForm;
import nablarch.core.beans.BeanUtil;
import nablarch.fw.web.HttpResponse;
import nablarch.test.core.http.RestTestSupport;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.text.SimpleDateFormat;

import static com.jayway.jsonassert.JsonAssert.with;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static nablarch.test.Assertion.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ProjectActionTest extends RestTestSupport {
    @Test
    public void プロジェクト一覧が取得できること() {
        String message = "プロジェクト一覧取得";
        HttpResponse response = sendRequest(get("/projects"));
        assertStatusCode(message, HttpResponse.Status.OK.getStatusCode(), response);

        assertThat(response.getBodyString(), hasJsonPath("$", hasSize(10)));
        try {
            JSONAssert.assertEquals(message, readTextResource("プロジェクト一覧が取得できること.json")
                    , response.getBodyString(), JSONCompareMode.LENIENT);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void プロジェクトを新規登録できること() {
        // プロジェクトが1件も登録されていないこと
        String message1 = "プロジェクト一覧取得(登録前)";
        HttpResponse beforeRegisterResponse = sendRequest(get("/projects"));
        assertStatusCode(message1, HttpResponse.Status.OK.getStatusCode(), beforeRegisterResponse);
        assertThat(beforeRegisterResponse.getBodyString(), hasJsonPath("$", empty()));

        // 新規登録
        String message2 = "プロジェクト新規登録";
        ProjectForm projectForm = createInsertProject();
        HttpResponse registerResponse = sendRequest(post("/projects").setBody(projectForm));
        assertStatusCode(message2, HttpResponse.Status.CREATED.getStatusCode(), registerResponse);

        // 登録したプロジェクトが取得できること
        String message3 = "プロジェクト一覧取得(登録後)";
        HttpResponse afterRegisterResponse = sendRequest(get("/projects"));
        assertStatusCode(message3, HttpResponse.Status.OK.getStatusCode(), afterRegisterResponse);
        assertProjectEquals(BeanUtil.createAndCopy(Project.class, projectForm), afterRegisterResponse);

        assertTableEquals("プロジェクトを新規登録できること");
    }

    @Test
    public void プロジェクトを更新できること() throws IOException {
        String project001Uri = "/projects?projectName=プロジェクト００１";
        String project888Uri = "/projects?projectName=プロジェクト８８８";

        String message1 = "変更前に変更しようとするプロジェクト名に一致するデータが存在しないこと";
        HttpResponse projectNameNotFoundResponse = sendRequest(get(project888Uri));
        assertStatusCode(message1, HttpResponse.Status.OK.getStatusCode(), projectNameNotFoundResponse);
        assertThat(message1, projectNameNotFoundResponse.getBodyString(), hasJsonPath("$", empty()));

        String message2 = "変更対象取得";
        HttpResponse getTargetProjectResponse = sendRequest(get(project001Uri));
        assertStatusCode(message2, HttpResponse.Status.OK.getStatusCode(), getTargetProjectResponse);
        assertThat(message2, getTargetProjectResponse.getBodyString(), isJson(allOf(
                withJsonPath("$", hasSize(1))
                , withJsonPath("$[0]", hasEntry("projectName", "プロジェクト００１")))));

        int projectId = JsonPath.read(getTargetProjectResponse.getBodyStream(), "$[0].projectId");
        ProjectUpdateForm updateForm = setUpdateProject(String.valueOf(projectId));

        String message3 = "プロジェクト更新";
        HttpResponse updateResponse = sendRequest(put("/projects").setBody(updateForm));
        assertStatusCode(message3, HttpResponse.Status.OK.getStatusCode(), updateResponse);

        String message4 = "変更前のプロジェクト名に一致するデータが存在しないこと";
        HttpResponse previousNameNotFoundResponse = sendRequest(get(project001Uri));
        assertStatusCode(message4, HttpResponse.Status.OK.getStatusCode(), previousNameNotFoundResponse);
        assertThat(message4, previousNameNotFoundResponse.getBodyString(), hasJsonPath("$", empty()));

        String message5 = "取得したプロジェクトが変更した内容と一致すること";
        HttpResponse projectNameFoundResponse = sendRequest(get(project888Uri));
        assertStatusCode(message5, HttpResponse.Status.OK.getStatusCode(), projectNameFoundResponse);
        // Projectとのアサート
        assertProjectEquals(BeanUtil.createAndCopy(Project.class, updateForm), projectNameFoundResponse);
    }


    private void assertProjectEquals(Project expected, HttpResponse response) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        with(response.getBodyString())
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
