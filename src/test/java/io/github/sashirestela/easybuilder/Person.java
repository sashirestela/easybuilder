package io.github.sashirestela.easybuilder;

@Builder
public record Person(
        String name,
        int age,
        Address address,
        String email) {
}
