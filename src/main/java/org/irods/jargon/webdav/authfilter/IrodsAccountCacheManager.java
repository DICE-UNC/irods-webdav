/**
 * The class for managing authenticated IRODSAccount's cached in Java Cache System (JCS) 
 */
package org.irods.jargon.webdav.authfilter;

import java.util.Arrays;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.irods.jargon.core.connection.IRODSAccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrodsAccountCacheManager {

    private static IrodsAccountCacheManager instance;
    private static CacheAccess<String, IRODSAccount> irodsAccountCache;
	private static final Logger log = LoggerFactory.getLogger(IrodsAccountCacheManager.class);

    private IrodsAccountCacheManager() throws IrodsAccountCacheManagerError  {

        try {
            irodsAccountCache = JCS.getInstance("irods_account");
        } catch (Exception e) {
            throw new IrodsAccountCacheManagerError("cache initialization failed.", e);
        }
    }

    public static IrodsAccountCacheManager getInstance() throws IrodsAccountCacheManagerError  {

        synchronized (IrodsAccountCacheManager.class) {

            if (instance == null) {
                instance = new IrodsAccountCacheManager();
            }
        }

        return instance;
    }

    public IRODSAccount getIRODSAccount(String username, String password)
        throws IrodsAccountCacheManagerError  {
        return getIRODSAccount(getAuthId(username,password));
    }

    public IRODSAccount getIRODSAccount(String authId)
        throws IrodsAccountCacheManagerError  {
        return irodsAccountCache.get("IRODSAccount" + authId);
    }

    public IRODSAccount putIRODSAccount(String username, String password, IRODSAccount account)
        throws IrodsAccountCacheManagerError  {

        try {
            irodsAccountCache.put("IRODSAccount" + getAuthId(username,password), account);
        } catch (Exception e) {
            throw new IrodsAccountCacheManagerError("cache put failed.", e);
        }

        // return account from cache
        return getIRODSAccount(username, password);
    }

    public static String getAuthId(String username, String password)
        throws IrodsAccountCacheManagerError  {

        String authId = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String data = username + ":" + password;
            md.update(data.getBytes("UTF-8"));
            authId = new BigInteger(1, md.digest()).toString(16);
            log.debug("cache authId: {}", authId);
        } catch (Exception e) {
            throw new IrodsAccountCacheManagerError("cache authId generation failed.", e);
        }

        return authId;
    }
}
