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
	private static final long serialVersionUID = -6227220359780082224L;

	/**
	 *
	 */
	public WebDavRuntimeException() {
	}

	/**
	 * @param message
	 */
	public WebDavRuntimeException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public WebDavRuntimeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WebDavRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
