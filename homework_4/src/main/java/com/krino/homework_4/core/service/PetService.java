package com.krino.homework_4.core.service;

import com.krino.homework_4.core.model.enums.Pet;
import org.springframework.http.ResponseEntity;

public interface PetService {
    public ResponseEntity<?> updatePetFull(Pet pet);
    public ResponseEntity<?> createPet(Pet pet);
    public ResponseEntity<?> getPet(int petId);
    public ResponseEntity<?> updatePetFull(int petId, String newName, String newStatus);
    public ResponseEntity<?> deletePet(int petId, String api);
}
