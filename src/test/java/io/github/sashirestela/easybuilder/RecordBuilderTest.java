package io.github.sashirestela.easybuilder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordBuilderTest {

    @Test
    void testFullRecordComponents() {
        Address expectedAddress = new Address("street", "city", "zipCode");
        Address actualAddress = AddressBuilder.builder().street("street").city("city").zipCode("zipCode").build();
        assertEquals(expectedAddress, actualAddress);
    }

    @Test
    void testPartialRecordComponents() {
        Address expectedAddress = new Address("street", null, "zipCode");
        Address actualAddress = AddressBuilder.builder().street("street").zipCode("zipCode").build();
        assertEquals(expectedAddress, actualAddress);
    }

    @Test
    void testNestedRecordComponents() {
        Person expectedPerson = new Person("name", 0, new Address("street", null, "zipCode"), null);
        Person actualPerson = PersonBuilder.builder().name("name")
                .address(AddressBuilder.builder().street("street").zipCode("zipCode").build())
                .build();
        assertEquals(expectedPerson, actualPerson);
    }

    @Test
    void testWithRecordComponents() {
        Person expectedPerson = new Person("name", 18, null, "otherEmail");
        Person somePerson = PersonBuilder.builder().name("name").age(18).email("email").build();
        Person actualPerson = PersonBuilder.builder(somePerson).email("otherEmail").build();
        assertEquals(expectedPerson, actualPerson);
    }

}
