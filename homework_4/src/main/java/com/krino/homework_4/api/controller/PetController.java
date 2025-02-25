package com.krino.homework_4.api.controller;

import com.krino.homework_4.core.model.enums.Pet;
import com.krino.homework_4.core.service.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pet")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PutMapping
    ResponseEntity<?> updatePetFull(@RequestBody Pet pet) {
        return petService.updatePetFull(pet);
    }
    @PostMapping
    ResponseEntity<?> createPet(@RequestBody Pet pet) {
        return petService.createPet(pet);
    }
    @GetMapping("/{petId}")
    ResponseEntity<?> getPet(@PathVariable int petId) {
        return petService.getPet(petId);
    }
    @PostMapping("/{petId}")
    ResponseEntity<?> updatePetFull(@PathVariable int petId,
                   @RequestParam(required = false) String newName,
                   @RequestParam(required = false) String newStatus) {
        return petService.updatePetFull(petId, newName, newStatus);
    }
    @PostMapping("/{petId}")
    ResponseEntity<?> deletePet(@PathVariable int petId, @RequestHeader("Authorization") String api) {
        return petService.deletePet(petId, api);
    }
}

