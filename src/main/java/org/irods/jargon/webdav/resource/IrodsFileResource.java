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
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class IrodsFileResource extends BaseResource implements
		CopyableResource, DeletableResource, GetableResource, MoveableResource,
		ReplaceableResource, PropFindableResource, LockableResource { // removed
	// PropFindableResource,
	// temporarily

	private static final Logger log = LoggerFactory
			.getLogger(IrodsFileResource.class);

	/**
	 * 
	 * @param host
	 *            - the requested host. E.g. www.mycompany.com
	 * @param factory
	 * @param file
	 */
	public IrodsFileResource(String host,
			IrodsFileSystemResourceFactory factory, IRODSFile file,
			IrodsFileContentService contentService) {
		super(factory, factory.getIrodsAccessObjectFactory(), factory
				.getWebDavConfig(), contentService);

		if (file == null) {
			throw new IllegalArgumentException("null file");
		}

		this.setIrodsFile(file);
	}

	@Override
	public Long getContentLength() {
		log.info("getContentLength()");
		long length = this.getIrodsFile().length();
		return length;
	}

	@Override
	public String getContentType(String preferredList) {
		log.info("getContentType()");
		String mime = ContentTypeUtils.findContentTypes(this.getIrodsFile()
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
	public void sendContent(OutputStream out, Range range,
			Map<String, String> params, String contentType) throws IOException,
			NotFoundException {
		log.info("sendContent()");
		InputStream in = null;
		try {
			log.debug("getting input stream...");
			in = this.getContentService().getFileContent(this.getIrodsFile(),
					retrieveIrodsAccount());
			log.debug("got input stream...");
			if (range != null) {
				log.debug("sendContent: ranged content: "
						+ this.getIrodsFile().getAbsolutePath());
				RangeUtils.writeRange(in, range, out);
			} else {
				log.debug("sendContent: send whole file "
						+ this.getIrodsFile().getAbsolutePath());
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
	public Long getMaxAgeSeconds(Auth auth) {
		return getFactory().getMaxAgeSeconds();
	}

	@Override
	public void replaceContent(InputStream in, Long length)
			throws BadRequestException, ConflictException,
			NotAuthorizedException {

		log.info("replaceContent()");

		try {
			getContentService().setFileContent(this.getIrodsFile(), in,
					this.retrieveIrodsAccount());
		} catch (IOException ex) {
			throw new BadRequestException("Couldnt write to: "
					+ this.getIrodsFile().getAbsolutePath(), ex);
		}
	}

	@Override
	public String getName() {
		return this.getIrodsFile().getName();
	}

	@Override
	public String getUniqueId() {
		return this.getIrodsFile().toString();
	}

	@Override
	public void moveTo(CollectionResource destinationPath, String newName)
			throws ConflictException, NotAuthorizedException,
			BadRequestException {
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

			destFile = this
					.fileFromCollectionResource(destinationPath, newName);

			DataTransferOperations dto = this.getIrodsAccessObjectFactory()
					.getDataTransferOperations(this.retrieveIrodsAccount());

			log.info("doing a move from source:{}", this.getIrodsFile());
			dto.move(this.getIrodsFile(), destFile);
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
		this.getIrodsFile().delete();
		log.info("deleted");

	}

	@Override
	protected void doCopy(IRODSFile dest) {
		log.info("dest:{}", dest);

		try {

			DataTransferOperations dto = this.getIrodsAccessObjectFactory()
					.getDataTransferOperations(this.retrieveIrodsAccount());

			log.info("doing a copy from source:{}", this.getIrodsFile());
			dto.copy(this.getIrodsFile(), dest, null, null);
			log.info("copy completed");

		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}

	}

	@Override
	public Date getModifiedDate() {
		return new Date(this.getIrodsFile().lastModified());
	}

	@Override
	public Date getCreateDate() {
		return null;
	}

	@Override
	public LockResult lock(LockTimeout timeout, LockInfo lockInfo)
			throws NotAuthorizedException {
		return getFactory().getLockManager().lock(timeout, lockInfo, this);
	}

	@Override
	public LockResult refreshLock(String token) throws NotAuthorizedException {
		return getFactory().getLockManager().refresh(token, this);
	}

	@Override
	public void unlock(String tokenId) throws NotAuthorizedException {
		getFactory().getLockManager().unlock(tokenId, this);
	}

	@Override
	public LockToken getCurrentLock() {
		if (getFactory().getLockManager() != null) {
			return getFactory().getLockManager().getCurrentToken(this);
		} else {
			log.warn("getCurrentLock called, but no lock manager: file: "
					+ this.getIrodsFile().getAbsolutePath());
			return null;
		}
	}
}