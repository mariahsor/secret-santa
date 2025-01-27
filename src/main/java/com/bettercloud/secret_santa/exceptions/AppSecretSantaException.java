package com.bettercloud.secret_santa.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serial;

@Getter
@Setter
public class AppSecretSantaException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3132537971813769195L;
    private final Integer code;
    private final String status;

    public AppSecretSantaException(String message, int code, String status) {
        super(message);
        this.code=code;
        this.status=status;
    }
    @Serial
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }

    @Serial
    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
