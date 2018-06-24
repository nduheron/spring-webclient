package fr.nduheron.poc.springwebclient.exception;

/**
 * Exception utilisée quand une resource n'existe pas. Le message est utilisé
 * pour les logs.
 */
public class NotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}

}
