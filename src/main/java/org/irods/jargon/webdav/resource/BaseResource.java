/**
 *
 */
package org.irods.jargon.webdav.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.webdav.authfilter.IrodsAuthService;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.exception.ConfigurationRuntimeException;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.Principal;
import io.milton.resource.AccessControlledResource;
import io.milton.resource.AccessControlledResource.Priviledge;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.LockableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.Resource;

/**
 * Base for resources
 *
 * @author Mike Conway
 *
 */
public abstract class BaseResource implements Resource, MoveableResource, CopyableResource, LockableResource {

	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private WebDavConfig webDavConfig;
	private final IrodsFileSystemResourceFactory factory;
	private IrodsFileContentService contentService;
	private String ssoPrefix = null;
	private IRODSFile irodsFile = null;

	private static final Logger log = LoggerFactory.getLogger(BaseResource.class);

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
			return irodsAccessObjectFactory.getIRODSFileFactory(retrieveIrodsAccount());
		} catch (JargonException e) {
			log.error("jargon error retrieving irodsFileFactory", e);
			throw new WebDavRuntimeException("unable to get irodsFileFactory", e);
		}
	}

	/**
	 * Default constructor
	 *
	 * @param irodsAccessObjectFactory
	 * @param webDavConfig
	 */
	public BaseResource(final IrodsFileSystemResourceFactory factory,
			final IRODSAccessObjectFactory irodsAccessObjectFactory, final WebDavConfig webDavConfig,
			final IrodsFileContentService contentService) {
		super();

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (webDavConfig == null) {
			throw new IllegalArgumentException("null webDavConfig");
		}

		if (factory == null) {
			throw new IllegalArgumentException("null factory");
		}

		if (factory.getLockManager() == null) {
			throw new ConfigurationRuntimeException("no lock manager configured!");
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
	protected void setIrodsAccessObjectFactory(final IRODSAccessObjectFactory irodsAccessObjectFactory) {
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
	protected void setWebDavConfig(final WebDavConfig webDavConfig) {
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
	protected void setContentService(final IrodsFileContentService contentService) {
		this.contentService = contentService;
	}

	protected IRODSFile fileFromCollectionResource(final CollectionResource collectionResource, final String name) {

		log.info("fileFromCollectionResource()");

		if (collectionResource == null) {
			throw new IllegalArgumentException("null collectionResource");
		}

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}

		IRODSFile dest;

		if (collectionResource instanceof IrodsDirectoryResource) {
			log.info("is a directory resource");
			try {
				BaseResource newFsParent = (BaseResource) collectionResource;

				dest = instanceIrodsFileFactory().instanceIRODSFile(newFsParent.getIrodsFile().getAbsolutePath(), name);
				return dest;
			} catch (JargonException e) {
				log.error("jargon exception on copy", e);
				throw new WebDavRuntimeException("exception in copy", e);
			}

		} else {
			log.error("unknown destination type for:{}", collectionResource);
			throw new WebDavRuntimeException("Destination is an unknown type. Must be a FsDirectoryResource");
		}

	}

	/**
	 * Will redirect if a default page has been specified on the factory
	 *
	 * @param request
	 * @return
	 */
	@Override
	public String checkRedirect(final Request request) {
		if (getFactory().getDefaultPage() != null) {
			return request.getAbsoluteUrl() + "/" + getFactory().getDefaultPage();
		} else {
			return null;
		}
	}

	@Override
	public void copyTo(final CollectionResource destinationPath, final String newName)
			throws NotAuthorizedException, BadRequestException, ConflictException {

		log.info("copyTo()");
		if (destinationPath == null) {
			throw new IllegalArgumentException("null destinationPath");
		}

		if (newName == null || newName.isEmpty()) {
			throw new IllegalArgumentException("null or empty newName");
		}

		IRODSFile dest = fileFromCollectionResource(destinationPath, newName);
		doCopy(dest);

	}

	/**
	 * Accomplish a copy with the given file
	 *
	 * @param dest
	 */
	protected abstract void doCopy(IRODSFile dest);

	@Override
	public Object authenticate(final String user, final String password) {
		return factory.getSecurityManager().authenticate(user, password);
	}

	@Override
	public boolean authorise(final Request request, final Method method, final Auth auth) {
		boolean b = factory.getSecurityManager().authorise(request, method, auth, this);
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
		String r = factory.getRealm(getWebDavConfig().getHost());
		if (r == null) {
			throw new NullPointerException(
					"Got null realm from: " + factory.getClass() + " for host=" + getWebDavConfig().getHost());
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
	public void setSsoPrefix(final String ssoPrefix) {
		this.ssoPrefix = ssoPrefix;
	}

	/**
	 * @return the irodsFile
	 */
	public IRODSFile getIrodsFile() {
		return irodsFile;
	}

	/**
	 * @param irodsFile
	 *            the irodsFile to set
	 */
	public void setIrodsFile(final IRODSFile irodsFile) {
		this.irodsFile = irodsFile;
	}

	protected Map<Principal, List<Priviledge>> irodsPermissionsToDavPermissions(
			List<UserFilePermission> userFilePermissions) throws JargonException {
		Priviledge priviledge;
		Map<Principal, List<Priviledge>> principalMap = new HashMap<Principal, List<Priviledge>>();
		List<Priviledge> priviledges = new ArrayList<Priviledge>();
		Principal principal;
		for (UserFilePermission permission : userFilePermissions) {
			IRODSAccount accountForPrincipal = IrodsPrincipalId.cloneAccountForUser(retrieveIrodsAccount(),
					permission.getNameWithZone(), "");
			accountForPrincipal.setHomeDirectory("");
			principal = new IrodsPrincipal(accountForPrincipal.toURI(false).toASCIIString());
			priviledges.add(irodsPrivToWebdavPriv(permission));
			principalMap.put(principal, priviledges);
		}
		return principalMap;
	}

	protected Priviledge irodsPrivToWebdavPriv(UserFilePermission permission) {
		if (permission == null) {
			throw new IllegalArgumentException("null permission");
		}
		Priviledge priv;
		log.info("getting priv for permission:{}", permission);

		switch (permission.getFilePermissionEnum()) {

		case READ:
			priv = AccessControlledResource.Priviledge.READ;
			break;
		case WRITE:
			priv = AccessControlledResource.Priviledge.WRITE_CONTENT;
			break;
		case OWN:
			priv = AccessControlledResource.Priviledge.ALL;
			break;
		default:
			throw new WebDavRuntimeException("cannot find equivalent dav priv for irods priv");
		}

		return priv;
	}

	protected String retriveOwnerAndGetPrincipal(
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
		String ownerName;
		if (collectionAndDataObjectListingEntry != null) {
			ownerName = collectionAndDataObjectListingEntry.getOwnerName();
		} else {
			log.info("using objStat to find owner, note that this is an extra irods call");
			ObjStat objStat;
			try {
				objStat = this.getIrodsAccessObjectFactory()
						.getCollectionAndDataObjectListAndSearchAO(this.retrieveIrodsAccount())
						.retrieveObjectStatForPath(this.getIrodsFile().getAbsolutePath());
			} catch (JargonException e) {
				log.error("error getting objStat for:{}", this.getIrodsFile());
				throw new WebDavRuntimeException("error getting objStat", e);
			}
			ownerName = objStat.getOwnerName();
		}

		try {
			return IrodsPrincipalId.cloneAccountForUser(this.retrieveIrodsAccount(), ownerName, "").toURI(false)
					.toASCIIString();
		} catch (JargonException e) {
			log.error("error getting principal", e);
			throw new WebDavRuntimeException("error getting principal", e);
		}
	}

}
