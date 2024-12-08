package io.github.sashirestela.easybuilder;

import io.github.sashirestela.easybuilder.annotation.Builder;

@Builder
public record Address(
        String street,
        String city,
        String zipCode) {
}
