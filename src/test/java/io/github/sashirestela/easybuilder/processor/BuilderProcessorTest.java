package io.github.sashirestela.easybuilder.processor;

import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi;
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
                .compilerMessage()
                .ofKindNote()
                .contains("Generating from Class io.github.sashirestela.easybuilder.Item.SubItem")
                .andThat()
                .generatedSourceFile("io.github.sashirestela.easybuilder.ItemBuilder")
                .exists()
                .andThat()
                .generatedSourceFile("io.github.sashirestela.easybuilder.SubItemBuilder")
                .exists()
                .executeTest();
    }

}
