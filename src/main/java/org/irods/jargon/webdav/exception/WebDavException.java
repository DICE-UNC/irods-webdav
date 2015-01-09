/**
 * 
 */
package org.irods.jargon.webdav.exception;

import org.irods.jargon.core.exception.JargonException;

/**
 * Catch-all base checked exception for WebDav processing
 * 
 * @author Mike Conway - DICE
 * 
 */
public class WebDavException extends JargonException {

	/**
	 * @param message
	 */
	public WebDavException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WebDavException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public WebDavException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WebDavException(String message, Throwable cause,
			int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WebDavException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public WebDavException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
