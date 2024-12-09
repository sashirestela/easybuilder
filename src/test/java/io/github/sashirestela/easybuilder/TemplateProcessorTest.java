package io.github.sashirestela.easybuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.sashirestela.easybuilder.model.RecordComponent;
import io.github.sashirestela.easybuilder.support.TemplateProcessor;
import io.github.sashirestela.easybuilder.support.TemplateProcessor1;

public class TemplateProcessorTest {

    public static void main(String[] args) throws IOException {
        Map<String, Object> context = new HashMap<>();
        context.put("packageName", "io.github.sashirestela.easybuilder");
        context.put("recordName", "Address");
        context.put("builderName", "AddressBuilder");
        context.put("recordComponents", List.of(
            new RecordComponent("street", "String"),
            new RecordComponent("city", "String"),
            new RecordComponent("zipCode", "String")));
        String content = TemplateProcessor.process(Paths.get("src/main/resources/record_builder.template"), context);
        System.out.println(content);
    }
}
