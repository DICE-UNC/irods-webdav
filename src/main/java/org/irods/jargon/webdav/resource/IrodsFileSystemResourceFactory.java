package org.irods.jargon.webdav.resource;

import io.milton.common.Path;
import io.milton.http.LockManager;
import io.milton.http.ResourceFactory;
import io.milton.http.fs.FsResource;
import io.milton.http.fs.NullSecurityManager;
import io.milton.resource.Resource;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSFileSystemSingletonWrapper;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.webdav.authfilter.IrodsAuthService;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource factory which provides access to files in a file system.
 *
 * Using this with milton is equivalent to using the dav servlet in tomcat
 *
 */
public final class IrodsFileSystemResourceFactory implements ResourceFactory {

	private static final Logger log = LoggerFactory
			.getLogger(IrodsFileSystemResourceFactory.class);
	private IrodsFileContentService irodsFileContentService;
	String root;
	io.milton.http.SecurityManager securityManager;
	LockManager lockManager;
	Long maxAgeSeconds;
	String contextPath;
	boolean allowDirectoryBrowsing;
	String defaultPage;
	boolean digestAllowed = true;
	private String ssoPrefix;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private IRODSFileSystem irodsFileSystem;
	private WebDavConfig webDavConfig;

	/**
	 * Creates and (optionally) initialises the factory. This looks for a
	 * properties file FileSystemResourceFactory.properties in the classpath If
	 * one is found it uses the root and realm properties to initialise
	 *
	 * If not found the factory is initialised with the defaults root: user.home
	 * system property realm: milton-fs-test
	 *
	 * These initialised values are not final, and may be changed through the
	 * setters or init method
	 *
	 * To be honest its pretty naf configuring like this, but i don't want to
	 * force people to use spring or any other particular configuration tool
	 *
	 */
	public IrodsFileSystemResourceFactory() {
		log.debug("setting default configuration...");
		String sRoot = "/";
		io.milton.http.SecurityManager sm = new NullSecurityManager();
		init(sRoot, sm);
	}

	protected void init(final String sRoot,
			final io.milton.http.SecurityManager securityManager) {
		root = sRoot;
		setSecurityManager(securityManager);
		irodsFileSystem = IRODSFileSystemSingletonWrapper.instance();
		try {
			irodsAccessObjectFactory = irodsFileSystem
					.getIRODSAccessObjectFactory();
		} catch (JargonException e) {
			log.error("error init() irodsAccessObjectFactory", e);
			throw new WebDavRuntimeException(
					"cannot create irodsAccessObjectFactory", e);
		}
	}

	/**
	 *
	 * @param root
	 *            - the root folder of the filesystem to expose. This must
	 *            include the context path. Eg, if you've deployed to webdav-fs,
	 *            root must contain a folder called webdav-fs
	 * @param securityManager
	 */
	public IrodsFileSystemResourceFactory(final String root,
			final io.milton.http.SecurityManager securityManager) {
		this.root = root;
		setSecurityManager(securityManager);
		init(root, securityManager);
	}

	/**
	 *
	 * @param root
	 *            - the root folder of the filesystem to expose
	 * @param securityManager
	 * @param contextPath
	 *            - this is the leading part of URL's to ignore. For example if
	 *            you're application is deployed to
	 *            http://localhost:8080/webdav-fs, the context path should be
	 *            webdav-fs
	 */
	public IrodsFileSystemResourceFactory(final String root,
			final io.milton.http.SecurityManager securityManager,
			final String contextPath) {
		this.root = root;
		setSecurityManager(securityManager);
		setContextPath(contextPath);
		init(root, securityManager);
	}

	@Override
	public Resource getResource(final String host, String url) {
		log.debug("getResource: host: " + host + " - url:" + url);
		url = stripContext(url);
		IRODSFile requested = resolvePath(root, url);
		BaseResource resolvedResource = resolveFile(host, requested);
		log.info("resolved as resource:{}", resolvedResource);
		return resolvedResource;
	}

	public BaseResource resolveFile(final String host, final IRODSFile file) {
		log.info("resolveFile()");
		log.info("host:{}", host);
		log.info("file:{}", file);
		BaseResource r;
		if (!file.exists()) {
			log.info("file not found, will return shell iRODS file: {}",
					file.getAbsolutePath());
			return null;
		} else if (file.isDirectory()) {
			log.info("file is a dir");
			r = new IrodsDirectoryResource(host, this, file,
					irodsFileContentService);
		} else {
			log.info("file is a data object");
			r = new IrodsFileResource(host, this, file, irodsFileContentService);
		}
		if (r != null) {
			r.setSsoPrefix(ssoPrefix);
		}
		return r;
	}

	public IRODSFile resolvePath(final String root, final String url) {
		log.info("resolvePath()");

		if (root == null || root.isEmpty()) {
			throw new IllegalArgumentException("null or empty root");
		}

		/*
		 * if (url == null || url.isEmpty()) { throw new
		 * IllegalArgumentException("null or empty url"); }
		 */

		log.info("root:{}", root);
		log.info("url:{}", url);

		Path path = Path.path(url);

		try {
			IRODSFile f = getIrodsAccessObjectFactory().getIRODSFileFactory(
					IrodsAuthService.retrieveCurrentIrodsAccount())
							.instanceIRODSFile(root);

			for (String s : path.getParts()) {
				f = getIrodsAccessObjectFactory().getIRODSFileFactory(
						IrodsAuthService.retrieveCurrentIrodsAccount())
								.instanceIRODSFile(f.getAbsolutePath(), s);
			}
			log.info("resolved as:{}", f);
			return f;
		} catch (JargonException e) {
			log.error("jargon exception resolving file path to an irods file",
					e);
			throw new WebDavRuntimeException("exception resolving path", e);
		}
	}

	public String getRealm(final String host) {
		String s = securityManager.getRealm(host);
		if (s == null) {
			throw new NullPointerException(
					"Got null realm from securityManager: " + securityManager
					+ " for host=" + host);
		}
		return s;
	}

	/**
	 *
	 * @return - the caching time for files
	 */
	public Long maxAgeSeconds(final FsResource resource) {
		return maxAgeSeconds;
	}

	public void setSecurityManager(
			final io.milton.http.SecurityManager securityManager) {
		if (securityManager != null) {
			log.debug("securityManager: " + securityManager.getClass());
		} else {
			log.warn("Setting null FsSecurityManager. This WILL cause null pointer exceptions");
		}
		this.securityManager = securityManager;
	}

	public io.milton.http.SecurityManager getSecurityManager() {
		return securityManager;
	}

	public void setMaxAgeSeconds(final Long maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	public Long getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public void setLockManager(final LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public void setContextPath(final String contextPath) {
		this.contextPath = contextPath;
	}

	public String getContextPath() {
		return contextPath;
	}

	/**
	 * Whether to generate an index page.
	 *
	 * @return
	 */
	public boolean isAllowDirectoryBrowsing() {
		return allowDirectoryBrowsing;
	}

	public void setAllowDirectoryBrowsing(final boolean allowDirectoryBrowsing) {
		this.allowDirectoryBrowsing = allowDirectoryBrowsing;
	}

	/**
	 * if provided GET requests to a folder will redirect to a page of this name
	 * within the folder
	 *
	 * @return - E.g. index.html
	 */
	public String getDefaultPage() {
		return defaultPage;
	}

	public void setDefaultPage(final String defaultPage) {
		this.defaultPage = defaultPage;
	}

	private String stripContext(String url) {
		if (contextPath != null && contextPath.length() > 0) {
			url = url.replaceFirst('/' + contextPath, "");
			log.debug("stripped context: " + url);
			return url;
		} else {
			return url;
		}
	}

	boolean isDigestAllowed() {
		boolean b = digestAllowed && securityManager != null
				&& securityManager.isDigestAllowed();
		if (log.isTraceEnabled()) {
			log.trace("isDigestAllowed: " + b);
		}
		return b;
	}

	public void setDigestAllowed(final boolean digestAllowed) {
		this.digestAllowed = digestAllowed;
	}

	public void setSsoPrefix(final String ssoPrefix) {
		this.ssoPrefix = ssoPrefix;
	}

	public String getSsoPrefix() {
		return ssoPrefix;
	}

	public IrodsFileContentService getContentService() {
		return irodsFileContentService;
	}

	public void setContentService(final IrodsFileContentService contentService) {
		irodsFileContentService = contentService;
	}

	/**
	 * @return the irodsFileContentService
	 */
	public IrodsFileContentService getIrodsFileContentService() {
		return irodsFileContentService;
	}

	/**
	 * @param irodsFileContentService
	 *            the irodsFileContentService to set
	 */
	public void setIrodsFileContentService(
			final IrodsFileContentService irodsFileContentService) {
		this.irodsFileContentService = irodsFileContentService;
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
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}
}