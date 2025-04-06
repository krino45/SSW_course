package com.krino.homework_4.api.controller;

import com.krino.homework_4.core.model.Pet;
import com.krino.homework_4.core.model.enums.Status;
import com.krino.homework_4.core.model.exception.UnauthorizedException;
import com.krino.homework_4.core.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PetControllerTests {

    @Mock
    private PetService petService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private PetController petController;

    private Pet testPet;

    @BeforeEach
    public void setUp() {
        testPet = new Pet();
        testPet.setId(1);
        testPet.setName("TestPet");
        testPet.setStatus(Status.AVAILABLE);
    }

    @Test
    public void testUpdatePetFull_Success() {
        when(petService.updatePetFull(testPet)).thenReturn(testPet);

        ResponseEntity<?> response = petController.updatePetFull(testPet);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPet, response.getBody());
        verify(petService).updatePetFull(testPet);
    }

    @Test
    public void testUpdatePetFull_NullId() {
        Pet petWithNullId = new Pet();

        ResponseEntity<?> response = petController.updatePetFull(petWithNullId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid ID supplied", response.getBody());
    }

    @Test
    public void testUpdatePetFull_NotFound() {
        when(petService.updatePetFull(testPet)).thenReturn(null);

        ResponseEntity<?> response = petController.updatePetFull(testPet);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Pet not found", response.getBody());
    }

    @Test
    public void testCreatePet_Success() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(petService.createPet(testPet)).thenReturn(testPet);

        ResponseEntity<?> response = petController.createPet(testPet, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPet, response.getBody());
    }

    @Test
    public void testCreatePet_ValidationError() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(null);

        ResponseEntity<?> response = petController.createPet(testPet, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetPet_Success() {
        when(petService.getPet(1)).thenReturn(testPet);

        ResponseEntity<?> response = petController.getPet(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testPet, response.getBody());
    }

    @Test
    public void testGetPet_NullId() {
        ResponseEntity<?> response = petController.getPet(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid ID supplied", response.getBody());
    }

    @Test
    public void testGetPet_NotFound() {
        when(petService.getPet(1)).thenReturn(null);

        ResponseEntity<?> response = petController.getPet(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Pet not found", response.getBody());
    }

    @Test
    public void testUpdatePet_Success() {
        when(petService.updatePet(1, "NewName", "PENDING")).thenReturn(true);

        ResponseEntity<?> response = petController.updatePet(1, "NewName", "PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());
    }

    @Test
    public void testUpdatePet_Failure() {
        when(petService.updatePet(1, "NewName", "PENDING")).thenReturn(false);

        ResponseEntity<?> response = petController.updatePet(1, "NewName", "PENDING");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody());
    }

    @Test
    public void testDeletePet_Success() throws UnauthorizedException {
        when(petService.deletePet(1, "api_key")).thenReturn(true);

        ResponseEntity<?> response = petController.deletePet(1, "api_key");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());
    }

    @Test
    public void testDeletePet_Failure() throws UnauthorizedException {
        when(petService.deletePet(1, "api_key")).thenReturn(false);

        ResponseEntity<?> response = petController.deletePet(1, "api_key");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid pet value", response.getBody());
    }

    @Test
    public void testDeletePet_Unauthorized() throws UnauthorizedException {
        when(petService.deletePet(1, "wrong_key")).thenThrow(new UnauthorizedException("Wrong API key"));

        assertThrows(UnauthorizedException.class, () -> {
            petController.deletePet(1, "wrong_key");
        });
    }
}