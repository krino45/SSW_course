package com.krino.homework_4.core.repository;

import com.krino.homework_4.core.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {
}
