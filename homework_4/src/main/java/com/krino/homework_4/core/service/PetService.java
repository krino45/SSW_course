package com.krino.homework_4.core.service;

import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.exception.UnauthorizedException;

public interface PetService {
    Pet updatePetFull(Pet pet);
    Pet createPet(Pet pet);
    Pet getPet(int petId);
    boolean updatePet(int petId, String newName, String newStatus);
    boolean deletePet(int petId, String api) throws UnauthorizedException;
}
