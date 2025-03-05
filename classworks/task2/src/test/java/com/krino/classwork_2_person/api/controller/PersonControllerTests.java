package com.krino.classwork_2_person.api.controller;


import com.krino.classwork_2_person.core.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;


public class PersonControllerTests {
    PersonController personController;

    Person guy;

    @BeforeEach
    public void setUp() {
        personController = new PersonController();
        guy = new Person(0, "Jordan", 24);
    }

    @Test
    public void testGetPerson_Success() {
        personController.PostPerson(guy);

        ResponseEntity<Person> response = personController.GetPerson(0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(guy, response.getBody());
    }

    @Test
    public void testGetAfterTwoPosts() {
        personController.PostPerson(guy);
        personController.PostPerson(guy);
        ResponseEntity<Person> response = personController.GetPerson(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(guy, response.getBody());
    }

    @Test
    public void testGetPerson_Fail() {
        ResponseEntity<Person> response = personController.GetPerson(guy.getId() + 1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetPerson_null() {
        ResponseEntity<Person> response = personController.GetPerson(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testPostPerson_Success() {
        int age = guy.getAge() + 4;
        ResponseEntity<?> response = personController.PostPerson(guy);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Person person = (Person) response.getBody();
        assertNotNull(person);
        assertNotNull(person.getId());
        assertNotNull(person.getName());
        assertNotNull(person.getAge());
        assertEquals(age, person.getAge());
    }

    @Test
    public void testPostPerson_Fail_nullName() {
        guy.setName(null);
        ResponseEntity<?> response = personController.PostPerson(guy);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Age or name is null",response.getBody());
    }

    @Test
    public void testPostPerson_Fail_nullAge() {
        guy.setAge(null);
        ResponseEntity<?> response = personController.PostPerson(guy);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Age or name is null",response.getBody());
    }


}
