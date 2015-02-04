/**
 * 
 */
package org.irods.jargon.webdav.authfilter;

import org.apache.commons.codec.binary.Base64;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class WebDavAuthUtils {

	private static Logger log = LoggerFactory.getLogger(WebDavAuthUtils.class);

	public static String basicAuthTokenFromIRODSAccount(
			final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");

		StringBuilder toEncode = new StringBuilder();
		toEncode.append(irodsAccount.getUserName());
		toEncode.append(":");
		toEncode.append(irodsAccount.getPassword());

		sb.append(Base64.encodeBase64String(toEncode.toString().getBytes()));
		return sb.toString();
	}

	/**
	 * Given the raw 'basic' auth header (with the Basic prefix), build an iRODS
	 * account
	 * 
	 * @param basicAuthData
	 * @param restConfiguration
	 * @return {@link UserAndPassword}
	 * @throws JargonException
	 */
	public static UserAndPassword getAccountFromBasicAuthValues(
			final String basicAuthData, final WebDavConfig webDavConfig)
			throws JargonException {

		log.debug("getIRODSAccountFromBasicAuthValues");

		if (basicAuthData == null || basicAuthData.isEmpty()) {
			throw new IllegalArgumentException("null or empty basicAuthData");
		}

		if (webDavConfig == null) {
			throw new IllegalArgumentException("null webDavConfig");
		}

		final int index = basicAuthData.indexOf(' ');
		log.debug("index of end of basic prefix:{}", index);
		String auth = basicAuthData.substring(index);

		String decoded = new String(Base64.decodeBase64(auth));

		log.debug("index of end of basic prefix:{}", index);
		if (decoded.isEmpty()) {
			throw new JargonException("user and password not in credentials");

		}
		final String[] credentials = decoded.split(":");

		log.debug("credentials:{}", credentials);

		if (credentials.length != 2) {
			throw new JargonException("user and password not in credentials");
		}

		log.debug("webDavConfig:{}", webDavConfig);

		UserAndPassword userAndPassword = new UserAndPassword();
		userAndPassword.setUserId(credentials[0]);
		userAndPassword.setPassword(credentials[1]);
		return userAndPassword;
	}

}
