/**
 * 
 */
package org.irods.jargon.webdav.authfilter;

import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.principal.DiscretePrincipal;

import java.util.Date;

import org.irods.jargon.core.connection.auth.AuthResponse;

/**
 * Wrapper for auth
 * 
 * @author Mike Conway - DICE
 *
 */
public class AuthWrapper implements DiscretePrincipal {

	private AuthResponse authResponse;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.principal.Principal#getIdenitifer()
	 */
	@Override
	public PrincipleId getIdenitifer() {
		return new IrodsPrincipleId(authResponse
				.getAuthenticatingIRODSAccount().toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#authenticate(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Object authenticate(String arg0, String arg1) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#authorise(io.milton.http.Request,
	 * io.milton.http.Request.Method, io.milton.http.Auth)
	 */
	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#checkRedirect(io.milton.http.Request)
	 */
	@Override
	public String checkRedirect(Request arg0) throws NotAuthorizedException,
			BadRequestException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#getModifiedDate()
	 */
	@Override
	public Date getModifiedDate() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#getName()
	 */
	@Override
	public String getName() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#getRealm()
	 */
	@Override
	public String getRealm() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.resource.Resource#getUniqueId()
	 */
	@Override
	public String getUniqueId() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.principal.DiscretePrincipal#getPrincipalURL()
	 */
	@Override
	public String getPrincipalURL() {
		return null;
	}

	/**
	 * @return the authResponse
	 */
	public AuthResponse getAuthResponse() {
		return authResponse;
	}

	/**
	 * @param authResponse
	 *            the authResponse to set
	 */
	public void setAuthResponse(AuthResponse authResponse) {
		this.authResponse = authResponse;
	}

}
