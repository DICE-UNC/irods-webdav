/**
 * 
 */
package org.irods.jargon.webdav.config;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway Wired-in class that takes configuration and core jargon
 *         components and injects appropriate configuration into the underlying
 *         jargon properties system
 *
 */
public class StartupConfigurator {

	private WebDavConfig webDavConfig;
	private IRODSSession irodsSession;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public StartupConfigurator() {

	}

	/**
	 * @return the webdavConfiguration
	 */
	public WebDavConfig getWebDavConfig() {
		return webDavConfig;
	}

	/**
	 * @param webDavConfig
	 *            the webdavConfiguration to set
	 */
	public void setWebDavConfig(WebDavConfig webDavConfig) {
		this.webDavConfig = webDavConfig;
	}

	/**
	 * @return the irodsSession
	 */
	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/**
	 * @param irodsSession
	 *            the irodsSession to set
	 */
	public void setIrodsSession(IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

	/**
	 * this method is wired into the spring config after the injection of the
	 * props and <code>IRODSSession</code> so that property configuration can be
	 * accomplished
	 */
	public void init() {
		log.info("init()");

		if (webDavConfig == null) {
			log.error("null webdavConfiguration");
			throw new IllegalStateException("null webdavConfiguration");
		}

		if (irodsSession == null) {
			log.error("null irodsSession");
			throw new IllegalStateException("null irodsSession");
		}

		log.info("configuration with:{}", webDavConfig);

		SettableJargonProperties props = new SettableJargonProperties(
				irodsSession.getJargonProperties());
		props.setComputeChecksumAfterTransfer(webDavConfig.isComputeChecksum());
		log.info("set checksum policy to:{}", webDavConfig.isComputeChecksum());

		SslNegotiationPolicy policyToSet = ClientServerNegotiationPolicy
				.findSslNegotiationPolicyFromString(webDavConfig
						.getSslNegotiationPolicy());

		log.info("policyToSet:{}", policyToSet);

		props.setNegotiationPolicy(policyToSet);
		log.info("negotiation policy set to:{}", props.getNegotiationPolicy());

		getIrodsSession().setJargonProperties(props);
		log.info("config of jargon props complete");

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StartupConfigurator [");
		if (webDavConfig != null) {
			builder.append("webdavConfiguration=").append(webDavConfig)
					.append(", ");
		}
		if (irodsSession != null) {
			builder.append("irodsSession=").append(irodsSession).append(", ");
		}
		if (irodsAccessObjectFactory != null) {
			builder.append("irodsAccessObjectFactory=")
					.append(irodsAccessObjectFactory).append(", ");
		}
		if (log != null) {
			builder.append("log=").append(log);
		}
		builder.append("]");
		return builder.toString();
	}

}
