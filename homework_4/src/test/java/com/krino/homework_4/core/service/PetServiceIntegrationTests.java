package com.krino.homework_4.core.service;

import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.enums.Status;
import com.krino.homework_4.core.model.exception.UnauthorizedException;
import com.krino.homework_4.core.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class PetServiceIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.2")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PetService petService;

    @Autowired
    private PetRepository petRepository;

    private Pet testPet;

    @BeforeEach
    void setUp() {
        petRepository.deleteAll();

        testPet = new Pet();
        testPet.setName("TestDog");
        testPet.setStatus(Status.AVAILABLE);
        testPet = petService.createPet(testPet);
    }

    @Test
    void testCreatePet() {
        Pet newPet = new Pet();
        newPet.setName("TestCat");
        newPet.setStatus(Status.PENDING);

        Pet createdPet = petService.createPet(newPet);

        assertNotNull(createdPet.getId());
        assertEquals("TestCat", createdPet.getName());
        assertEquals(Status.PENDING, createdPet.getStatus());
    }

    @Test
    void testGetPet() {
        Pet retrievedPet = petService.getPet(testPet.getId());

        assertNotNull(retrievedPet);
        assertEquals(testPet.getId(), retrievedPet.getId());
        assertEquals("TestDog", retrievedPet.getName());
    }

    @Test
    void testUpdatePet() {
        boolean updated = petService.updatePet(testPet.getId(), "UpdatedDog", "SOLD");

        assertTrue(updated);

        Pet updatedPet = petService.getPet(testPet.getId());
        assertEquals("UpdatedDog", updatedPet.getName());
        assertEquals(Status.SOLD, updatedPet.getStatus());
    }

    @Test
    void testUpdatePetNonExistent() {
        boolean updated = petService.updatePet(999, "NonExistent", "AVAILABLE");

        assertFalse(updated);
    }

    @Test
    void testDeletePet() throws UnauthorizedException {
        boolean deleted = petService.deletePet(testPet.getId(), "api_key");

        assertTrue(deleted);
        assertNull(petService.getPet(testPet.getId()));
    }

    @Test
    void testDeletePetUnauthorized() {
        assertThrows(UnauthorizedException.class, () ->
                petService.deletePet(testPet.getId(), "wrong_key"));
    }
}