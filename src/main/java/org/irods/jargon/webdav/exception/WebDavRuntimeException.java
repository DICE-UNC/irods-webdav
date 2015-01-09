/**
 * 
 */
package org.irods.jargon.webdav.exception;

import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * General runtime exception in the operation of WebDav
 * 
 * @author Mike Conway - DICE
 * 
 */
public class WebDavRuntimeException extends JargonRuntimeException {

	/**
	 * 
	 */
	public WebDavRuntimeException() {
	}

	/**
	 * @param message
	 */
	public WebDavRuntimeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public WebDavRuntimeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WebDavRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
