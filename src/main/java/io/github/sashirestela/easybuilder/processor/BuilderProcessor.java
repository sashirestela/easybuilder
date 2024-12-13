package io.github.sashirestela.easybuilder.processor;

import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import io.github.sashirestela.easybuilder.annotation.Builder;
import io.github.sashirestela.easybuilder.model.RecordComponent;
import io.github.sashirestela.easybuilder.support.Configurator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({ "io.github.sashirestela.easybuilder.annotation.Builder" })
public class BuilderProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        messager.printMessage(Kind.NOTE, "Started Processor " + BuilderProcessor.class.getSimpleName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!annotations.isEmpty()) {
            messager.printMessage(Kind.NOTE, "Processing Annotations " + annotations.toString());
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(Builder.class)) {
            if (element.getKind() != ElementKind.RECORD) {
                messager.printMessage(Kind.WARNING, "The annotated element isn't a Record.", element);
                continue;
            } else if (element.getModifiers().stream().anyMatch(modifier -> modifier == Modifier.PRIVATE)) {
                messager.printMessage(Kind.WARNING, "The annotated Record is private.", element);
                continue;
            } else if (((TypeElement) element).getRecordComponents().isEmpty()) {
                messager.printMessage(Kind.WARNING, "The annotated Record hasn't components.", element);
                continue;
            }
            try {
                messager.printMessage(Kind.NOTE, "Generating from Class " + element);
                generateSourceFile((TypeElement) element);
            } catch (Exception e) {
                messager.printMessage(Kind.ERROR, "Error with " + element + ": " + e.getMessage());
            }
        }
        return true;
    }

    private void generateSourceFile(TypeElement recordElement) throws Exception {
        String packageName = getPackageName(recordElement);
        String recordName = getRecordName(recordElement);
        String builderName = getBuilderName(recordElement);
        List<RecordComponent> recordComponents = getRecordComponents(recordElement);

        Map<String, Object> context = new HashMap<>();
        context.put("packageName", packageName);
        context.put("recordName", recordName);
        context.put("builderName", builderName);
        context.put("recordComponents", recordComponents);

        TemplateOutput templateOutput = new StringOutput();
        Configurator.one().getTemplateEngine().render("record_builder.jte", context, templateOutput);

        JavaFileObject javaFile = filer.createSourceFile(packageName.isEmpty()
                ? builderName
                : packageName + "." + builderName);
        try (Writer writer = javaFile.openWriter()) {
            writer.write(templateOutput.toString());
        }
    }

    private String getPackageName(TypeElement recordElement) {
        return elementUtils.getPackageOf(recordElement).getQualifiedName().toString();
    }

    private String getRecordName(TypeElement recordElement) {
        String recordName = "";
        Element element = recordElement;
        while (!(element instanceof PackageElement)) {
            recordName = ((TypeElement) element).getSimpleName().toString() +
                    (recordName.isEmpty() ? "" : ".") + recordName;
            element = element.getEnclosingElement();
        }
        return recordName;
    }

    private String getBuilderName(TypeElement recordElement) {
        return recordElement.getSimpleName().toString() + "Builder";
    }

    private List<RecordComponent> getRecordComponents(TypeElement recordElement) {
        return recordElement.getRecordComponents()
                .stream()
                .map(rc -> new RecordComponent(
                        rc.getSimpleName().toString(),
                        rc.asType().toString()))
                .collect(Collectors.toList());
    }

}
