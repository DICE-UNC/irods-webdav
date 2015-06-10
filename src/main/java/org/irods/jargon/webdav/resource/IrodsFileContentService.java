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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.pub.io.PackingIrodsInputStream;
import org.irods.jargon.core.pub.io.PackingIrodsOutputStream;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE
 */
public class IrodsFileContentService implements FileContentService {

	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	private WebDavConfig webDavConfig;

	private static final Logger log = LoggerFactory
			.getLogger(IrodsFileContentService.class);

	@Override
	public void setFileContent(final IRODSFile dest, final InputStream in,
			final IRODSAccount irodsAccount) throws FileNotFoundException,
			IOException {

		log.info("setFileContent()");

		if (dest == null) {
			throw new IllegalArgumentException("null dest");
		}

		if (in == null) {
			throw new IllegalArgumentException("null in");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("doing transfer");
		try {
			Stream2StreamAO stream2Stream = irodsAccessObjectFactory
					.getStream2StreamAO(irodsAccount);

			if (webDavConfig.isUsePackingStreams()) {
				log.info("use packing stream for transfer");
				IRODSFileOutputStream outputStream = this
						.getIrodsAccessObjectFactory()
						.getIRODSFileFactory(irodsAccount)
						.instanceIRODSFileOutputStream(dest);
				PackingIrodsOutputStream ipos = new PackingIrodsOutputStream(
						outputStream);
				stream2Stream.streamToStreamCopyUsingStandardIO(in, ipos);
			} else {
				log.info("use normal stream for transfer");
				stream2Stream.transferStreamToFileUsingIOStreams(in,
						(File) dest, dest.length(), irodsAccessObjectFactory
								.getJargonProperties()
								.getInputToOutputCopyBufferByteSize());

			}
		} catch (JargonException e) {
			log.error("error in setting file content", e);
			throw new WebDavRuntimeException("exception streaming to file", e);
		}

	}

	@Override
	public InputStream getFileContent(final IRODSFile file,
			final IRODSAccount irodsAccount) throws FileNotFoundException {

		log.info("getFileContent()");

		if (file == null) {
			throw new IllegalArgumentException("null file");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (!file.exists()) {
			log.error("did not find file at:{}", file);
			throw new FileNotFoundException("file not found");
		}

		try {
			IRODSFileFactory factory = irodsAccessObjectFactory
					.getIRODSFileFactory(irodsAccount);

			IRODSFileInputStream inputStream = factory
					.instanceIRODSFileInputStream(file.getAbsolutePath());

			if (this.webDavConfig.isUsePackingStreams()) {
				log.info("using packing stream");
				return new PackingIrodsInputStream(inputStream);
			} else {
				log.info("use normal irods stream");
				return inputStream;
			}

		} catch (JargonException e) {
			log.error("error in setting file content", e);
			throw new WebDavRuntimeException("exception streaming to file", e);
		}

	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * @return the webDavConfig
	 */
	public WebDavConfig getWebDavConfig() {
		return webDavConfig;
	}

	/**
	 * @param webDavConfig
	 *            the webDavConfig to set
	 */
	public void setWebDavConfig(WebDavConfig webDavConfig) {
		this.webDavConfig = webDavConfig;
	}
}
