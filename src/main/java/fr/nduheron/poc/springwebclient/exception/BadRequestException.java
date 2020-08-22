package fr.nduheron.poc.springwebclient.exception;

import java.util.List;

import fr.nduheron.poc.springwebclient.exception.model.ErrorParameter;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private List<ErrorParameter> errors;

	public BadRequestException(List<ErrorParameter> errors) {
		super();
		this.errors = errors;
	}

	public BadRequestException(List<ErrorParameter> errors, Throwable throwable) {
		super(throwable);
		this.errors = errors;
	}

	public List<ErrorParameter> getErrors() {
		return errors;
	}

}
