package com.bettercloud.secret_santa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * ParticipantRequestDTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantRequestDTO {
    private String name;
    private Integer tempFamilyId; //A temporary ID. Used to correlate participants before they are persisted in the database.It could be null.
    private String email;
}
