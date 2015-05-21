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
	 *
	 */
	private static final long serialVersionUID = 5779246158604949830L;

	/**
	 * @param message
	 */
	public WebDavException(final String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WebDavException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public WebDavException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WebDavException(final String message, final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param cause
	 * @param underlyingIRODSExceptionCode
	 */
	public WebDavException(final Throwable cause,
			final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	/**
	 * @param message
	 * @param underlyingIRODSExceptionCode
	 */
	public WebDavException(final String message,
			final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
