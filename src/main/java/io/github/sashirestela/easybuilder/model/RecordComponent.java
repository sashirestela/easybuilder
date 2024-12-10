package io.github.sashirestela.easybuilder.model;

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

}
