package org.irods.jargon.webdav.resource;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.http11.auth.DigestGenerator;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.resource.DigestResource;
import io.milton.resource.PropFindableResource;

import java.util.Date;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSFileSystemSingletonWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Milton resources
 * 
 * @author Mike Conway - DICE
 *
 */
public abstract class AbstractResource implements DigestResource,
		PropFindableResource {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private IRODSFileSystem irodsFileSystem;

	public AbstractResource() {
		this.irodsFileSystem = IRODSFileSystemSingletonWrapper.instance();
	}

	@Override
	public Object authenticate(String user, String requestedPassword) {
		if (user.equals("user") && requestedPassword.equals("password")) {
			return user;
		}
		return null;

	}

	@Override
	public Object authenticate(DigestResponse digestRequest) {
		if (digestRequest.getUser().equals("user")) {
			DigestGenerator gen = new DigestGenerator();
			String actual = gen.generateDigest(digestRequest, "password");
			if (actual.equals(digestRequest.getResponseDigest())) {
				return digestRequest.getUser();
			} else {
				log.warn("that password is incorrect. Try 'password'");
			}
		} else {
			log.warn("user not found: " + digestRequest.getUser()
					+ " - try 'userA'");
		}
		return null;

	}

	@Override
	public String getUniqueId() {
		return null;
	}

	@Override
	public String checkRedirect(Request request) {
		return null;
	}

	@Override
	public boolean authorise(Request request, Method method, Auth auth) {
		log.debug("authorise");
		return auth != null;
	}

	@Override
	public String getRealm() {
		return "testrealm@host.com";
	}

	@Override
	public Date getModifiedDate() {
		return null;
	}

	@Override
	public Date getCreateDate() {
		return null;
	}

	@Override
	public boolean isDigestAllowed() {
		return true;
	}

}
