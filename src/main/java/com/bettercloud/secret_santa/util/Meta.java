package com.bettercloud.secret_santa.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

/**
 * Meta
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Meta {

    @Builder.Default
    private String transactionID = randomUUID().toString();

    private String status;

    private int statusCode;

    @Builder.Default
    private String timestamp = now().toString();

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String devMessage;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String message;

    public Meta(String transactionID, String status, int statusCode) {
        this.transactionID = transactionID;
        this.status = status;
        this.statusCode = statusCode;
        this.timestamp = LocalDateTime.now().toString();
    }

    public Meta(String transactionID, String status, int statusCode, String message) {
        this.transactionID = transactionID;
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }

}
