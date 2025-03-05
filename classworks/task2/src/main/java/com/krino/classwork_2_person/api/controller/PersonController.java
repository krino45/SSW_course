package com.krino.classwork_2_person.api.controller;

import com.krino.classwork_2_person.core.model.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RequestMapping("/persons")
@Controller
public class PersonController {
    ArrayList<Person> persons = new ArrayList<>();


    @GetMapping("/{id}")
    ResponseEntity<Person> GetPerson(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(persons.get(id));
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    ResponseEntity<?> PostPerson(@RequestBody Person person) {
        if (person.getAge() == null || person.getName() == null)
            return ResponseEntity.badRequest().body("Age or name is null");
        person.setId(persons.size());
        person.setAge(person.getAge() + 4);
        persons.add(person);
        return ResponseEntity.ok(person);
    }

}
