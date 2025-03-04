package com.krino.homework_4.api.controller;

import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.exception.UnauthorizedException;
import com.krino.homework_4.core.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pet")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PutMapping
    ResponseEntity<?> updatePetFull(@Valid @RequestBody Pet pet) {
        if (pet.getId() == null) {
            return ResponseEntity.badRequest().body("Invalid ID supplied");
        }
        Pet newPet = petService.updatePetFull(pet);
        if (newPet == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found");
        return ResponseEntity.ok(newPet);
    }
    @PostMapping
    ResponseEntity<?> createPet(@Valid @RequestBody Pet pet, BindingResult result) {
        if (result.hasErrors())
        {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        return ResponseEntity.ok(petService.createPet(pet));
    }
    @GetMapping("/{petId}")
    ResponseEntity<?> getPet(@PathVariable Integer petId) {
        if (petId == null) {
            return ResponseEntity.badRequest().body("Invalid ID supplied");
        }
        Pet pet = petService.getPet(petId);
        if (pet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found");
        }
        return ResponseEntity.ok().body(pet);
    }
    @PostMapping("/{petId}")
    ResponseEntity<?> updatePet(@PathVariable int petId,
                   @RequestParam(required = false) String newName,
                   @RequestParam(required = false) String newStatus) {
        if (!petService.updatePet(petId, newName, newStatus))
            return ResponseEntity.badRequest().body("Invalid input");
        return ResponseEntity.ok("Success");
    }
    @DeleteMapping("/{petId}")
    ResponseEntity<?> deletePet(@PathVariable int petId, @RequestHeader("Authorization") String api) throws UnauthorizedException {
        if (!petService.deletePet(petId, api))
            return ResponseEntity.badRequest().body("Invalid pet value");
        return ResponseEntity.ok("Success");
    }
}

