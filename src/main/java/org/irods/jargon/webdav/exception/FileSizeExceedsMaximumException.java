package org.irods.jargon.webdav.exception;

import org.irods.jargon.core.exception.JargonRuntimeException;

/**
 * File size exceeds a configured maximum
 *
 * @author Mike Conway - DICE
 *
 */
public class FileSizeExceedsMaximumException extends JargonRuntimeException {

	private static final long serialVersionUID = 124144366850943280L;

	public FileSizeExceedsMaximumException(final String message) {
		super(message);
	}

}
