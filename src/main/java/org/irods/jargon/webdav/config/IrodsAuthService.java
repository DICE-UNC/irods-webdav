/**
 * 
 */
package org.irods.jargon.webdav.config;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.webdav.exception.ConfigurationRuntimeException;
import org.irods.jargon.webdav.exception.WebDavException;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to handle iRODS authentication, and conversion of auth tokens to
 * iRODS accounts
 * 
 * 
 * @author Mike Conway - DICE
 * 
 */
public class IrodsAuthService {

	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private WebDavConfig webDavConfig;

	private static final ThreadLocal<AuthResponse> authResponseCache = new ThreadLocal<AuthResponse>();

	private static final Logger log = LoggerFactory
			.getLogger(IrodsAuthService.class);

	/**
	 * Given a user name and password, authenticate the given user and return
	 * account info
	 * 
	 * @param userName
	 *            <code>String</code> with iRODS user name
	 * @param password
	 *            <code>String</code> with iRODS password
	 * @return {@link AuthResponse} with the authenticated user information
	 * @throws AuthenticationException
	 * @throws JargonException
	 */
	public AuthResponse authenticate(final String userName,
			final String password) throws AuthenticationException,
			WebDavException {
		log.info("authenticate()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("null or empty password");
		}

		IRODSAccount irodsAccount;
		try {
			irodsAccount = getIrodsAccountFromAuthValues(userName, password);
		} catch (JargonException e) {
			log.error("jargon exception creating IRODSAccount", e);
			throw new WebDavException("exception in auth", e);
		}

		if (irodsAccessObjectFactory == null) {
			throw new ConfigurationRuntimeException(
					"null irodsAccessObjectFactory");
		}

		log.info("authenticating:{}", irodsAccount);
		try {
			AuthResponse response = irodsAccessObjectFactory
					.authenticateIRODSAccount(irodsAccount);
			authResponseCache.set(response);
			return response;
		} catch (AuthenticationException e) {
			log.error("auth exception", e);
			throw e;
		} catch (JargonException je) {
			log.error("jargon exception during auth", je);
			throw new WebDavException("exception in auth", je);
		}

	}

	/**
	 * Given a user name and password, interpolate with the configuration to
	 * derive iRODS accounts
	 * 
	 * @param userName
	 *            <code>String</code> user name
	 * @param password
	 *            <code>String</code> password
	 * @return {@link IRODSAccount} for this user, based on other configuration
	 * @throws JargonException
	 */
	public IRODSAccount getIrodsAccountFromAuthValues(final String userName,
			final String password) throws JargonException {

		log.info("getIrodsAccountFromAuthValues");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("null or empty password");
		}

		if (webDavConfig == null) {
			throw new ConfigurationRuntimeException(
					"webDavConfig not available");
		}

		AuthScheme authScheme;
		if (webDavConfig.getAuthScheme() == null
				|| webDavConfig.getAuthScheme().isEmpty()) {
			log.info("unspecified authScheme, use STANDARD");
			authScheme = AuthScheme.STANDARD;
		} else if (webDavConfig.getAuthScheme().equals(
				AuthScheme.STANDARD.toString())) {
			log.info("using standard auth");
			authScheme = AuthScheme.STANDARD;
		} else if (webDavConfig.getAuthScheme().equals(
				AuthScheme.PAM.toString())) {
			log.info("using PAM");
			authScheme = AuthScheme.PAM;
		} else {
			log.error("cannot support authScheme:{}", webDavConfig);
			throw new ConfigurationRuntimeException(
					"unknown or unsupported auth scheme");
		}

		return IRODSAccount.instance(webDavConfig.getHost(),
				webDavConfig.getPort(), userName, password, "",
				webDavConfig.getZone(),
				webDavConfig.getDefaultStorageResource(), authScheme);
	}

	/**
	 * Get the iRODS account to use in actual connections to iRODS
	 * 
	 * @param authResponse
	 * @link AuthResponse} that came from an authentication attempt
	 * @return {@link IRODSAccount} suitable for providing when interacting with
	 *         Jargon
	 */
	public static IRODSAccount retrieveIrodsAccountFromAuthResponse(
			final AuthResponse authResponse) {
		log.info("retrieveIrodsAccountAssociatedWithThread()");
		if (authResponse == null) {
			throw new IllegalArgumentException("null authResponse");
		}
		return authResponse.getAuthenticatingIRODSAccount();

	}

	/**
	 * Access the cache that holds the current authentication (in a thread
	 * local) and return an irodsAccount suitable for connecting to the grid.
	 * 
	 * @return {@link IRODSAccount} with the appropriate authentication
	 *         credential
	 */
	public static IRODSAccount retrieveCurrentIrodsAccount() {
		log.info("retrieveCurrentIrodsAccount()");
		AuthResponse authResponse = authResponseCache.get();
		if (authResponse == null) {
			throw new WebDavRuntimeException("no authResponseCache value");
		}
		return retrieveIrodsAccountFromAuthResponse(authResponse);
	}

	/**
	 * 
	 */
	public IrodsAuthService() {
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * @return the webDavConfig
	 */
	public WebDavConfig getWebDavConfig() {
		return webDavConfig;
	}

	/**
	 * @param webDavConfig
	 *            the webDavConfig to set
	 */
	public void setWebDavConfig(WebDavConfig webDavConfig) {
		this.webDavConfig = webDavConfig;
	}

}
