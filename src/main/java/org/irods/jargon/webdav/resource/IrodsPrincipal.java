/**
 * 
 */
package org.irods.jargon.webdav.resource;

import io.milton.principal.Principal;

/**
 * @author mcc
 *
 */
public class IrodsPrincipal implements Principal {

	private final String irodsUser;

	/**
	 * 
	 */
	public IrodsPrincipal(final String irodsUser) {
		this.irodsUser = irodsUser;
	}

	@Override
	public PrincipleId getIdenitifer() {
		return new IrodsPrincipalId(irodsUser);
	}

}
