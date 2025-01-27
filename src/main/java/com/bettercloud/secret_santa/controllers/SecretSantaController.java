package com.bettercloud.secret_santa.controllers;

import com.bettercloud.secret_santa.dto.ApiResponseDTO;
import com.bettercloud.secret_santa.dto.ParticipantRequestDTO;
import com.bettercloud.secret_santa.services.SecretSantaService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/secret-santa")
public class SecretSantaController {
    private final SecretSantaService secretSantaService;

    public SecretSantaController(SecretSantaService secretSantaService) {
        this.secretSantaService = secretSantaService;
    }

    @PostMapping("/generate/{year}")
    public ApiResponseDTO generate(@PathVariable("year") int currentYear,
                                   @RequestBody List<ParticipantRequestDTO> participantRequestDTOList) {
        return this.secretSantaService.createAssignments(participantRequestDTOList, currentYear);
    }
}
