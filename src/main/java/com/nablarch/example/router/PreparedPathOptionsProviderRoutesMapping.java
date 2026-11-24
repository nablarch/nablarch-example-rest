package com.nablarch.example.router;

import java.util.Collections;
import java.util.Map;

import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpCookie;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.integration.router.PathOptionsProviderRoutesMapping;

/**
 * 起動時にルートの探索木を作っておく {@link PathOptionsProviderRoutesMapping}。
 * <p>
 * http-request-router は、探索木を最初のリクエストを処理するときに、ロックをかけずに作る。
 * 起動直後に複数のリクエストが同時に来ると探索木が壊れることがあり、
 * 壊れた探索木は作り直されないため、一部のパスが 404 を返し続ける。
 * <p>
 * このクラスは {@link #initialize()} の最後に 1 回だけ振り分けを行う。
 * {@code initialize()} はリクエストを受け付ける前に 1 つのスレッドで呼ばれるので、
 * 探索木は競合せずに作られる。
 *
 * @author Nabu Rakutaro
 */
public class PreparedPathOptionsProviderRoutesMapping extends PathOptionsProviderRoutesMapping {

    @Override
    public void initialize() {
        super.initialize();
        prepareRoutingTree();
    }

    /**
     * ダミーのリクエストで 1 回だけ振り分けを行い、探索木を作る。
     * <p>
     * 振り分けの結果は使わない。一致するルートが無ければ 404 の例外になるが、探索木は作られている。
     */
    private void prepareRoutingTree() {
        try {
            getHandlerClass(new RoutingTreePreparationRequest(), new ExecutionContext());
        } catch (HttpErrorResponse notFound) {
            // 一致するルートが無い場合。探索木は作られているので問題ない
        } catch (ClassNotFoundException e) {
            // ダミーのパスがルートに一致し、そのクラスが無い場合だけ起こる。探索木は作られている
        }
    }

    /**
     * 探索木を作るためだけに使うリクエスト。パス "/" と HTTP メソッド GET だけを持つ。
     */
    private static final class RoutingTreePreparationRequest extends HttpRequest {

        /**
         * コンストラクタ。
         */
        RoutingTreePreparationRequest() {
            setRequestUri("/");
        }

        @Override
        public String getMethod() {
            return "GET";
        }

        @Override
        public String getHttpVersion() {
            return "HTTP/1.1";
        }

        @Override
        public Map<String, String[]> getParamMap() {
            return Collections.emptyMap();
        }

        @Override
        public String[] getParam(String name) {
            return null;
        }

        @Override
        public HttpRequest setParam(String name, String... params) {
            return this;
        }

        @Override
        public HttpRequest setParamMap(Map<String, String[]> params) {
            return this;
        }

        @Override
        public Map<String, String> getHeaderMap() {
            return Collections.emptyMap();
        }

        @Override
        public String getHeader(String headerName) {
            return null;
        }

        @Override
        public HttpCookie getCookie() {
            return null;
        }
    }
}
