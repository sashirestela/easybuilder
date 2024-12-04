package io.github.sashirestela.easybuilder;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.github.sashirestela.easybuilder.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
@SuppressWarnings("unused")
public class BuilderProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Builder.class.getCanonicalName());
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Initializing @Builder annotations...");
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing: " + annotations.toString());
        for (Element element : roundEnv.getElementsAnnotatedWith(Builder.class)) {
            if (element.getKind() != ElementKind.RECORD) {
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR,
                                "@Builder can only be applied to records");
                continue;
            }

            try {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating builder for: " + element);
                generateBuilder((TypeElement) element);
            } catch (Exception e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error in builder: " + e.getMessage());
            }
        }
        return true;
    }

    private void generateBuilder(TypeElement recordElement) throws Exception {
        String packageName = processingEnv.getElementUtils().getPackageOf(recordElement).getQualifiedName().toString();
        String recordName = recordElement.getSimpleName().toString();
        String builderName = recordName + "Builder";

        // Generate fields and methods for the builder
        String fields = recordElement.getRecordComponents()
                .stream()
                .map(component -> "private " + component.asType() + " " + component.getSimpleName() + ";")
                .collect(Collectors.joining("\n"));

        String ofMethod = "public static " + builderName + " of(" + recordName + " other) {\n" +
                "    " + builderName + " one = new " + builderName + "();\n" +
                recordElement.getRecordComponents()
                        .stream()
                        .map(component -> "    one." + component.getSimpleName() + " = other."
                                + component.getSimpleName() + "();")
                        .collect(Collectors.joining("\n"))
                + "\n" +
                "    return one;\n" +
                "}";

        String methods = recordElement.getRecordComponents()
                .stream()
                .map(component -> "public " + builderName + " " + component.getSimpleName() + "(" + component.asType()
                        + " " + component.getSimpleName() + ") {\n"
                        + "    this." + component.getSimpleName() + " = " + component.getSimpleName() + ";\n"
                        + "    return this;\n"
                        + "}")
                .collect(Collectors.joining("\n\n"));

        String buildMethod = "public " + recordName + " build() {\n" +
                "    return new " + recordName + "(" +
                recordElement.getRecordComponents()
                        .stream()
                        .map(component -> component.getSimpleName().toString())
                        .collect(Collectors.joining(", "))
                +
                ");\n}";

        String classContent = "package " + packageName + ";\n\n" +
                "public class " + builderName + " {\n" +
                fields + "\n\n" +
                ofMethod + "\n\n" +
                methods + "\n\n" +
                buildMethod + "\n\n" +
                "}";

        // Write the file
        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + builderName);
        try (Writer writer = file.openWriter()) {
            writer.write(classContent);
        }
    }

}
