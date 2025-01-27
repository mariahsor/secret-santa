package com.bettercloud.secret_santa.services;

import com.bettercloud.secret_santa.dto.ApiResponseDTO;
import com.bettercloud.secret_santa.dto.ParticipantRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SecretSantaService {
    ApiResponseDTO createAssignments(List<ParticipantRequestDTO> participantRequestDTOList, int currentYear);
}
