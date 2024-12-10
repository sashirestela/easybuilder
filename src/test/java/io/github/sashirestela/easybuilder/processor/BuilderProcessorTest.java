package io.github.sashirestela.easybuilder.processor;

import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi;
import io.toolisticon.cute.JavaFileObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class BuilderProcessorTest {

    private CuteApi.BlackBoxTestSourceFilesInterface compileTestBuilder;

    @BeforeEach
    void init() {
        compileTestBuilder = Cute
                .blackBoxTest()
                .given()
                .processors(List.of(BuilderProcessor.class));
    }

    @Test
    void test_valid_usage() {
        compileTestBuilder
                .andSourceFiles("testcases/Item.java.ct")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .compilerMessage()
                .ofKindNote()
                .contains("Generating from Class io.github.sashirestela.easybuilder.Item")
                .andThat()
                .generatedSourceFile("io.github.sashirestela.easybuilder.ItemBuilder")
                .matches(
                        CuteApi.ExpectedFileObjectMatcherKind.BINARY,
                        JavaFileObjectUtils.readFromResource("../../../../../testcases/ItemBuilder.java.ct",
                                BuilderProcessorTest.class))
                .executeTest();
    }

}
