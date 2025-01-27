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

        when(participantRepository.save(any(Participant.class)))
                .thenAnswer(invocation -> {
                    Participant participant = invocation.getArgument(0);
                    participant.setId(participant.getEmail().equals("john@example.com") ? 1 : 2);
                    return participant;
                });

        when(logAssignmentRepository.countRecentAssignments(anyInt(), anyInt(), anyInt()))
                .thenReturn(0);

        ApiResponseDTO response = secretSantaService.createAssignments(validParticipants, 2023);

        assertNotNull(response);
        assertNotNull(response.getData());

        verify(logAssignmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createAssignments_OddNumberOfParticipants_ThrowsException() {

        List<ParticipantRequestDTO> oddParticipants = List.of(
                new ParticipantRequestDTO("John Doe", 1, "john@example.com")
        );

        AppSecretSantaException exception = assertThrows(
                AppSecretSantaException.class,
                () -> secretSantaService.createAssignments(oddParticipants, 2023)
        );

        assertEquals("The participant list must have an even number of elements", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getCode());
    }

    @Test
    void createAssignments_NoValidAssignment_ThrowsException() {

        when(participantRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class)))
                .thenAnswer(invocation -> {
                    Participant p = invocation.getArgument(0);
                    p.setId(p.getEmail().equals("john@example.com") ? 1 : 2);
                    return p;
                });

        when(logAssignmentRepository.countRecentAssignments(anyInt(), anyInt(), anyInt()))
                .thenReturn(1);

        AppSecretSantaException exception = assertThrows(
                AppSecretSantaException.class,
                () -> secretSantaService.createAssignments(validParticipants, 2023)
        );

        assertTrue(exception.getMessage().contains("No valid assignment found"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getCode());
    }
}
