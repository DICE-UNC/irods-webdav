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

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to close iRODS sessions after a request
 * 
 * @author Mike Conway - DICE
 *
 */
public class ConnectionClosingFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * Injected dependency
	 */
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * 
	 */
	public ConnectionClosingFilter() {
	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		chain.doFilter(request, response);
		log.debug("closing iRODS connection after filter processing");
		this.irodsAccessObjectFactory.closeSessionAndEatExceptions();
	}

	@Override
	public void init(FilterConfig config) throws ServletException {

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

}
