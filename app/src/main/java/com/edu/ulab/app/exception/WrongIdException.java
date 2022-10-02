package com.edu.ulab.app.exception;

public class WrongIdException extends RuntimeException{
    public WrongIdException(String message){
        super("Field id is bad format: " + message);
    }
}
