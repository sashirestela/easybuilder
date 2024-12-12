package io.github.sashirestela.easybuilder.processor;

import io.toolisticon.cute.Cute;
import io.toolisticon.cute.CuteApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void shouldCompileFromStandardRecordAndTestFreshRecordFromBuilder() {
        String originalFile = "com.mycompany.demo.model.StandardRecord";
        String generatedFile = "com.mycompany.demo.model.StandardRecordBuilder";
        compileTestBuilder
                .andSourceFiles("testcases/StandardRecord.java.ct")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedSourceFile(generatedFile)
                .exists()
                .andThat()
                .generatedClassesTestedSuccessfullyBy(cuteClassLoader -> {
                    String[] methods = { "id", "name", "amount" };
                    Class<?>[] classes = { Integer.class, String.class, Double.class };
                    Object[] values = { 101, "Sample", 17.65 };

                    Class<?> recordClass = cuteClassLoader.getClass(originalFile);
                    Object expectedRecord = newObjectFromClass(recordClass, classes, values);

                    Class<?> builderClass = cuteClassLoader.getClass(generatedFile);
                    Object actualRecord = newFreshObjectFromBuilder(builderClass, methods, classes, values);

                    assertEquals(expectedRecord, actualRecord);
                })
                .executeTest();
    }

    @Test
    void shouldCompileFromStandardRecordAndTestStaleRecordFromBuilder() {
        String originalFile = "com.mycompany.demo.model.StandardRecord";
        String generatedFile = "com.mycompany.demo.model.StandardRecordBuilder";
        compileTestBuilder
                .andSourceFiles("testcases/StandardRecord.java.ct")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedSourceFile(generatedFile)
                .exists()
                .andThat()
                .generatedClassesTestedSuccessfullyBy(cuteClassLoader -> {
                    String[] methods = { "id", "name", "amount" };
                    Class<?>[] classes = { Integer.class, String.class, Double.class };
                    Object[] values1 = { 101, "Expected", 17.65 };
                    Object[] values2 = { 101, "Temporal", 17.65 };

                    Class<?> recordClass = cuteClassLoader.getClass(originalFile);
                    Object expectedRecord = newObjectFromClass(recordClass, classes, values1);
                    Object temporalRecord = newObjectFromClass(recordClass, classes, values2);

                    Class<?> builderClass = cuteClassLoader.getClass(generatedFile);
                    Object actualRecord = newStaleObjectFromBuilder(builderClass, methods[1], classes[1], values1[1],
                            recordClass, temporalRecord);

                    assertEquals(expectedRecord, actualRecord);
                })
                .executeTest();
    }

    @Test
    void shouldCompileFromCompositeRecordAndTestNestedRecord() {
        String originalFile = "com.mycompany.demo.model.CompositeRecord$IntermediateRecord$NestedRecord";
        String generatedFile = "com.mycompany.demo.model.NestedRecordBuilder";
        compileTestBuilder
                .andSourceFiles("testcases/CompositeRecord.java.ct")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedSourceFile(generatedFile)
                .exists()
                .andThat()
                .generatedClassesTestedSuccessfullyBy(cuteClassLoader -> {
                    String[] methods = { "id", "detail" };
                    Class<?>[] classes = { Integer.class, String.class };
                    Object[] values = { 101, "Sample" };

                    Class<?> recordClass = cuteClassLoader.getClass(originalFile);
                    Object expectedRecord = newObjectFromClass(recordClass, classes, values);

                    Class<?> builderClass = cuteClassLoader.getClass(generatedFile);
                    Object actualRecord = newFreshObjectFromBuilder(builderClass, methods, classes, values);

                    assertEquals(expectedRecord, actualRecord);
                })
                .executeTest();
    }

    @Test
    void shouldCompileFromNoPackageRecord() {
        String generatedFile = "NoPackageRecordBuilder";
        compileTestBuilder
                .andSourceFiles("testcases/NoPackageRecord.java.ct")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedSourceFile(generatedFile)
                .exists()
                .executeTest();
    }

    @Test
    void shouldCompileFromEmptyRecord() {
        String generatedFile = "com.mycompany.demo.model.EmptyRecordBuilder";
        compileTestBuilder
                .andSourceFiles("testcases/EmptyRecord.java.ct")
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .generatedSourceFile(generatedFile)
                .exists()
                .executeTest();
    }

    private Object newObjectFromClass(Class<?> clazz, Class<?>[] classes, Object[] values)
            throws Exception {
        return Util.instance(Util.constructor(clazz, classes), values);
    }

    private Object newFreshObjectFromBuilder(Class<?> builderClass, String[] methods, Class<?>[] classes,
            Object[] values) throws Exception {
        return newObjectFromBuilder(builderClass, methods, classes, values, null, null);
    }

    private Object newStaleObjectFromBuilder(Class<?> builderClass, String method, Class<?> clazz, Object value,
            Class<?> baseClass, Object baseObject) throws Exception {
        return newObjectFromBuilder(builderClass, new String[] { method }, new Class[] { clazz },
                new Object[] { value }, baseClass, baseObject);
    }

    private Object newObjectFromBuilder(Class<?> builderClass, String[] methods, Class<?>[] classes, Object[] values,
            Class<?> baseClass, Object baseObject) throws Exception {
        Object[] EMPTY = new Object[0];
        Object builderObject = null;
        if (baseClass == null) {
            builderObject = Util.invoke(Util.method(builderClass, "builder"), null, EMPTY);
        } else {
            builderObject = Util.invoke(Util.method(builderClass, "builder", baseClass), null, baseObject);
        }
        for (int i = 0; i < methods.length; i++) {
            builderObject = Util.invoke(Util.method(builderClass, methods[i], classes[i]), builderObject, values[i]);
        }
        return Util.invoke(Util.method(builderClass, "build"), builderObject, EMPTY);
    }

}
