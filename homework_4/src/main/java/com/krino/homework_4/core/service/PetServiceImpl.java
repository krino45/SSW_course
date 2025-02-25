package com.krino.homework_4.core.service;

import com.krino.homework_4.core.model.enums.Pet;
import com.krino.homework_4.core.model.enums.Status;
import com.krino.homework_4.core.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PetServiceImpl implements PetService {
    private PetRepository petRepository;

    @Autowired
    public PetServiceImpl(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public ResponseEntity<?> updatePetFull(Pet pet) {

        Optional<Pet> existingPet = petRepository.findById(pet.getId());
        if (existingPet.isPresent()) {
            petRepository.save(pet);
            return ResponseEntity.ok(petRepository.findById(pet.getId()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found.");
    }
    public ResponseEntity<?> createPet(Pet pet) {
        petRepository.save(pet);
        return ResponseEntity.status(HttpStatus.CREATED).body(petRepository.findById(pet.getId()));
    }
    public ResponseEntity<?> getPet(int petId) {
        Optional<Pet> pet = petRepository.findById(petId);
        if(pet.isPresent()) {
            return ResponseEntity.ok().body(pet);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found.");
    }
    public ResponseEntity<?> updatePetFull(int petId, String newName, String newStatus) {
        Optional<Pet> petOptional = petRepository.findById(petId);
        if (petOptional.isPresent()) {
            Pet pet = petOptional.get();
            pet.setName(newName);
            pet.setStatus(Status.valueOf(newStatus));
            petRepository.save(pet);
            return ResponseEntity.ok("Pet updated successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found.");
    }
    public ResponseEntity<?> deletePet(int petId, String api) {
        if (!api.contentEquals("api_key"))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("API is wrong");
        boolean removed = petRepository.delete(petId);
        if (removed) {
            return ResponseEntity.ok("Pet deleted successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Pet not found.");
    }
}