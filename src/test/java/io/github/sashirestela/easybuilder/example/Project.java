package io.github.sashirestela.easybuilder.example;

import io.github.sashirestela.easybuilder.annotation.Builder;

import java.util.Date;
import java.util.List;

@Builder
public record Project(
        Integer id,
        String name,
        String ownerName,
        List<Task> tasks) {

    @Builder
    public static record Task(
            Integer id,
            String name,
            Date startDate) {
    }

}
