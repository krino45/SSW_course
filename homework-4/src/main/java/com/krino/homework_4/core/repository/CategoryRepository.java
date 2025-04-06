package com.krino.homework_4.core.repository;

import com.krino.homework_4.core.model.Category;
import com.krino.homework_4.core.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
