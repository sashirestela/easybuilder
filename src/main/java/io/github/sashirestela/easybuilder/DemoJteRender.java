package io.github.sashirestela.easybuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import io.github.sashirestela.easybuilder.model.RecordComponent;
import io.github.sashirestela.easybuilder.support.Configurator;

public class DemoJteRender {

    public static void main(String[] args) throws IOException {
        Map<String, Object> context = new HashMap<>();
        context.put("packageName", "io.github.sashirestela.easybuilder");
        context.put("recordName", "Address");
        context.put("builderName", "AddressBuilder");
        context.put("recordComponents", List.of(
                new RecordComponent("street", "String"),
                new RecordComponent("city", "String"),
                new RecordComponent("zipCode", "String")));
        TemplateOutput output = new StringOutput();
        Configurator.one().getTemplateEngine().render("record_builder.jte", context, output);
        System.out.println(output.toString());
    }
}
