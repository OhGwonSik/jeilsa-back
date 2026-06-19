package com.jeil.delivery.configuration.error;

public class NoDataException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE = "데이터가 없습니다.";

	public NoDataException() {
		super(MESSAGE);
	}

	public NoDataException(String msg) {
		super(msg);
	}
}
