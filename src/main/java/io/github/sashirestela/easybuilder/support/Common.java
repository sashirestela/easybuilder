package io.github.sashirestela.easybuilder.support;

public class Common {

    private Common() {
    }

    public static String toPascalCase(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1, text.length());
    }

}
