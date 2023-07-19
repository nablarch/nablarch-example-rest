package com.nablarch.example.code;

import nablarch.core.validation.ee.EnumElement;

/**
 * コード値を定義したEnumが実装するインタフェース。
 *
 * @author Nabu Rakutaro
 */
public interface CodeEnum extends EnumElement.WithValue<String> {
    /**
     * ラベルを返却する。
     * @return ラベル
     */
    String getLabel();

    /**
     * コード値を返却する。
     * @return コード値
     */
    @Override
    String getValue();
}
