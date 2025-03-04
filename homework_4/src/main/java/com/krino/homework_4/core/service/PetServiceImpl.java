package com.krino.homework_4.core.service;

import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.enums.Status;
import com.krino.homework_4.core.model.exception.UnauthorizedException;
import com.krino.homework_4.core.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PetServiceImpl implements PetService {
    private PetRepository petRepository;

    @Autowired
    public PetServiceImpl(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public Pet updatePetFull(Pet pet) {
        Optional<Pet> existingPet = petRepository.findById(pet.getId());
        if (existingPet.isPresent()) {
            petRepository.save(pet);
            return pet;
        }
        return null;
    }
    public Pet createPet(Pet pet) {
        petRepository.save(pet);
        return pet;
    }
    public Pet getPet(int petId) {
        Optional<Pet> pet = petRepository.findById(petId);
        return pet.orElse(null);
    }
    public boolean updatePet(int petId, String newName, String newStatus) {
        Optional<Pet> petOptional = petRepository.findById(petId);
        if (petOptional.isPresent()) {
            Pet pet = petOptional.get();
            pet.setName(newName);
            pet.setStatus(Status.fromString(newStatus));
            petRepository.save(pet);
            return true;
        }
        return false;
    }
    public boolean deletePet(int petId, String api) throws UnauthorizedException {
        if (!api.contentEquals("api_key"))
            throw new UnauthorizedException("Wrong API key");
        return petRepository.delete(petId);
    }
}