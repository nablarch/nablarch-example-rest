package com.nablarch.example.validator;

import nablarch.core.util.DateUtil;
import nablarch.core.util.StringUtil;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * YYYYMMDDの検証を行う実装クラス。
 *
 * @author Nabu Rakutaro
 */
public class YYYYMMDDValidator implements ConstraintValidator<YYYYMMDD, String> {

    /**
     * 許容する年月日のフォーマット
     */
    private String allowFormat;

    @Override
    public void initialize(YYYYMMDD constraintAnnotation) {
        allowFormat = constraintAnnotation.allowFormat();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtil.isNullOrEmpty(value)) {
            return true;
        }
        try {
            return DateUtil.getParsedDate(value, allowFormat) != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
