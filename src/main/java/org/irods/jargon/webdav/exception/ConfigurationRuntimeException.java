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
	public ConfigurationRuntimeException() {
	}

	/**
	 * @param message
	 */
	public ConfigurationRuntimeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConfigurationRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConfigurationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
