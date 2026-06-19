package com.jeil.delivery.configuration.error;

public class InsertCheckedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "ms.saveErr";

    public InsertCheckedException () {
    	super(MESSAGE);
    }

    public InsertCheckedException (String msg) {
    	super(msg);
    }
}
