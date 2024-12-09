package io.github.sashirestela.easybuilder.support;

import java.nio.file.Path;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;

public class Configurator {

    private static Configurator configurator = null;

    private TemplateEngine templateEngine = null;

    private Configurator() {
    }

    public static Configurator one() {
        if (configurator == null) {
            configurator = new Configurator();
        }
        return configurator;
    }

    public TemplateEngine getTemplateEngine() {
        if (templateEngine == null) {
            CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("src/main/jte"));
            templateEngine = TemplateEngine.create(
                codeResolver,
                Path.of("jte-classes"),
                ContentType.Plain,
                this.getClass().getClassLoader());
        }
        return templateEngine;
    }
}
