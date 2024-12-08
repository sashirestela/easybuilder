package io.github.sashirestela.easybuilder;

import io.github.sashirestela.easybuilder.annotation.Builder;

@Builder
public record Person(
        String name,
        int age,
        Address address,
        String email) {
}
