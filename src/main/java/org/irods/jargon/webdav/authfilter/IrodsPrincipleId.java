/**
 *
 */
package org.irods.jargon.webdav.authfilter;

import io.milton.principal.Principal.PrincipleId;

import javax.xml.namespace.QName;

/**
 * Wrapper for principle
 *
 * @author Mike Conway - DICE
 *
 */
public class IrodsPrincipleId implements PrincipleId {

	public IrodsPrincipleId(final String irodsAccountAsString) {
		super();
		this.irodsAccountAsString = irodsAccountAsString;
	}

	private String irodsAccountAsString = "";

	/*
	 * (non-Javadoc)
	 *
	 * @see io.milton.principal.Principal.PrincipleId#getIdType()
	 */
	@Override
	public QName getIdType() {

		return new QName("irods");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.milton.principal.Principal.PrincipleId#getValue()
	 */
	@Override
	public String getValue() {
		return irodsAccountAsString;
	}

	/**
	 * @return the irodsAccountAsString
	 */
	public String getIrodsAccountAsString() {
		return irodsAccountAsString;
	}

	/**
	 * @param irodsAccountAsString
	 *            the irodsAccountAsString to set
	 */
	public void setIrodsAccountAsString(final String irodsAccountAsString) {
		this.irodsAccountAsString = irodsAccountAsString;
	}

}
