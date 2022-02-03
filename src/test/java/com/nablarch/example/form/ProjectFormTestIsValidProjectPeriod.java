package com.nablarch.example.form;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link ProjectForm#isValidProjectPeriod()}のテスト。
 */
class ProjectFormTestIsValidProjectPeriod {

    static List<Fixture> プロジェクト期間の相関バリデーションのテスト() {
        return Arrays.asList(
            new Fixture("", "", true),
            new Fixture("", "20150101", true),
            new Fixture("20150101", "", true),
            new Fixture("20150101", "20150102", true),
            new Fixture("20150102", "20150101", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void プロジェクト期間の相関バリデーションのテスト(Fixture fixture) throws Exception {
        final ProjectForm form = new ProjectForm();
        form.setProjectStartDate(fixture.start);
        form.setProjectEndDate(fixture.end);

        assertThat(form.isValidProjectPeriod(), CoreMatchers.is(fixture.expected));
    }

    private static class Fixture {
        private final String start;
        private final String end;
        private final boolean expected;

        public Fixture(final String start, final String end, final boolean expected) {
            this.start = start;
            this.end = end;
            this.expected = expected;
        }

        @Override
        public String toString() {
            return "Fixture{" +
                    "start='" + start + '\'' +
                    ", end='" + end + '\'' +
                    '}';
        }
    }
}