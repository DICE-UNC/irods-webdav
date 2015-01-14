/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.irods.jargon.webdav.resource;

import io.milton.http.LockInfo;
import io.milton.http.LockManager;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.fs.FsResource;
import io.milton.resource.LockableResource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.irods.jargon.core.pub.io.IRODSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class IrodsMemoryLockManager implements LockManager {

	private static final Logger log = LoggerFactory
			.getLogger(IrodsMemoryLockManager.class);
	/**
	 * maps current locks by the file associated with the resource
	 */
	Map<IRODSFile, CurrentLock> locksByFile;
	Map<String, CurrentLock> locksByToken;

	public IrodsMemoryLockManager() {
		locksByFile = new HashMap<IRODSFile, CurrentLock>();
		locksByToken = new HashMap<String, CurrentLock>();
	}

	@Override
	public synchronized LockResult lock(LockTimeout timeout, LockInfo lockInfo,
			LockableResource r) {
		IrodsFileResource resource = (IrodsFileResource) r;
		LockToken currentLock = currentLock(resource);
		if (currentLock != null) {
			return LockResult.failed(LockResult.FailureReason.ALREADY_LOCKED);
		}

		LockToken newToken = new LockToken(UUID.randomUUID().toString(),
				lockInfo, timeout);
		CurrentLock newLock = new CurrentLock(resource.getFile(), newToken,
				lockInfo.lockedByUser);
		locksByFile.put(resource.getFile(), newLock);
		locksByToken.put(newToken.tokenId, newLock);
		return LockResult.success(newToken);
	}

	@Override
	public synchronized LockResult refresh(String tokenId,
			LockableResource resource) {
		CurrentLock curLock = locksByToken.get(tokenId);
		if (curLock == null) {
			log.debug("can't refresh because no lock");
			return LockResult
					.failed(LockResult.FailureReason.PRECONDITION_FAILED);
		} else {
			curLock.token.setFrom(new Date());
			return LockResult.success(curLock.token);
		}
	}

	@Override
	public synchronized void unlock(String tokenId, LockableResource r)
			throws NotAuthorizedException {
		IrodsFileResource resource = (IrodsFileResource) r;
		LockToken lockToken = currentLock(resource);
		if (lockToken == null) {
			log.debug("not locked");
			return;
		}
		if (lockToken.tokenId.equals(tokenId)) {
			removeLock(lockToken);
		} else {
			throw new NotAuthorizedException(resource);
		}
	}

	private LockToken currentLock(IrodsFileResource resource) {
		CurrentLock curLock = locksByFile.get(resource.getFile());
		if (curLock == null)
			return null;
		LockToken token = curLock.token;
		if (token.isExpired()) {
			removeLock(token);
			return null;
		} else {
			return token;
		}
	}

	private void removeLock(LockToken token) {
		log.debug("removeLock: " + token.tokenId);
		CurrentLock currentLock = locksByToken.get(token.tokenId);
		if (currentLock != null) {
			locksByFile.remove(currentLock.file);
			locksByToken.remove(currentLock.token.tokenId);
		} else {
			log.warn("couldnt find lock: " + token.tokenId);
		}
	}

	@Override
	public LockToken getCurrentToken(LockableResource r) {
		FsResource resource = (FsResource) r;
		CurrentLock lock = locksByFile.get(resource.getFile());
		if (lock == null)
			return null;
		LockToken token = new LockToken();
		token.info = new LockInfo(LockInfo.LockScope.EXCLUSIVE,
				LockInfo.LockType.WRITE, lock.lockedByUser,
				LockInfo.LockDepth.ZERO);
		token.info.lockedByUser = lock.lockedByUser;
		token.timeout = lock.token.timeout;
		token.tokenId = lock.token.tokenId;
		return token;
	}

	class CurrentLock {

		final IRODSFile file;
		final LockToken token;
		final String lockedByUser;

		public CurrentLock(IRODSFile irodsFile, LockToken token,
				String lockedByUser) {
			this.file = irodsFile;
			this.token = token;
			this.lockedByUser = lockedByUser;
		}
	}
}
