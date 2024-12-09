package io.github.sashirestela.easybuilder.processor;

import com.google.auto.service.AutoService;

import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import io.github.sashirestela.easybuilder.annotation.Builder;
import io.github.sashirestela.easybuilder.model.RecordComponent;
import io.github.sashirestela.easybuilder.support.Configurator;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<RecordComponent> recordComponents = recordElement.getRecordComponents().stream()
                .map(rc -> new RecordComponent(rc.getSimpleName().toString(), rc.asType().toString()))
                .collect(Collectors.toList());
        Map<String, Object> context = new HashMap<>();
        context.put("packageName", packageName);
        context.put("recordName", recordName);
        context.put("builderName", builderName);
        context.put("recordComponents", recordComponents);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Context: " + context.toString());

        TemplateOutput output = new StringOutput();
        Configurator.one().getTemplateEngine().render("record_builder.jte", context, output);
        // Write the file
        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + builderName);
        try (Writer writer = file.openWriter()) {
            writer.write(output.toString());
        }
    }

}
