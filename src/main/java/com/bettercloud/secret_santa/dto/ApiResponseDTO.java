package com.bettercloud.secret_santa.dto;

import com.bettercloud.secret_santa.util.Meta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ApiResponseDTO
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(
        chain  = true,
        fluent = false
)
public class ApiResponseDTO {

    private Meta meta;
    private Object data;

}
