/**
 *
 */
package org.irods.jargon.webdav.resource;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.webdav.authfilter.IrodsAuthService;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.SecurityManager;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.Resource;

/**
 * Security manager implementation for iRODS
 *
 * <p/>
 * This class provides a method that will extract user and password info from
 * these thread locals, and provision a threadLocal that contains the
 * authResponse data from iRODS, allowing resource servers to derive the valid
 * iRODS account for each operation
 *
 * @author Mike Conway - DICE
 *
 */
public class IrodsSecurityManager implements SecurityManager {

	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private WebDavConfig webDavConfig;
	private IrodsAuthService irodsAuthService;
	private static final Logger log = LoggerFactory.getLogger(IrodsSecurityManager.class);

	private static final ThreadLocal<AuthResponse> authResponseCache = new ThreadLocal<AuthResponse>();

	/**
	 *
	 */
	public IrodsSecurityManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.milton.http.SecurityManager#authenticate(io.milton.http.http11.auth
	 * .DigestResponse)
	 */
	@Override
	public Object authenticate(final DigestResponse digest) {
		throw new WebDavRuntimeException("digest auth is not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.http.SecurityManager#authenticate(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Object authenticate(final String userName, final String password) {
		log.info("authenticate()");
		// clearThreadlocals();

		AuthResponse authResponse = irodsAuthService.getCached(userName, password);

		if (authResponse == null) {
			throw new WebDavRuntimeException("no cached authentication!");
		}
		// log.info("storing authResponse in threadlocal as
		// authResponseCache");
		// authResponseCache.set(authResponse);
		return authResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.http.SecurityManager#authorise(io.milton.http.Request,
	 * io.milton.http.Request.Method, io.milton.http.Auth,
	 * io.milton.resource.Resource)
	 */
	@Override
	public boolean authorise(final Request arg0, final Method arg1, final Auth arg2, final Resource arg3) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.http.SecurityManager#getRealm(java.lang.String)
	 */
	@Override
	public String getRealm(final String host) {
		// right now hard-coded
		return "irods";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.http.SecurityManager#isDigestAllowed()
	 */
	@Override
	public boolean isDigestAllowed() {
		return false;
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
	public void setIrodsAccessObjectFactory(final IRODSAccessObjectFactory irodsAccessObjectFactory) {
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

	/**
	 * @return the irodsAuthService
	 */
	public IrodsAuthService getIrodsAuthService() {
		return irodsAuthService;
	}

	/**
	 * @param irodsAuthService
	 *            the irodsAuthService to set
	 */
	public void setIrodsAuthService(final IrodsAuthService irodsAuthService) {
		this.irodsAuthService = irodsAuthService;
	}

	public static void clearThreadlocals() {
		authResponseCache.remove();
	}

}
