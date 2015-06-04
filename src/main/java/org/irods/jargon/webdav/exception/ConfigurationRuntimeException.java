/**
 *
 */
package org.irods.jargon.webdav.exception;

import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * Runtime exception in the configuration of WebDav
 *
 * @author Mike Conway - DICE
 *
 */
public class ConfigurationRuntimeException extends JargonRuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 7961867030415020285L;

	/**
	 *
	 */
	public ConfigurationRuntimeException() {
	}

	/**
	 * @param message
	 */
	public ConfigurationRuntimeException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConfigurationRuntimeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConfigurationRuntimeException(final String message,
			final Throwable cause) {
		super(message, cause);
	}

}
