/**
 *
 */
package org.irods.jargon.webdav.authfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet filter implements basic auth
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class BasicAuthFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private WebDavConfig webDavConfig;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private IrodsAuthService irodsAuthService;

	/**
	 *
	 */
	public BasicAuthFilter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		log.debug("doFilter()");

		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		String auth = httpRequest.getHeader("Authorization");

		if (auth == null || auth.isEmpty()) {
			log.error("auth null or empty");
			sendAuthError(httpResponse);
			return;
		}

		AuthResponse authResponse = null;
		try {

			UserAndPassword userAndPassword = WebDavAuthUtils
					.getAccountFromBasicAuthValues(auth, webDavConfig);
			log.debug("account for auth:{}", userAndPassword.getUserId());

			authResponse = irodsAuthService.authenticate(
					userAndPassword.getUserId(), userAndPassword.getPassword());

			log.debug("authResponse:{}", authResponse);
			log.debug("success!");

			chain.doFilter(httpRequest, httpResponse);
			return;

		} catch (JargonException e) {
			log.warn("auth exception", e);
			sendAuthError(httpResponse);
			return;
		}

	}

	private void sendAuthError(final HttpServletResponse httpResponse)
			throws IOException {
		httpResponse.setHeader("WWW-Authenticate", "Basic realm=\""
				+ webDavConfig.getRealm() + "\"");
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {

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

}
