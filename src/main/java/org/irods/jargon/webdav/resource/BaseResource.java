/**
 * 
 */
package org.irods.jargon.webdav.resource;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.Resource;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.webdav.authfilter.IrodsAuthService;
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
public abstract class BaseResource implements Resource, MoveableResource,
		CopyableResource {

	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private WebDavConfig webDavConfig;
	private final IrodsFileSystemResourceFactory factory;
	private IrodsFileContentService contentService;
	private String ssoPrefix = null;

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

	protected IRODSFile fileFromCollectionResource(
			CollectionResource collectionResource, String name) {
		if (collectionResource == null) {
			throw new IllegalArgumentException("null collectionResource");
		}

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}

		IRODSFile dest;

		if (collectionResource instanceof IrodsDirectoryResource) {
			try {
				IrodsDirectoryResource newFsParent = (IrodsDirectoryResource) collectionResource;

				dest = this.instanceIrodsFileFactory().instanceIRODSFile(
						newFsParent.getDir().getAbsolutePath(), name);
				return dest;
			} catch (JargonException e) {
				log.error("jargon exception on copy", e);
				throw new WebDavRuntimeException("exception in copy", e);
			}

		} else {
			log.error("unknown destination type for:{}", collectionResource);
			throw new WebDavRuntimeException(
					"Destination is an unknown type. Must be a FsDirectoryResource");
		}

	}

	/**
	 * Will redirect if a default page has been specified on the factory
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public String checkRedirect(Request request) {
		if (getFactory().getDefaultPage() != null) {
			return request.getAbsoluteUrl() + "/"
					+ getFactory().getDefaultPage();
		} else {
			return null;
		}
	}

	@Override
	public void copyTo(CollectionResource destinationPath, String newName)
			throws NotAuthorizedException, BadRequestException,
			ConflictException {

		log.info("copyTo()");
		if (destinationPath == null) {
			throw new IllegalArgumentException("null destinationPath");
		}

		if (newName == null || newName.isEmpty()) {
			throw new IllegalArgumentException("null or empty newName");
		}

		IRODSFile dest = this.fileFromCollectionResource(destinationPath,
				newName);
		doCopy(dest);

	}

	/**
	 * Accomplish a copy with the given file
	 * 
	 * @param dest
	 */
	protected abstract void doCopy(IRODSFile dest);

	@Override
	public Object authenticate(String user, String password) {
		return factory.getSecurityManager().authenticate(user, password);
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		boolean b = factory.getSecurityManager().authorise(request, method,
				auth, this);
		if (log.isTraceEnabled()) {
			log.trace("authorise: result=" + b);
		}
		return b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#getRealm()
	 */
	@Override
	public String getRealm() {
		String r = factory.getRealm(this.getWebDavConfig().getHost());
		if (r == null) {
			throw new NullPointerException("Got null realm from: "
					+ factory.getClass() + " for host="
					+ this.getWebDavConfig().getHost());
		}
		return r;
	}

	/**
	 * @return the ssoPrefix
	 */
	public String getSsoPrefix() {
		return ssoPrefix;
	}

	/**
	 * @param ssoPrefix
	 *            the ssoPrefix to set
	 */
	public void setSsoPrefix(String ssoPrefix) {
		this.ssoPrefix = ssoPrefix;
	}

}
