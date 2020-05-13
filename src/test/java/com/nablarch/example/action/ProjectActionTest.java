package com.nablarch.example.action;

import com.nablarch.example.form.ProjectForm;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.RestMockHttpRequest;
import nablarch.fw.web.RestMockHttpRequestBuilder;
import nablarch.test.core.http.RestTestSupport;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URISyntaxException;

public class ProjectActionTest extends RestTestSupport {
    @Test
    public void プロジェクト一覧が取得できること() throws IOException, URISyntaxException, JSONException {
        RestMockHttpRequestBuilder requestBuilder = getHttpRequestBuilder();

        HttpResponse response = execute(requestBuilder.get("/projects"));

        assertStatusCode(HttpResponse.Status.OK, response);
        JSONAssert.assertEquals(readFile("プロジェクト一覧が取得できること.json")
                , response.getBodyString()
                , JSONCompareMode.LENIENT);
    }

    @Test
    public void プロジェクトを新規登録できること() throws IOException, URISyntaxException, JSONException {
        RestMockHttpRequestBuilder requestBuilder = getHttpRequestBuilder();

        RestMockHttpRequest request = requestBuilder
                .post("/projects")
//                .setBody(createInsertProject());
                .setBody("{\"projectName\":\"プロジェクト９９９\",\"projectType\":\"development\",\"projectClass\":\"s\",\"projectManager\":\"鈴木\",\"projectLeader\":\"佐藤\",\"clientId\":\"10\",\"projectStartDate\":\"20160101\",\"projectEndDate\":\"20160331\",\"note\":\"備考９９９\",\"sales\":\"10000\",\"costOfGoodsSold\":\"1000\",\"sga\":\"2000\",\"allocationOfCorpExpenses\":\"3000\"}");

        HttpResponse response = execute(request);

        assertStatusCode(HttpResponse.Status.CREATED, response);

        HttpResponse getResponse = execute(requestBuilder.get("/projects"));

        assertStatusCode(HttpResponse.Status.OK, getResponse);
        JSONAssert.assertEquals(readFile("プロジェクトを新規登録できること.json")
                , getResponse.getBodyString()
                , JSONCompareMode.LENIENT);
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
}
