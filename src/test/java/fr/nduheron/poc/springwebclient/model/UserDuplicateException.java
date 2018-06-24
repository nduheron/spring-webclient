package fr.nduheron.poc.springwebclient.model;

import fr.nduheron.poc.springwebclient.exception.FunctionalException;

public class UserDuplicateException extends FunctionalException {

	private static final long serialVersionUID = 1L;

	public UserDuplicateException(int id) {
		super("user.duplicate", String.valueOf(id));
	}

	@Override
	public String getCode() {
		return "DuplicateId";
	}

}
