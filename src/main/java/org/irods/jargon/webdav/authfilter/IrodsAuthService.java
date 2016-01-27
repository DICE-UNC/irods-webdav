/**
 *
 */
package org.irods.jargon.webdav.authfilter;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.webdav.config.WebDavConfig;
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

	//private static final ThreadLocal<AuthResponse> authResponseCache = new ThreadLocal<AuthResponse>();
	private static final ThreadLocal<String> authIdCache = new ThreadLocal<String>();

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
		log.debug("authenticate()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("null or empty password");
		}

        /*
		log.debug("look in cache for cached login");
		AuthResponse cached = authResponseCache.get();

		if (cached != null) {
			log.debug("in thread local cache");
			if (!cached.getAuthenticatingIRODSAccount().getUserName()
					.equals(userName)) {
				log.warn("cache is not same as user name");
			} else {
				return cached;
			}
		}
        */

		/*
		 * Did not hit the thread local cache
		 */
		log.debug("login to irods and cache");

		IRODSAccount irodsAccount = null;


        // Try to get authenticated IRODSAccount from the IrodsAccountCacheManager
        try {
            irodsAccount = IrodsAccountCacheManager.getInstance().getIRODSAccount(userName, password);
            log.debug("authenticated IRODSAccount obtained from cache: {}", irodsAccount);
		} catch (IrodsAccountCacheManagerError e) {
            log.error("fail to retrieve cached IRODSAccount", e);
        }

        if ( irodsAccount == null ) {
            try {
	 	   	irodsAccount = getIrodsAccountFromAuthValues(userName, password);
	 	   } catch (JargonException e) {
	 	   	log.error("jargon exception creating IRODSAccount", e);
	 	   	throw new WebDavException("exception in auth", e);
	 	   }
        }

		if (irodsAccessObjectFactory == null) {
			throw new ConfigurationRuntimeException(
					"null irodsAccessObjectFactory");
		}

		log.debug("authenticating:{}", irodsAccount);
		try {
			AuthResponse response = irodsAccessObjectFactory
					.authenticateIRODSAccount(irodsAccount);
            
			authIdCache.set(IrodsAccountCacheManager.getAuthId(userName, password));
			//authResponseCache.set(response);
			return response;
		} catch (IrodsAccountCacheManagerError e) {
			log.error("auth cache manager error", e);
			throw new WebDavException("exception in auth", e);
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

		log.debug("getIrodsAccountFromAuthValues");

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
			log.debug("unspecified authScheme, use STANDARD");
			authScheme = AuthScheme.STANDARD;
		} else if (webDavConfig.getAuthScheme().equals(
				AuthScheme.STANDARD.toString())) {
			log.debug("using standard auth");
			authScheme = AuthScheme.STANDARD;
		} else if (webDavConfig.getAuthScheme().equals(
				AuthScheme.PAM.toString())) {
			log.debug("using PAM");
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
		log.debug("retrieveIrodsAccountAssociatedWithThread()");
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
		log.debug("retrieveCurrentIrodsAccount()");
		//AuthResponse authResponse = authResponseCache.get();
		String authId = authIdCache.get();
		if (authId == null) {
			throw new WebDavRuntimeException("no authId in cache");
		}

        IRODSAccount irodsAccount = null;
        try {
		    irodsAccount = IrodsAccountCacheManager.getInstance().getIRODSAccount(authId);
        } catch (IrodsAccountCacheManagerError e) {
			throw new WebDavRuntimeException(e);
        }

        return irodsAccount;
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
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
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
	public void setWebDavConfig(final WebDavConfig webDavConfig) {
		this.webDavConfig = webDavConfig;
	}

}
