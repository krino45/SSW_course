package com.krino.homework_4.core.service;

import com.krino.homework_4.core.model.Category;
import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.Tag;
import com.krino.homework_4.core.model.enums.Status;
import com.krino.homework_4.core.model.exception.UnauthorizedException;
import com.krino.homework_4.core.repository.CategoryRepository;
import com.krino.homework_4.core.repository.PetRepository;
import com.krino.homework_4.core.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PetServiceImpl implements PetService {
    private final PetRepository petRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Transactional
    public Pet updatePetFull(Pet pet) {
        if (petRepository.existsById(pet.getId())) {
            if (pet.getCategory() != null) {
                Category category = pet.getCategory();
                if (category.getId() == null) {
                    category = categoryRepository.save(category);
                } else {
                    Category finalCategory = category;
                    category = categoryRepository.findById(category.getId())
                            .orElseGet(() -> categoryRepository.save(finalCategory));
                }
                pet.setCategory(category);
            }

            if (pet.getTags() != null && !pet.getTags().isEmpty()) {
                List<Tag> savedTags = new ArrayList<>();
                for (Tag tag : pet.getTags()) {
                    if (tag.getId() == null) {
                        tag = tagRepository.save(tag);
                    } else {
                        Tag finalTag = tag;
                        tag = tagRepository.findById(tag.getId())
                                .orElseGet(() -> tagRepository.save(finalTag));
                    }
                    savedTags.add(tag);
                }
                pet.setTags(savedTags);
            }

            return petRepository.save(pet);
        }
        return null;
    }
    @Transactional
    public Pet createPet(Pet pet) {
        if (pet.getCategory() != null) {
            Category category = pet.getCategory();
            if (category.getId() == null) {
                category = categoryRepository.save(category);
            } else {
                Category finalCategory = category;
                category = categoryRepository.findById(category.getId())
                        .orElseGet(() -> categoryRepository.save(finalCategory));
            }
            pet.setCategory(category);
        }

        if (pet.getTags() != null && !pet.getTags().isEmpty()) {
            List<Tag> savedTags = new ArrayList<>();
            for (Tag tag : pet.getTags()) {
                if (tag.getId() == null) {
                    tag = tagRepository.save(tag);
                } else {
                    Tag finalTag = tag;
                    tag = tagRepository.findById(tag.getId())
                            .orElseGet(() -> tagRepository.save(finalTag));
                }
                savedTags.add(tag);
            }
            pet.setTags(savedTags);
        }

        return petRepository.save(pet);
    }
    public Pet getPet(int petId) {
        Optional<Pet> pet = petRepository.findById(petId);
        return pet.orElse(null);
    }
    @Transactional
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
    @Transactional
    public boolean deletePet(int petId, String api) throws UnauthorizedException {
        if (!api.contentEquals("api_key"))
            throw new UnauthorizedException("Wrong API key");
        if (petRepository.existsById(petId)) {
            petRepository.deleteById(petId);
            return true;
        }
        return false;
    }
}