/**
 *
 */
package org.irods.jargon.webdav.config;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy;

/**
 * @author Mike Conway - DICE
 * 
 *         Config object wired by Spring
 *
 */
public class WebDavConfig {

	private String host = "";
	private String zone = "";
	private int port = 0;
	private String defaultStorageResource = "";
	private String authScheme = AuthScheme.STANDARD.getTextValue();
	private String realm = "irods";
	/**
	 * sets ssl negotiation policy in jargon
	 */
	private String sslNegotiationPolicy = ClientServerNegotiationPolicy.SslNegotiationPolicy.CS_NEG_DONT_CARE
			.toString();

	/**
	 * requests, if true, that a checksum be computed on upload
	 */
	private boolean computeChecksum = false;

	/**
	 * Maximum upload size in Gb
	 */
	private long maxUploadInGb = 5;

	/**
	 * Maximum download size in Gb
	 */
	private long maxDownloadInGb = 5;

	/**
	 * Use an optimization to cache file data (length, etc) preventing requery
	 * of file data, may cause stale data issues
	 */
	private boolean cacheFileDemographics = false;
	/**
	 * Default positioning of webdav on login with a pure URL
	 */
	private DefaultStartingLocationEnum defaultStartingLocationEnum = DefaultStartingLocationEnum.USER_HOME;

	/**
	 * Absolute path to starting location on login with a pure URL, only
	 * activated if <code>DefaultStatingLocationEnum</code> is set to
	 * <code>PROVIDED</code>
	 */
	private String providedDefaultStartingLocation = "";

	/**
	 * Use the jargon packing io streams to read ahead and write behind at
	 * larger buffer sizes for better performance
	 */
	private boolean usePackingStreams = false;

	/**
	 *
	 */
	public WebDavConfig() {
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(final String host) {
		this.host = host;
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @param zone
	 *            the zone to set
	 */
	public void setZone(final String zone) {
		this.zone = zone;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * @return the defaultStorageResource
	 */
	public String getDefaultStorageResource() {
		return defaultStorageResource;
	}

	/**
	 * @param defaultStorageResource
	 *            the defaultStorageResource to set
	 */
	public void setDefaultStorageResource(final String defaultStorageResource) {
		this.defaultStorageResource = defaultStorageResource;
	}

	/**
	 * @return the authScheme
	 */
	public String getAuthScheme() {
		return authScheme;
	}

	/**
	 * @param authScheme
	 *            the authScheme to set
	 */
	public void setAuthScheme(final String authScheme) {
		this.authScheme = authScheme;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WebDavConfig [");
		if (host != null) {
			builder.append("host=").append(host).append(", ");
		}
		if (zone != null) {
			builder.append("zone=").append(zone).append(", ");
		}
		builder.append("port=").append(port).append(", ");
		if (defaultStorageResource != null) {
			builder.append("defaultStorageResource=")
					.append(defaultStorageResource).append(", ");
		}
		if (authScheme != null) {
			builder.append("authScheme=").append(authScheme).append(", ");
		}
		if (realm != null) {
			builder.append("realm=").append(realm).append(", ");
		}
		if (sslNegotiationPolicy != null) {
			builder.append("sslNegotiationPolicy=")
					.append(sslNegotiationPolicy).append(", ");
		}
		builder.append("computeChecksum=").append(computeChecksum)
				.append(", maxUploadInGb=").append(maxUploadInGb)
				.append(", maxDownloadInGb=").append(maxDownloadInGb)
				.append(", cacheFileDemographics=")
				.append(cacheFileDemographics).append(", ");
		if (defaultStartingLocationEnum != null) {
			builder.append("defaultStartingLocationEnum=")
					.append(defaultStartingLocationEnum).append(", ");
		}
		if (providedDefaultStartingLocation != null) {
			builder.append("providedDefaultStartingLocation=")
					.append(providedDefaultStartingLocation).append(", ");
		}
		builder.append("usePackingStreams=").append(usePackingStreams)
				.append("]");
		return builder.toString();
	}

	/**
	 * @return the realm
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * @param realm
	 *            the realm to set
	 */
	public void setRealm(final String realm) {
		this.realm = realm;
	}

	/**
	 * @return the defaultStartingLocationEnum
	 */
	public DefaultStartingLocationEnum getDefaultStartingLocationEnum() {
		return defaultStartingLocationEnum;
	}

	/**
	 * @param defaultStartingLocationEnum
	 *            the defaultStartingLocationEnum to set
	 */
	public void setDefaultStartingLocationEnum(
			final DefaultStartingLocationEnum defaultStartingLocationEnum) {
		this.defaultStartingLocationEnum = defaultStartingLocationEnum;
	}

	/**
	 * @return the providedDefaultStartingLocation
	 */
	public String getProvidedDefaultStartingLocation() {
		return providedDefaultStartingLocation;
	}

	/**
	 * @param providedDefaultStartingLocation
	 *            the providedDefaultStartingLocation to set
	 */
	public void setProvidedDefaultStartingLocation(
			final String providedDefaultStartingLocation) {
		this.providedDefaultStartingLocation = providedDefaultStartingLocation;
	}

	/**
	 * @return the cacheFileDemographics
	 */
	public boolean isCacheFileDemographics() {
		return cacheFileDemographics;
	}

	/**
	 * @param cacheFileDemographics
	 *            the cacheFileDemographics to set
	 */
	public void setCacheFileDemographics(final boolean cacheFileDemographics) {
		this.cacheFileDemographics = cacheFileDemographics;
	}

	/**
	 * @return the usePackingStreams
	 */
	public boolean isUsePackingStreams() {
		return usePackingStreams;
	}

	/**
	 * @param usePackingStreams
	 *            the usePackingStreams to set
	 */
	public void setUsePackingStreams(final boolean usePackingStreams) {
		this.usePackingStreams = usePackingStreams;
	}

	/**
	 * @return the maxUploadInGb
	 */
	public long getMaxUploadInGb() {
		return maxUploadInGb;
	}

	/**
	 * @param maxUploadInGb
	 *            the maxUploadInGb to set
	 */
	public void setMaxUploadInGb(final long maxUploadInGb) {
		this.maxUploadInGb = maxUploadInGb;
	}

	/**
	 * @return the maxDownloadInGb
	 */
	public long getMaxDownloadInGb() {
		return maxDownloadInGb;
	}

	/**
	 * @param maxDownloadInGb
	 *            the maxDownloadInGb to set
	 */
	public void setMaxDownloadInGb(final long maxDownloadInGb) {
		this.maxDownloadInGb = maxDownloadInGb;
	}

	/**
	 * @return the sslNegotiationPolicy
	 */
	public String getSslNegotiationPolicy() {
		return sslNegotiationPolicy;
	}

	/**
	 * @param sslNegotiationPolicy
	 *            the sslNegotiationPolicy to set
	 */
	public void setSslNegotiationPolicy(String sslNegotiationPolicy) {
		this.sslNegotiationPolicy = sslNegotiationPolicy;
	}

	/**
	 * @return the computeChecksum
	 */
	public boolean isComputeChecksum() {
		return computeChecksum;
	}

	/**
	 * @param computeChecksum
	 *            the computeChecksum to set
	 */
	public void setComputeChecksum(boolean computeChecksum) {
		this.computeChecksum = computeChecksum;
	}

}
