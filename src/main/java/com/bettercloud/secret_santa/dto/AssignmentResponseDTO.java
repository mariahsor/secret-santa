package com.bettercloud.secret_santa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * AssignmentResponseDTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponseDTO{

    private Integer logId;
    private Integer giverId;
    private Integer receiverId;

}

