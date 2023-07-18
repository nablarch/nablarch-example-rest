package com.nablarch.example.code;

import nablarch.core.validation.ee.EnumElement.WithValue;
/**
 * 本Exampleで使用するプロジェクト種類を定義したEnum。
 *
 * @author Nabu Rakutaro
 */
public enum ProjectType implements WithValue<String> {
    /** 新規開発PJ */
    DEVELOPMENT("development", "新規開発PJ"),
    /** 保守PJ */
    MAINTENANCE("maintenance", "保守PJ");

    /** プロジェクト種類のラベル */
    private final String label;
    /** プロジェクト種類のコード */
    private final String code;

    /**
     * コンストラクタ。
     * @param code コード値
     * @param label ラベル
     */
    ProjectType(String code, String label) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return code;
    }
}
