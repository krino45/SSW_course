package com.krino.homework_4.core.service;

import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.enums.Status;
import com.krino.homework_4.core.model.exception.UnauthorizedException;
import com.krino.homework_4.core.repository.CategoryRepository;
import com.krino.homework_4.core.repository.PetRepository;
import com.krino.homework_4.core.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PetServiceTests {

    @Mock
    private PetRepository petRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PetServiceImpl petServiceImpl;

    private PetService petService;


    private Pet testPet;

    @BeforeEach
    public void setUp() {
        petService = petServiceImpl;
        testPet = new Pet();
        testPet.setId(1);
        testPet.setName("TestPet");
        testPet.setStatus(Status.AVAILABLE);
    }

    @Test
    public void testUpdatePetFull_Success() {
        when(petRepository.existsById(testPet.getId())).thenReturn(true);
        when(petRepository.save(testPet)).thenReturn(testPet);

        Pet updatedPet = petService.updatePetFull(testPet);

        assertNotNull(updatedPet);
        assertEquals(testPet, updatedPet);
        verify(petRepository).save(testPet);
    }

    @Test
    public void testUpdatePet_LowerCaseStatus() {
        when(petRepository.findById(1)).thenReturn(Optional.of(testPet));
        when(petRepository.save(testPet)).thenReturn(testPet);

        boolean updated = petService.updatePet(1, "NewName", "pending");

        assertTrue(updated);
        assertEquals("NewName", testPet.getName());
        assertEquals(Status.PENDING, testPet.getStatus());
        verify(petRepository).save(testPet);
    }

    @Test
    public void testUpdatePet_MixedCaseStatus() {
        when(petRepository.findById(1)).thenReturn(Optional.of(testPet));
        when(petRepository.save(testPet)).thenReturn(testPet);

        boolean updated = petService.updatePet(1, "NewName", "SoLd");

        assertTrue(updated);
        assertEquals("NewName", testPet.getName());
        assertEquals(Status.SOLD, testPet.getStatus());
        verify(petRepository).save(testPet);
    }

    @Test
    public void testUpdatePet_InvalidStatus() {
        when(petRepository.findById(1)).thenReturn(Optional.of(testPet));

        assertThrows(IllegalArgumentException.class, () -> {
            petService.updatePet(1, "NewName", "invalid_status");
        });

        verify(petRepository, never()).save(any());
    }

    @Test
    public void testUpdatePetFull_PetNotFound() {
        when(petRepository.existsById(testPet.getId())).thenReturn(false);

        Pet updatedPet = petService.updatePetFull(testPet);

        assertNull(updatedPet);
        verify(petRepository, never()).save(any());
    }

    @Test
    public void testCreatePet() {
        when(petRepository.save(testPet)).thenReturn(testPet);

        Pet createdPet = petService.createPet(testPet);

        assertNotNull(createdPet);
        assertEquals(testPet, createdPet);
        verify(petRepository).save(testPet);
    }

    @Test
    public void testGetPet_Found() {
        when(petRepository.findById(1)).thenReturn(Optional.of(testPet));

        Pet retrievedPet = petService.getPet(1);

        assertNotNull(retrievedPet);
        assertEquals(testPet, retrievedPet);
    }

    @Test
    public void testGetPet_NotFound() {
        when(petRepository.findById(1)).thenReturn(Optional.empty());

        Pet retrievedPet = petService.getPet(1);

        assertNull(retrievedPet);
    }

    @Test
    public void testUpdatePet_Success() {
        when(petRepository.findById(1)).thenReturn(Optional.of(testPet));
        when(petRepository.save(testPet)).thenReturn(testPet);

        boolean updated = petService.updatePet(1, "NewName", "PENDING");

        assertTrue(updated);
        assertEquals("NewName", testPet.getName());
        assertEquals(Status.PENDING, testPet.getStatus());
        verify(petRepository).save(testPet);
    }

    @Test
    public void testUpdatePet_PetNotFound() {
        when(petRepository.findById(1)).thenReturn(Optional.empty());

        boolean updated = petService.updatePet(1, "NewName", "PENDING");

        assertFalse(updated);
        verify(petRepository, never()).save(any());
    }

    @Test
    public void testDeletePet_Authorized() throws UnauthorizedException {
        doNothing().when(petRepository).deleteById(1);
        when(petRepository.existsById(1)).thenReturn(true);

        boolean deleted = petService.deletePet(1, "api_key");

        assertTrue(deleted);
        verify(petRepository).deleteById(1);
    }

    @Test
    public void testDeletePet_Unauthorized() {
        assertThrows(UnauthorizedException.class, () -> {
            petService.deletePet(1, "wrong_key");
        });
    }
}