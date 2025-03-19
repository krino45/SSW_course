package com.krino.homework_4.core.repository;

import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
}
