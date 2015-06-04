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
import org.irods.jargon.core.utils.MiscIRODSUtils;
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
	 */
	public IrodsFileSystemResourceFactory() {
		log.debug("setting default configuration...");
		io.milton.http.SecurityManager sm = new NullSecurityManager();
		init(sm);
	}

	protected void init(final io.milton.http.SecurityManager securityManager) {
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
	 *
	 * @param securityManager
	 */
	public IrodsFileSystemResourceFactory(
			final io.milton.http.SecurityManager securityManager) {
		setSecurityManager(securityManager);
		init(securityManager);
	}

	/**
	 *
	 *
	 * @param securityManager
	 * @param contextPath
	 *            - this is the leading part of URL's to ignore. For example if
	 *            you're application is deployed to
	 *            http://localhost:8080/webdav-fs, the context path should be
	 *            webdav-fs
	 */
	public IrodsFileSystemResourceFactory(
			final io.milton.http.SecurityManager securityManager,
			final String contextPath) {
		setSecurityManager(securityManager);
		setContextPath(contextPath);
		init(securityManager);
	}

	@Override
	public Resource getResource(final String host, String url) {
		log.debug("getResource: host: " + host + " - url:" + url);
		url = stripContext(url);
		IRODSFile requested = resolvePath(url);
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

	/**
	 * Find the right base path to use based on the provided configuration
	 *
	 * @return
	 */
	protected String getBasePathBasedOnConfig() {
		log.info("getBasePathBasedOnConfig()");
		if (webDavConfig == null) {
			throw new WebDavRuntimeException("no webDavConfig is present");
		}

		switch (webDavConfig.getDefaultStartingLocationEnum()) {

		case ROOT:
			return "/";
		case USER_HOME:
			return MiscIRODSUtils
					.buildIRODSUserHomeForAccountUsingDefaultScheme(IrodsAuthService
							.retrieveCurrentIrodsAccount());
		case PROVIDED:
			return webDavConfig.getProvidedDefaultStartingLocation();

		default:
			throw new WebDavRuntimeException("unknown configured base path");

		}

	}

	public IRODSFile resolvePath(final String pathToResolve) {
		log.info("resolvePath()");

		if (pathToResolve == null || pathToResolve.isEmpty()) {
			throw new IllegalArgumentException("null or empty url");
		}

		log.info("url:{}", pathToResolve);

		try {
			IRODSFile f = getIrodsAccessObjectFactory().getIRODSFileFactory(
					IrodsAuthService.retrieveCurrentIrodsAccount())
					.instanceIRODSFile(this.getBasePathBasedOnConfig());

			/*
			 * the path will have any existing prefix trimmed off when requested
			 * by the client, as the root was set elsewhere, and all paths are
			 * expected to be under that prefix.
			 *
			 * So if my webDavConfig is set to base on user home, the
			 * pathToResolve may be /zone/home/user/subdir/blah, and I want to
			 * Just access /subdir/blah
			 */
			Path path;
			String prefix = getBasePathBasedOnConfig();
			if (pathToResolve.equals(prefix)) {
				// path is same as prefix, so leave f unchanged
			} else {
				if (pathToResolve.startsWith(prefix)) {

					path = Path.path(MiscIRODSUtils
							.subtractPrefixFromGivenPath(f.getAbsolutePath(),
									pathToResolve));
				} else {
					path = Path.path(pathToResolve);
				}

				for (String s : path.getParts()) {
					f = getIrodsAccessObjectFactory().getIRODSFileFactory(
							IrodsAuthService.retrieveCurrentIrodsAccount())
							.instanceIRODSFile(f.getAbsolutePath(), s);
				}
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
	 *            if (webDavConfig.getDefaultStartingLocationEnum() != )
	 *
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

}