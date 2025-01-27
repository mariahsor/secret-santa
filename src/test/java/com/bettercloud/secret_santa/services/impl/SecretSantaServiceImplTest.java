package com.bettercloud.secret_santa.services.impl;

import com.bettercloud.secret_santa.dto.ApiResponseDTO;
import com.bettercloud.secret_santa.dto.ParticipantRequestDTO;
import com.bettercloud.secret_santa.entities.Participant;
import com.bettercloud.secret_santa.exceptions.AppSecretSantaException;
import com.bettercloud.secret_santa.repositories.LogAssignmentRepository;
import com.bettercloud.secret_santa.repositories.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretSantaServiceImplTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogAssignmentRepository logAssignmentRepository;

    @InjectMocks
    private SecretSantaServiceImpl secretSantaService;

    private List<ParticipantRequestDTO> validParticipants;

    @BeforeEach
    void setUp() {
        validParticipants = List.of(
                new ParticipantRequestDTO("John Doe", 1, "john@example.com"),
                new ParticipantRequestDTO("Jane Doe", 2, "jane@example.com")
        );
    }

    @Test
    void createAssignments_Successful() {
        // GIVEN
        // Se simula que ninguno de los participantes existe, así que se crean
        when(participantRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(participantRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());

        // Se simula que al guardar el participante, se les asigna un ID en la BD
        when(participantRepository.save(any(Participant.class)))
                .thenAnswer(invocation -> {
                    Participant participant = invocation.getArgument(0);
                    // Suponiendo IDs autogenerados
                    participant.setId(participant.getEmail().equals("john@example.com") ? 1 : 2);
                    return participant;
                });

        // Simulamos que no hay repeticiones en los últimos 3 años
        when(logAssignmentRepository.countRecentAssignments(anyInt(), anyInt(), anyInt()))
                .thenReturn(0);

        // WHEN
        ApiResponseDTO response = secretSantaService.createAssignments(validParticipants, 2023);

        // THEN
        assertNotNull(response);
        assertNotNull(response.getData());
        //assertEquals("OK", response.getMeta().getMessage());
        // Dependiendo de tu implementación, podrías chequear que el tamaño de la lista
        // de assignments en response.getData() sea igual al número de participantes, etc.

        // Verificar que se haya guardado la asignación en el repositorio
        verify(logAssignmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createAssignments_OddNumberOfParticipants_ThrowsException() {
        // GIVEN
        // Lista con tamaño impar
        List<ParticipantRequestDTO> oddParticipants = List.of(
                new ParticipantRequestDTO("John Doe", 1, "john@example.com")
        );

        // WHEN - THEN
        AppSecretSantaException exception = assertThrows(
                AppSecretSantaException.class,
                () -> secretSantaService.createAssignments(oddParticipants, 2023)
        );

        assertEquals("The participant list must have an even number of elements", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getCode());
    }

    @Test
    void createAssignments_NoValidAssignment_ThrowsException() {
        // GIVEN
        // Para forzar que no haya un assignment válido, podemos simular que
        // countRecentAssignments devuelva un valor > 0 para todos y no permita
        // ninguna asignación.
        when(participantRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class)))
                .thenAnswer(invocation -> {
                    Participant p = invocation.getArgument(0);
                    p.setId(p.getEmail().equals("john@example.com") ? 1 : 2);
                    return p;
                });

        // Suponiendo que la asignación está repetida para romper la lógica y
        // que no se logre asignar nadie
        when(logAssignmentRepository.countRecentAssignments(anyInt(), anyInt(), anyInt()))
                .thenReturn(1);

        // WHEN - THEN
        AppSecretSantaException exception = assertThrows(
                AppSecretSantaException.class,
                () -> secretSantaService.createAssignments(validParticipants, 2023)
        );

        assertTrue(exception.getMessage().contains("No valid assignment found"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getCode());
    }
}
