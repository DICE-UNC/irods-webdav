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
import io.milton.http.Range;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
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
		ReplaceableResource, PropFindableResource { // removed
													// PropFindableResource,
													// temporarily

	private static final Logger log = LoggerFactory
			.getLogger(IrodsFileResource.class);

	private final IRODSFile file;

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

		this.file = file;
	}

	@Override
	public Long getContentLength() {
		return file.length();
	}

	@Override
	public String getContentType(String preferredList) {
		String mime = ContentTypeUtils.findContentTypes(this.file.getName());
		String s = ContentTypeUtils.findAcceptableContentType(mime,
				preferredList);
		if (log.isTraceEnabled()) {
			log.trace("getContentType: preferred: {} mime: {} selected: {}",
					new Object[] { preferredList, mime, s });
		}
		return s;
	}

	@Override
	public void sendContent(OutputStream out, Range range,
			Map<String, String> params, String contentType) throws IOException,
			NotFoundException {
		InputStream in = null;
		try {
			in = this.getContentService().getFileContent(file,
					retrieveIrodsAccount());
			if (range != null) {
				log.debug("sendContent: ranged content: "
						+ file.getAbsolutePath());
				RangeUtils.writeRange(in, range, out);
			} else {
				log.debug("sendContent: send whole file "
						+ file.getAbsolutePath());
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
		try {
			getContentService().setFileContent(file, in,
					this.retrieveIrodsAccount());
		} catch (IOException ex) {
			throw new BadRequestException("Couldnt write to: "
					+ file.getAbsolutePath(), ex);
		}
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getUniqueId() {
		return file.toString();
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

			log.info("doing a move from source:{}", this.file);
			dto.move(file, destFile);
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
		this.file.delete();
		log.info("deleted");

	}

	@Override
	protected void doCopy(IRODSFile dest) {
		log.info("dest:{}", dest);

		try {

			DataTransferOperations dto = this.getIrodsAccessObjectFactory()
					.getDataTransferOperations(this.retrieveIrodsAccount());

			log.info("doing a copy from source:{}", this.file);
			dto.copy(file, dest, null, null);
			log.info("copy completed");

		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}

	}

	@Override
	public Date getModifiedDate() {
		return new Date(file.lastModified());
	}

	/**
	 * @return the file
	 */
	public IRODSFile getFile() {
		return file;
	}

	@Override
	public Date getCreateDate() {
		return null;
	}
}