package io.github.sashirestela.easybuilder;

@Builder
public record Address(
        String street,
        String city,
        String zipCode) {
}
