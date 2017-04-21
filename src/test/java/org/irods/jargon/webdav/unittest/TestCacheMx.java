/**
 * 
 */
package org.irods.jargon.webdav.unittest;

import org.irods.jargon.core.connection.auth.AuthResponse;

/**
 * Thread local auth cache mx methods for testing
 * 
 * @author mcc
 *
 */
public class TestCacheMx {

	private static ThreadLocal<AuthResponse> authResponseCache = new ThreadLocal<AuthResponse>();

	/**
	 * 
	 */
	public TestCacheMx() {
	}

	public static void clearCache() {
		authResponseCache.set(null);
	}

}
