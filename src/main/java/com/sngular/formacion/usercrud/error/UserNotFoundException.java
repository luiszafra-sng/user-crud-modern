package com.sngular.formacion.usercrud.error;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuario " + id + " no encontrado.");
    }
}
