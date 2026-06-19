package com.jeil.delivery.configuration.error;

public class UpdateCheckedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "ms.updateErr";

    public UpdateCheckedException() {
        super(MESSAGE);
    }

    public UpdateCheckedException(String msg) {
        super(msg);
    }

}
