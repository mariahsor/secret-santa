package com.bettercloud.secret_santa.services.impl;

import com.bettercloud.secret_santa.dto.ApiResponseDTO;
import com.bettercloud.secret_santa.dto.AssignmentResponseDTO;
import com.bettercloud.secret_santa.dto.ParticipantRequestDTO;
import com.bettercloud.secret_santa.entities.LogAssignment;
import com.bettercloud.secret_santa.entities.Participant;
import com.bettercloud.secret_santa.exceptions.AppSecretSantaException;
import com.bettercloud.secret_santa.mappers.LogAssignmentMapper;
import com.bettercloud.secret_santa.repositories.LogAssignmentRepository;
import com.bettercloud.secret_santa.repositories.ParticipantRepository;
import com.bettercloud.secret_santa.services.SecretSantaService;
import com.bettercloud.secret_santa.util.Meta;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class SecretSantaServiceImpl implements SecretSantaService {
    private final ParticipantRepository participantRepository;
    private final LogAssignmentRepository logAssignmentRepository;

    public SecretSantaServiceImpl(ParticipantRepository participantRepository, LogAssignmentRepository logAssignmentRepository) {
        this.participantRepository = participantRepository;
        this.logAssignmentRepository = logAssignmentRepository;
    }

    private final Meta meta = new Meta(UUID.randomUUID().toString(), "OK", 200);

    /**
     * Creates Secret Santa assignments for the given list of participants,
     * applying the necessary constraints.
     *
     * @param participantRequestDTOList list of participants to be processed
     * @param currentYear               the current year for the assignments
     * @return an ApiResponseDTO with the assignment results
     */
    @Override
    @Transactional
    public ApiResponseDTO createAssignments(List<ParticipantRequestDTO> participantRequestDTOList, int currentYear) {

        // 1. Ensure an even number of participants
        if (participantRequestDTOList.size() % 2 != 0) {
            throw new AppSecretSantaException("The participant list must have an even number of elements",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name()
            );
        }

        // 2. Find or create participants in the database
        List<Participant> participants = ensureParticipantsExist(participantRequestDTOList);

        // 3. Map participantId -> familyId and collect participant IDs
        Map<Integer, Integer> familyMap = new HashMap<>();
        List<Integer> participantIds = new ArrayList<>();

        for (int i = 0; i < participants.size(); i++) {
            Participant participant = participants.get(i);
            participantIds.add(participant.getId());

            Integer familyId = participantRequestDTOList.get(i).getTempFamilyId();
            familyMap.put(participant.getId(), familyId);
        }

        //4. Perform the backtracking assignment process
        Map<Integer, Integer> finalAssignments = new HashMap<>();
        // Shuffle the list to ensure randomness
        Collections.shuffle(participantIds);
        boolean success = assignGifts(0, participantIds, finalAssignments, currentYear, familyMap);

        if (!success) {
            throw new AppSecretSantaException("No valid assignment found with the current constraints",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name());
        }

        // 5. Convert the final assignments to LogAssignment entities and save them
        List<LogAssignment> logsToSaveList = LogAssignmentMapper.fromFinalAssignments(finalAssignments, currentYear);
        logAssignmentRepository.saveAll(logsToSaveList);

        List<AssignmentResponseDTO> assignmentResponseDTOList = LogAssignmentMapper.toDtoList(logsToSaveList);

        ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
        apiResponseDTO.setMeta(meta);
        apiResponseDTO.setData(assignmentResponseDTOList);

        return apiResponseDTO;
    }

    /**
     * Checks whether each participant (by email) already exists in the database.
     * If a participant does not exist, a new record is created.
     *
     * @param participantRequestDTOList list of potential participants
     * @return a list of Participant entities with valid IDs
     */
    private List<Participant> ensureParticipantsExist(List<ParticipantRequestDTO> participantRequestDTOList) {
        List<Participant> result = new ArrayList<>();

        for (ParticipantRequestDTO participantRequestDTO : participantRequestDTOList) {
            Optional<Participant> participantExists = participantRepository.findByEmail(participantRequestDTO.getEmail());

            Participant participant;
            if (participantExists.isPresent()) {
                participant = participantExists.get();

                // Optionally update the name if needed
                if (!Objects.equals(participant.getName(), participantRequestDTO.getName())) {
                    participant.setName(participantRequestDTO.getName());
                    participantRepository.save(participant);
                }
            } else {
                // Create a new participant if not found
                participant = new Participant();
                participant.setName(participantRequestDTO.getName());
                participant.setEmail(participantRequestDTO.getEmail());
                participant = participantRepository.save(participant);
            }
            result.add(participant);
        }

        return result;
    }

    /**
     * A backtracking algorithm that assigns a receiver to each giver under the following constraints:
     * - No self-assignments
     * - Do not assign a receiver with the same familyId
     * - Do not repeat the same (giver, receiver) pair from the last 3 years
     *
     * @param index          current position in the participant list
     * @param participantIds the list of participant IDs
     * @param result         a map of final assignments (giver -> receiver)
     * @param currentYear    the current year for the assignments
     * @param familyMap      a map linking participantId to familyId
     * @return true if all participants could be assigned without breaking constraints, false otherwise
     */
    private boolean assignGifts(int index,
                                List<Integer> participantIds,
                                Map<Integer, Integer> result,
                                int currentYear,
                                Map<Integer, Integer> familyMap) {

        // Base case: all participants have been assigned
        if (index == participantIds.size()) {
            return true;
        }

        Integer giverId = participantIds.get(index);

        // Potential candidates for giverId
        List<Integer> candidatesList = new ArrayList<>();

        for (Integer receiverId : participantIds) {

            Integer giverFamilyId = familyMap.get(giverId);
            Integer receiverFamilyId = familyMap.get(receiverId);

            // Check all constraints in a single if
            // 1) Not the same person
            // 2) Not the same family if both familyIds are non-null
            // 3) Receiver not already used
            // 4) Not repeated in the last 3 years
            if (!receiverId.equals(giverId)
                    && !(giverFamilyId != null && giverFamilyId.equals(receiverFamilyId))
                    && !result.containsValue(receiverId)
                    && isNotRepeatedIn3Years(giverId, receiverId, currentYear)) {

                // If we get here, it's a valid candidate
                candidatesList.add(receiverId);
            }
        }

        // Try each candidate using backtracking
        for (Integer candidate : candidatesList) {
            result.put(giverId, candidate);
            if (assignGifts(index + 1, participantIds, result, currentYear, familyMap)) {
                return true;
            }
            //Revert this assignment if subsequent steps fail
            result.remove(giverId);
        }

        // No valid assignment found at this stage
        return false;
    }

    /**
     * Checks whether the (giver, receiver) pair has not been used in the last 3 years.
     * @return True if this pair has not occurred in the last 3 years; false otherwise.
     */
    private boolean isNotRepeatedIn3Years(Integer giverId, Integer receiverId, int currentYear) {
        int yearLimit = currentYear - 2;
        int count = logAssignmentRepository.countRecentAssignments(giverId, receiverId, yearLimit);
        return (count == 0);
    }

}
