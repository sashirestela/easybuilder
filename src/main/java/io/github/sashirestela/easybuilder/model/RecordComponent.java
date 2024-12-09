package io.github.sashirestela.easybuilder.model;

import io.github.sashirestela.easybuilder.support.Common;

public class RecordComponent {

    private String name;
    private String type;

    public RecordComponent(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getNamePascalCase() {
        return Common.toPascalCase(name);
    }

    public String toString() {
        return "[name:" + this.name + ", type:" + this.type + "]";
    }

}
