package io.github.sashirestela.easybuilder.example;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordBuilderTest {

    @Test
    void testFullRecord() {
        Address expectedAddress = new Address("street", "city", "zipCode");
        Address actualAddress = AddressBuilder.builder().street("street").city("city").zipCode("zipCode").build();
        assertEquals(expectedAddress, actualAddress);
    }

    @Test
    void testPartialRecord() {
        Address expectedAddress = new Address("street", null, "zipCode");
        Address actualAddress = AddressBuilder.builder().street("street").zipCode("zipCode").build();
        assertEquals(expectedAddress, actualAddress);
    }

    @Test
    void testComposedRecord() {
        Person expectedPerson = new Person("name", 0, new Address("street", null, "zipCode"), null);
        Person actualPerson = PersonBuilder.builder()
                .name("name")
                .address(AddressBuilder.builder().street("street").zipCode("zipCode").build())
                .build();
        assertEquals(expectedPerson, actualPerson);
    }

    @Test
    void testBuildRecordFromAnotherOne() {
        Person expectedPerson = new Person("name", 18, null, "otherEmail");
        Person somePerson = PersonBuilder.builder().name("name").age(18).email("email").build();
        Person actualPerson = PersonBuilder.builder(somePerson).email("otherEmail").build();
        assertEquals(expectedPerson, actualPerson);
    }

    @Test
    void testNestedRecords() {
        List<Project.Task> tasks = List.of(
                TaskBuilder.builder().id(101).name("First Task").startDate(new Date()).build(),
                TaskBuilder.builder().id(101).name("Second Task").startDate(new Date()).build());
        Project project = ProjectBuilder.builder().id(201).name("Main Project").ownerName("Max").tasks(tasks).build();
        System.out.println(project);
    }

}
