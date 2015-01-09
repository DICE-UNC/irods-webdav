package org.irods.jargon.webdav.resource;

import io.milton.common.Path;
import io.milton.http.LockManager;
import io.milton.http.ResourceFactory;
import io.milton.http.fs.FsDirectoryResource;
import io.milton.http.fs.FsFileResource;
import io.milton.http.fs.FsResource;
import io.milton.http.fs.NullSecurityManager;
import io.milton.resource.Resource;

import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.webdav.config.IrodsAuthService;
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
	private WebDavConfig webDavConfig;
	private static final ThreadLocal<AuthResponse> authResponseCache = new ThreadLocal<AuthResponse>();

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

	protected void init(String sRoot,
			io.milton.http.SecurityManager securityManager) {
		this.root = sRoot;
		setSecurityManager(securityManager);
	}

	/**
	 * 
	 * @param root
	 *            - the root folder of the filesystem to expose. This must
	 *            include the context path. Eg, if you've deployed to webdav-fs,
	 *            root must contain a folder called webdav-fs
	 * @param securityManager
	 */
	public IrodsFileSystemResourceFactory(String root,
			io.milton.http.SecurityManager securityManager) {
		this.root = root;
		setSecurityManager(securityManager);
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
	public IrodsFileSystemResourceFactory(String root,
			io.milton.http.SecurityManager securityManager, String contextPath) {
		this.root = root;
		setSecurityManager(securityManager);
		setContextPath(contextPath);
	}

	@Override
	public Resource getResource(String host, String url) {
		log.debug("getResource: host: " + host + " - url:" + url);
		url = stripContext(url);
		IRODSFile requested = resolvePath(root, url);
		return resolveFile(host, requested);
	}

	public FsResource resolveFile(String host, IRODSFile file) {
		FsResource r;
		if (!file.exists()) {
			log.debug("file not found: " + file.getAbsolutePath());
			return null;
		} else if (file.isDirectory()) {
			r = new FsDirectoryResource(host, this, file,
					irodsFileContentService);
		} else {
			r = new FsFileResource(host, this, file, irodsFileContentService);
		}
		if (r != null) {
			r.ssoPrefix = ssoPrefix;
		}
		return r;
	}

	public IRODSFile resolvePath(String root, String url) {
		log.info("resolvePath()");

		if (root == null || root.isEmpty()) {
			throw new IllegalArgumentException("null or empty root");
		}

		if (url == null || url.isEmpty()) {
			throw new IllegalArgumentException("null or empty url");
		}

		log.info("root:{}", root);
		log.info("url:{}", url);

		Path path = Path.path(url);

		try {
			IRODSFile f = this
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(
							IrodsAuthService.retrieveCurrentIrodsAccount())
					.instanceIRODSFile(root);

			for (String s : path.getParts()) {
				f = this.getIrodsAccessObjectFactory()
						.getIRODSFileFactory(
								IrodsAuthService.retrieveCurrentIrodsAccount())
						.instanceIRODSFile(root, s);
			}
			log.info("resolved as:{}", f);
			return f;
		} catch (JargonException e) {
			log.error("jargon exception resolving file path to an irods file",
					e);
			throw new WebDavRuntimeException("exception resolving path", e);
		}
	}

	public String getRealm(String host) {
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
	public Long maxAgeSeconds(FsResource resource) {
		return maxAgeSeconds;
	}

	public void setSecurityManager(
			io.milton.http.SecurityManager securityManager) {
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

	public void setMaxAgeSeconds(Long maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	public Long getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public void setContextPath(String contextPath) {
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

	public void setAllowDirectoryBrowsing(boolean allowDirectoryBrowsing) {
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

	public void setDefaultPage(String defaultPage) {
		this.defaultPage = defaultPage;
	}

	private String stripContext(String url) {
		if (this.contextPath != null && contextPath.length() > 0) {
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

	public void setDigestAllowed(boolean digestAllowed) {
		this.digestAllowed = digestAllowed;
	}

	public void setSsoPrefix(String ssoPrefix) {
		this.ssoPrefix = ssoPrefix;
	}

	public String getSsoPrefix() {
		return ssoPrefix;
	}

	public IrodsFileContentService getContentService() {
		return irodsFileContentService;
	}

	public void setContentService(IrodsFileContentService contentService) {
		this.irodsFileContentService = contentService;
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
			IrodsFileContentService irodsFileContentService) {
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

	/**
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}
}