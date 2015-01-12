/**
 * 
 */
package org.irods.jargon.webdav.resource;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.webdav.config.IrodsAuthService;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for resources
 * 
 * @author Mike Conway
 * 
 */
public abstract class BaseResource {

	private static final ThreadLocal<AuthResponse> authResponseCache = new ThreadLocal<AuthResponse>();

	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private WebDavConfig webDavConfig;
	private final IrodsFileSystemResourceFactory factory;
	private IrodsFileContentService contentService;

	private static final Logger log = LoggerFactory
			.getLogger(BaseResource.class);

	/**
	 * Get the <code>IRODSAccount</code> for this operation
	 * 
	 * @return {@link IRODSAccount}
	 */
	protected IRODSAccount retrieveIrodsAccount() {
		return IrodsAuthService.retrieveCurrentIrodsAccount();
	}

	/**
	 * Get the <code>IRODSFileFactory</code> for the current user
	 * 
	 * @return {@link IRODSFileFactory} that can be used to create file objects
	 */
	protected IRODSFileFactory instanceIrodsFileFactory() {
		try {
			return irodsAccessObjectFactory
					.getIRODSFileFactory(retrieveIrodsAccount());
		} catch (JargonException e) {
			log.error("jargon error retrieving irodsFileFactory", e);
			throw new WebDavRuntimeException("unable to get irodsFileFactory",
					e);
		}
	}

	/**
	 * Default constructor
	 * 
	 * @param irodsAccessObjectFactory
	 * @param webDavConfig
	 */
	public BaseResource(final IrodsFileSystemResourceFactory factory,
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final WebDavConfig webDavConfig,
			final IrodsFileContentService contentService) {
		super();

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (webDavConfig == null) {
			throw new IllegalArgumentException("null webDavConfig");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.webDavConfig = webDavConfig;
		this.factory = factory;
		this.contentService = contentService;
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	protected IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	protected void setIrodsAccessObjectFactory(
			IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * @return the webDavConfig
	 */
	protected WebDavConfig getWebDavConfig() {
		return webDavConfig;
	}

	/**
	 * @param webDavConfig
	 *            the webDavConfig to set
	 */
	protected void setWebDavConfig(WebDavConfig webDavConfig) {
		this.webDavConfig = webDavConfig;
	}

	/**
	 * @return the factory
	 */
	protected IrodsFileSystemResourceFactory getFactory() {
		return factory;
	}

	protected io.milton.http.SecurityManager getSecurityManager() {
		return factory.getSecurityManager();
	}

	/**
	 * @return the contentService
	 */
	protected IrodsFileContentService getContentService() {
		return contentService;
	}

	/**
	 * @param contentService
	 *            the contentService to set
	 */
	protected void setContentService(IrodsFileContentService contentService) {
		this.contentService = contentService;
	}

}
