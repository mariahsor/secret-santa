package com.bettercloud.secret_santa.mappers;

import com.bettercloud.secret_santa.dto.AssignmentResponseDTO;
import com.bettercloud.secret_santa.entities.LogAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for mapping assignment structures to LogAssignment entities
 * and converting them to DTO lists.
 * <p>
 * This class is not intended for instantiation.
 */
public class LogAssignmentMapper {

    private LogAssignmentMapper() {
        throw new IllegalStateException("This utility class cannot be instantiated.");
    }

    /**
     * Converts a map of (giverId -> receiverId) into a list of LogAssignment
     * entities for the specified year.
     */
    public static List<LogAssignment> fromFinalAssignments(Map<Integer, Integer> finalAssignments, int currentYear) {
        List<LogAssignment> logsToSave = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : finalAssignments.entrySet()) {
            Integer giverId = entry.getKey();
            Integer receiverId = entry.getValue();

            LogAssignment log = new LogAssignment();
            log.setGiverId(giverId);
            log.setReceiverId(receiverId);
            log.setYear(currentYear);

            logsToSave.add(log);
        }
        return logsToSave;
    }

    /**
     * Converts a list of LogAssignment entities to a list of AssignmentResponseDTO objects.
     */
    public static List<AssignmentResponseDTO> toDtoList(List<LogAssignment> assignments) {
        List<AssignmentResponseDTO> dtoList = new ArrayList<>();
        for (LogAssignment log : assignments) {
            dtoList.add(new AssignmentResponseDTO(
                    log.getId(),
                    log.getGiverId(),
                    log.getReceiverId()
            ));
        }
        return dtoList;
    }


}
