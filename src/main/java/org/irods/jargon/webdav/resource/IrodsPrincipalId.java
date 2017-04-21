/**
 * 
 */
package org.irods.jargon.webdav.resource;

import javax.xml.namespace.QName;

import org.irods.jargon.core.connection.IRODSAccount;

import io.milton.principal.Principal.PrincipleId;

/**
 * @author mcc
 *
 */
public class IrodsPrincipalId implements PrincipleId {

	private final String userName;

	/**
	 * 
	 */
	public IrodsPrincipalId(final String userName) {

		this.userName = userName;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.principal.Principal.PrincipleId#getIdType()
	 */
	@Override
	public QName getIdType() {
		return new QName("D:href");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.milton.principal.Principal.PrincipleId#getValue()
	 */
	@Override
	public String getValue() {
		return userName;
	}

	public static IRODSAccount cloneAccountForUser(final IRODSAccount irodsAccount, final String userName,
			final String password) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}
		if (password == null) {
			throw new IllegalArgumentException("null password");
		}

		return new IRODSAccount(irodsAccount.getHost(), irodsAccount.getPort(), userName, password, "",
				irodsAccount.getZone(), irodsAccount.getDefaultStorageResource());

	}

}
