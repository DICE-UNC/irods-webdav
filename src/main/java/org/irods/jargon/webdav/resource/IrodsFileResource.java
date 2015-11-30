package org.irods.jargon.webdav.resource;

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

import io.milton.common.ContentTypeUtils;
import io.milton.common.RangeUtils;
import io.milton.common.ReadingException;
import io.milton.common.WritingException;
import io.milton.http.Auth;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.LockableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.ReplaceableResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.webdav.exception.ConfigurationRuntimeException;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrodsFileResource extends BaseResource implements
CopyableResource, DeletableResource, GetableResource, MoveableResource,
ReplaceableResource, PropFindableResource, LockableResource {
	private static final Logger log = LoggerFactory
			.getLogger(IrodsFileResource.class);

	/**
	 * This field may be <code>null</code> and is only available when the milton
	 * config is set to cache file demographics. In this case, the entry will be
	 * used to provide file data such as length without necessitating a new
	 * query
	 */
	private final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry;

	/**
	 *
	 * @param host
	 *            - the requested host. E.g. www.mycompany.com
	 * @param factory
	 * @param file
	 */
	public IrodsFileResource(final String host,
			final IrodsFileSystemResourceFactory factory, final IRODSFile file,
			final IrodsFileContentService contentService) {
		super(factory, factory.getIrodsAccessObjectFactory(), factory
				.getWebDavConfig(), contentService);

		if (file == null) {
			throw new IllegalArgumentException("null file");
		}

		setIrodsFile(file);
		collectionAndDataObjectListingEntry = null;
	}

	/**
	 * Constructor takes a parameter that will provided cached values for
	 * collection and data object listing entries
	 *
	 * @param host
	 * @param factory
	 * @param file
	 * @param collectionAndDataObjectListingEntry
	 * @param contentService
	 */
	public IrodsFileResource(
			final String host,
			final IrodsFileSystemResourceFactory factory,
			final IRODSFile file,
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry,
			final IrodsFileContentService contentService) {

		super(factory, factory.getIrodsAccessObjectFactory(), factory
				.getWebDavConfig(), contentService);

		if (file == null) {
			throw new IllegalArgumentException("null file");
		}

		setIrodsFile(file);
		this.collectionAndDataObjectListingEntry = collectionAndDataObjectListingEntry;
	}

	@Override
	public Long getContentLength() {
		log.info("getContentLength()");

		if (collectionAndDataObjectListingEntry != null) {
			log.debug("cached length");
			return collectionAndDataObjectListingEntry.getDataSize();
		} else {
			return getIrodsFile().length();
		}
	}

	@Override
	public String getContentType(final String preferredList) {
		log.info("getContentType()");
		String mime = ContentTypeUtils.findContentTypes(getIrodsFile()
				.getName());
		String s = ContentTypeUtils.findAcceptableContentType(mime,
				preferredList);
		if (log.isTraceEnabled()) {
			log.trace("getContentType: preferred: {} mime: {} selected: {}",
					new Object[] { preferredList, mime, s });
		}
		log.info("content type:{}", s);
		return s;
	}

	@Override
	public void sendContent(final OutputStream out, final Range range,
			final Map<String, String> params, final String contentType)
					throws IOException, NotFoundException {
		log.info("sendContent()");
		InputStream in = null;
		try {
			log.debug("getting input stream...");
			in = getContentService().getFileContent(getIrodsFile(),
					retrieveIrodsAccount());
			log.debug("got input stream...");
			if (range != null) {
				log.debug("sendContent: ranged content: "
						+ getIrodsFile().getAbsolutePath());
				RangeUtils.writeRange(in, range, out);
			} else {
				log.debug("sendContent: send whole file "
						+ getIrodsFile().getAbsolutePath());
				IOUtils.copy(in, out);
			}
			out.flush();
		} catch (FileNotFoundException e) {
			throw new NotFoundException("Couldnt locate content");
		} catch (ReadingException e) {
			throw new IOException(e);
		} catch (WritingException e) {
			throw new IOException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * @{@inheritDoc
	 */
	@Override
	public Long getMaxAgeSeconds(final Auth auth) {
		log.info("getMaxAgeSeconds()");
		return getFactory().getMaxAgeSeconds();
	}

	@Override
	public void replaceContent(final InputStream in, final Long length)
			throws BadRequestException, ConflictException,
			NotAuthorizedException {

		log.info("replaceContent()");

		try {
			getContentService().setFileContent(getIrodsFile(), in,
					retrieveIrodsAccount());
		} catch (IOException ex) {
			throw new BadRequestException("Couldnt write to: "
					+ getIrodsFile().getAbsolutePath(), ex);
		}
	}

	@Override
	public String getName() {
		return getIrodsFile().getName();
	}

	@Override
	public String getUniqueId() {
		return getIrodsFile().toString();
	}

	@Override
	public void moveTo(final CollectionResource destinationPath,
			final String newName) throws ConflictException,
			NotAuthorizedException, BadRequestException {
		log.info("moveTo()");
		if (destinationPath == null) {
			throw new IllegalArgumentException("null destinationPath");
		}

		if (newName == null || newName.isEmpty()) {
			throw new IllegalArgumentException("null or empty newName");
		}

		log.info("destinationPath:{}", destinationPath);
		log.info("newName:{}", newName);

		IRODSFile destFile;
		try {

			destFile = fileFromCollectionResource(destinationPath, newName);

			DataTransferOperations dto = getIrodsAccessObjectFactory()
					.getDataTransferOperations(retrieveIrodsAccount());

			log.info("doing a move from source:{}", getIrodsFile());
			dto.move(getIrodsFile(), destFile);
			log.info("move completed");

		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}
	}

	@Override
	public void delete() throws NotAuthorizedException, ConflictException,
	BadRequestException {

		log.info("delete()");
		getIrodsFile().delete();
		log.info("deleted");

	}

	@Override
	protected void doCopy(final IRODSFile dest) {
		log.info("doCopy()");
		log.info("dest:{}", dest);

		try {
			DataTransferOperations dto = getIrodsAccessObjectFactory()
					.getDataTransferOperations(retrieveIrodsAccount());

			log.info("doing a copy from source:{}", getIrodsFile());
			dto.copy(getIrodsFile(), dest, null, null);
			log.info("copy completed");

		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}

	}

	@Override
	public Date getModifiedDate() {
		log.info("getModifiedDate()");

		if (collectionAndDataObjectListingEntry != null) {
			log.debug("cached modified date");
			return collectionAndDataObjectListingEntry.getModifiedAt();
		} else {
			return new Date(getIrodsFile().lastModified());
		}
	}

	@Override
	public Date getCreateDate() {
		return null;
	}

	@Override
	public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo)
			throws NotAuthorizedException {
		log.info("lock()");
		if (getFactory().getLockManager() == null) {
			log.error("unable to get lock manager from factory");
			throw new ConfigurationRuntimeException(
					"a lock manager was not configured");
		}
		return getFactory().getLockManager().lock(timeout, lockInfo, this);
	}

	@Override
	public LockResult refreshLock(final String token)
			throws NotAuthorizedException {
		log.info("refreshLock()");
		return getFactory().getLockManager().refresh(token, this);
	}

	@Override
	public void unlock(final String tokenId) throws NotAuthorizedException {
		log.info("unlock");
		getFactory().getLockManager().unlock(tokenId, this);
	}

	@Override
	public LockToken getCurrentLock() {
		log.info("getCurrentLock()");
		if (getFactory().getLockManager() == null) {
			log.error("unable to get lock manager from factory");
			throw new ConfigurationRuntimeException(
					"a lock manager was not configured");
		}
		if (getFactory().getLockManager() != null) {
			return getFactory().getLockManager().getCurrentToken(this);
		} else {
			log.warn("getCurrentLock called, but no lock manager: file: "
					+ getIrodsFile().getAbsolutePath());
			return null;
		}
	}
}