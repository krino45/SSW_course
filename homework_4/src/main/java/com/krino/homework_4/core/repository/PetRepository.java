package com.krino.homework_4.core.repository;

import com.krino.homework_4.core.model.enums.Pet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PetRepository {
    private final List<Pet> pets = new ArrayList<>();

    public Optional<Pet> findById(int petId) {
        return pets.stream().filter(pet -> pet.getId() == petId).findFirst();
    }

    public void save(Pet pet) {
        pets.removeIf(existingPet -> Objects.equals(existingPet.getId(), pet.getId()));
        pets.add(pet);
    }

    public boolean delete(int petId) {
        return pets.removeIf(pet -> pet.getId() == petId);
    }
}
