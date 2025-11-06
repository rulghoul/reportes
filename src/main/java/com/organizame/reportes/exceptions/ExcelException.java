package com.organizame.reportes.exceptions;

public class ExcelException extends RuntimeException {
    public ExcelException(String message) {
        super("Fallo la creacion del excep por:" + message);
    }
}
