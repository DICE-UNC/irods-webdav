/**
 * General exception concerning IrodsAccountCacheManager errors 
 */
package org.irods.jargon.webdav.authfilter;

public class IrodsAccountCacheManagerError extends Exception {

    public IrodsAccountCacheManagerError(String message) {
        super(message);
    }

    public IrodsAccountCacheManagerError(String message, Throwable throwable) {
        super(message, throwable);
    }
}
