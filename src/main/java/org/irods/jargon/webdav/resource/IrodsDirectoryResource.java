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

import io.milton.http.Auth;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Range;
import io.milton.http.XmlWriter;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.LockableResource;
import io.milton.resource.LockingCollectionResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.MoveableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a directory in a physical file system.
 *
 */
public class IrodsDirectoryResource extends BaseResource implements
		CollectionResource, MakeCollectionableResource, PutableResource,
		CopyableResource, DeletableResource, MoveableResource, GetableResource,
		PropFindableResource, LockingCollectionResource, LockableResource {

	private static final Logger log = LoggerFactory
			.getLogger(IrodsDirectoryResource.class);

	private final IrodsFileContentService contentService;
	private final String host;
	/**
	 * This field may be <code>null</code> and is only available when the milton
	 * config is set to cache file demographics. In this case, the entry will be
	 * used to provide file data such as length without necessitating a new
	 * query
	 */
	private final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry;

	public IrodsDirectoryResource(final String host,
			final IrodsFileSystemResourceFactory factory, final IRODSFile dir,
			final IrodsFileContentService contentService) {
		super(factory, factory.getIrodsAccessObjectFactory(), factory
				.getWebDavConfig(), contentService);
		this.contentService = contentService;
		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory does not exist: "
					+ dir.getAbsolutePath());
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: "
					+ dir.getAbsolutePath());
		}
		setIrodsFile(dir);
		this.host = host;
		this.collectionAndDataObjectListingEntry = null;
	}

	/**
	 * Constructor that can provide a
	 * <code>CollectionAndDataObjectListingEntry</code>. This will trigger a
	 * caching behavior for some file demographics which can result in 'stale'
	 * data, while limiting queries to the catalog and increasing performance.
	 * 
	 * @param host
	 * @param factory
	 * @param dir
	 * @param collectionAndDataObjectListingEntry
	 * @param contentService
	 */
	public IrodsDirectoryResource(
			final String host,
			final IrodsFileSystemResourceFactory factory,
			final IRODSFile dir,
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry,
			final IrodsFileContentService contentService) {
		super(factory, factory.getIrodsAccessObjectFactory(), factory
				.getWebDavConfig(), contentService);
		this.contentService = contentService;
		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory does not exist: "
					+ dir.getAbsolutePath());
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: "
					+ dir.getAbsolutePath());
		}
		setIrodsFile(dir);
		this.host = host;
		this.collectionAndDataObjectListingEntry = collectionAndDataObjectListingEntry;
	}

	@Override
	public CollectionResource createCollection(final String name) {
		log.info("createCollection()");
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}

		log.info("name:{}", name);
		IRODSFile fnew;
		try {
			fnew = instanceIrodsFileFactory().instanceIRODSFile(
					getIrodsFile().getAbsolutePath(), name);
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create file", e);
		}
		boolean ok = fnew.mkdir();
		if (!ok) {
			log.error("error creating collection:{}", fnew);
			throw new WebDavRuntimeException("error creating directory");
		}
		return new IrodsDirectoryResource(host, getFactory(), fnew,
				contentService);
	}

	@Override
	public Resource child(final String name) {
		log.info("child()");
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}

		log.info("name:{}", name);
		IRODSFile fchild;
		try {
			fchild = instanceIrodsFileFactory().instanceIRODSFile(
					getIrodsFile().getAbsolutePath(), name);
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create file", e);
		}
		return getFactory().resolveFile(host, fchild);

	}

	@Override
	public List<? extends Resource> getChildren() {
		log.info("getChildren()");
		if (this.getWebDavConfig().isCacheFileDemographics()) {
			return getChildrenUtilizingCaching();
		} else {
			return getChildrenViaFile();
		}
	}

	/**
	 * Method to get children that will cache file information. This can avoid
	 * additional queries for things like file length
	 * 
	 * @return
	 */
	private List<? extends Resource> getChildrenUtilizingCaching() {
		log.info("getChildrenUtilizingCaching()");
		try {
			CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = this
					.getIrodsAccessObjectFactory()
					.getCollectionAndDataObjectListAndSearchAO(
							this.retrieveIrodsAccount());

			log.info("getting collections");
			List<CollectionAndDataObjectListingEntry> entries = null;
			List<BaseResource> resources = new ArrayList<BaseResource>();
			boolean more = true;
			int count = 0;
			while (more) {
				log.info("querying child collections starting at:{}", count);
				log.info("under parent:{}", this.getIrodsFile()
						.getAbsolutePath());
				entries = collectionAndDataObjectListAndSearchAO
						.listCollectionsUnderPath(getIrodsFile()
								.getAbsolutePath(), count);

				if (entries.isEmpty()) {
					break;
				}

				for (CollectionAndDataObjectListingEntry entry : entries) {
					IRODSFile childFile = this
							.instanceIrodsFileFactory()
							.instanceIRODSFile(entry.getFormattedAbsolutePath());
					IrodsDirectoryResource fileResource = new IrodsDirectoryResource(
							this.host, this.getFactory(), childFile, entry,
							this.contentService);
					resources.add(fileResource);
					if (entry.isLastResult()) {
						more = false;
					}
					count = entry.getCount();
				}

			}
			more = true;
			count = 0;
			while (more) {
				log.info("querying child files starting at:{}", count);
				log.info("under parent:{}", this.getIrodsFile()
						.getAbsolutePath());
				entries = collectionAndDataObjectListAndSearchAO
						.listDataObjectsUnderPath(getIrodsFile()
								.getAbsolutePath(), count);

				if (entries.isEmpty()) {
					break;
				}

				for (CollectionAndDataObjectListingEntry entry : entries) {
					IRODSFile childFile = this
							.instanceIrodsFileFactory()
							.instanceIRODSFile(entry.getFormattedAbsolutePath());
					IrodsFileResource fileResource = new IrodsFileResource(
							this.host, this.getFactory(), childFile, entry,
							this.contentService);
					resources.add(fileResource);
					if (entry.isLastResult()) {
						more = false;
					}
					count = entry.getCount();
				}

			}
			log.info("...done");
			return resources;

		} catch (JargonException e) {
			log.error("error getting irods access object", e);
			throw new WebDavRuntimeException(
					"unable to get irods access object", e);
		}

	}

	/**
	 * Method to get children that will not cache file information, may be more
	 * timely,but necessitates multiple individual queries
	 * 
	 * @return
	 */
	private List<? extends Resource> getChildrenViaFile() {
		log.info("for dir:{}", getIrodsFile());
		ArrayList<BaseResource> list = new ArrayList<BaseResource>();
		File[] files = getIrodsFile().listFiles();
		if (files != null) {
			for (File fchild : files) {
				BaseResource res = getFactory().resolveFile(host,
						(IRODSFile) fchild);
				log.info("added as child:{}", res);
				if (res != null) {
					list.add(res);
				} else {
					log.error("Couldnt resolve file {}",
							fchild.getAbsolutePath());
				}
			}
		}
		return list;
	}

	@Override
	public Resource createNew(final String name, final InputStream in,
			final Long length, final String contentType) throws IOException {
		log.info("createNew()");
		IRODSFile dest;
		try {
			dest = instanceIrodsFileFactory().instanceIRODSFile(
					getIrodsFile().getAbsolutePath(), name);
			dest.createNewFile();
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create new file", e);
		}
		contentService.setFileContent(dest, in, retrieveIrodsAccount());
		return getFactory().resolveFile(host, dest);

	}

	/**
	 * Will generate a listing of the contents of this directory, unless the
	 * factory's allowDirectoryBrowsing has been set to false.
	 *
	 * If so it will just output a message saying that access has been disabled.
	 *
	 * @param out
	 * @param range
	 * @param params
	 * @param contentType
	 * @throws IOException
	 * @throws NotAuthorizedException
	 */
	@Override
	public void sendContent(final OutputStream out, final Range range,
			final Map<String, String> params, final String contentType)
			throws IOException, NotAuthorizedException {

		String subpath = getIrodsFile().getCanonicalPath();
		if (getIrodsFile().getCanonicalPath().length() > 1) {
			subpath = subpath.substring(getIrodsFile().getCanonicalPath()
					.length());
		}
		String uri = subpath.replace('\\', '/');

		// String uri = "/" + factory.getContextPath() + subpath;
		XmlWriter w = new XmlWriter(out);
		w.open("html");
		w.open("head");
		w.writeText(""
				+ "<script type=\"text/javascript\" language=\"javascript1.1\">\n"
				+ "    var fNewDoc = false;\n"
				+ "  </script>\n"
				+ "  <script LANGUAGE=\"VBSCRIPT\">\n"
				+ "    On Error Resume Next\n"
				+ "    Set EditDocumentButton = CreateObject(\"SharePoint.OpenDocuments.3\")\n"
				+ "    fNewDoc = IsObject(EditDocumentButton)\n"
				+ "  </script>\n"
				+ "  <script type=\"text/javascript\" language=\"javascript1.1\">\n"
				+ "    var L_EditDocumentError_Text = \"The edit feature requires a SharePoint-compatible application and Microsoft Internet Explorer 4.0 or greater.\";\n"
				+ "    var L_EditDocumentRuntimeError_Text = \"Sorry, couldnt open the document.\";\n"
				+ "    function editDocument(strDocument) {\n"
				+ "      strDocument = 'http://192.168.1.2:8080' + strDocument; "
				+ "      if (fNewDoc) {\n"
				+ "        if (!EditDocumentButton.EditDocument(strDocument)) {\n"
				+ "          alert(L_EditDocumentRuntimeError_Text + ' - ' + strDocument); \n"
				+ "        }\n"
				+ "      } else { \n"
				+ "        alert(L_EditDocumentError_Text + ' - ' + strDocument); \n"
				+ "      }\n" + "    }\n" + "  </script>\n");

		w.close("head");
		w.open("body");
		w.begin("h1").open().writeText(getName()).close();
		w.open("table");
		for (Resource r : getChildren()) {
			w.open("tr");

			w.open("td");
			String path = buildHref(uri, r.getName());
			w.begin("a").writeAtt("href", path).open().writeText(r.getName())
					.close();

			w.begin("a").writeAtt("href", "#")
					.writeAtt("onclick", "editDocument('" + path + "')").open()
					.writeText("(edit with office)").close();

			w.close("td");

			w.begin("td").open().writeText(r.getModifiedDate() + "").close();
			w.close("tr");
		}
		w.close("table");
		w.close("body");
		w.close("html");
		w.flush();
	}

	/**
	 * @{@inheritDoc
	 */
	@Override
	public Long getMaxAgeSeconds(final Auth auth) {
		return getFactory().getMaxAgeSeconds();
	}

	@Override
	public String getContentType(final String accepts) {
		return "text/html";
	}

	@Override
	public Long getContentLength() {
		return 0L;
	}

	private String buildHref(final String uri, final String name) {
		String abUrl = uri;

		if (!abUrl.endsWith("/")) {
			abUrl += "/";
		}
		if (getFactory().getSsoPrefix() == null) {
			return abUrl + name;
		} else {
			// This is to match up with the prefix set on
			// SimpleSSOSessionProvider in MyCompanyDavServlet
			String s = insertSsoPrefix(abUrl, getFactory().getSsoPrefix());
			return s += name;
		}
	}

	public static String insertSsoPrefix(final String abUrl, final String prefix) {
		// need to insert the ssoPrefix immediately after the host and port
		int pos = abUrl.indexOf("/", 8);
		String s = abUrl.substring(0, pos) + "/" + prefix;
		s += abUrl.substring(pos);
		return s;
	}

	@Override
	public Date getModifiedDate() {

		if (this.collectionAndDataObjectListingEntry != null) {
			return this.collectionAndDataObjectListingEntry.getModifiedAt();
		} else {

			IRODSFile file;
			try {
				file = instanceIrodsFileFactory().instanceIRODSFile(
						getIrodsFile().getAbsolutePath());
				return new Date(file.lastModified());
			} catch (JargonException e) {
				log.error("unable to create IRODSFile", e);
				throw new WebDavRuntimeException("unable to create file", e);
			}
		}
	}

	@Override
	public String getName() {
		if (this.collectionAndDataObjectListingEntry != null) {
			return MiscIRODSUtils
					.getLastPathComponentForGiveAbsolutePath(collectionAndDataObjectListingEntry
							.getFormattedAbsolutePath());
		} else {
			IRODSFile file;
			try {
				file = instanceIrodsFileFactory().instanceIRODSFile(
						getIrodsFile().getAbsolutePath());
				return file.getName();
			} catch (JargonException e) {
				log.error("unable to create IRODSFile", e);
				throw new WebDavRuntimeException("unable to create file", e);
			}
		}
	}

	@Override
	public String getUniqueId() {
		if (this.collectionAndDataObjectListingEntry != null) {
			return collectionAndDataObjectListingEntry
					.getFormattedAbsolutePath();
		} else {

			IRODSFile file;
			try {
				file = instanceIrodsFileFactory().instanceIRODSFile(
						getIrodsFile().getAbsolutePath());
				return file.toString();
			} catch (JargonException e) {
				log.error("unable to create IRODSFile", e);
				throw new WebDavRuntimeException("unable to create file", e);
			}
		}
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

		// IrodsDirectoryResource dirResource = (IrodsDirectoryResource)
		// destinationPath;

		// IRODSFile file;
		// IRODSFile destFile;
		try {

			IRODSFile destFile = fileFromCollectionResource(destinationPath,
					newName);

			log.info("dest file:{}", destFile);
			// file.renameTo(destFile);

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
		log.info("of collection:{}", getIrodsFile());
		IRODSFile file;
		try {
			file = instanceIrodsFileFactory().instanceIRODSFile(
					getIrodsFile().getAbsolutePath());
			file.delete();
			log.info("delete successful");
		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}

	}

	@Override
	protected void doCopy(final IRODSFile destFile) {
		log.info("doCopy()");
		if (destFile == null) {
			throw new IllegalArgumentException("null destFile");
		}

		log.info("desFilet:{}", destFile);

		IRODSFile file;
		try {
			file = instanceIrodsFileFactory().instanceIRODSFile(
					getIrodsFile().getAbsolutePath());

			DataTransferOperations dto = getIrodsAccessObjectFactory()
					.getDataTransferOperations(retrieveIrodsAccount());

			log.info("doing a copy from source:{}", getIrodsFile());
			dto.copy(file, destFile, null, null);
			log.info("copy completed");

		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}

	}

	@Override
	public Date getCreateDate() {
		return null;
	}

	@Override
	public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo)
			throws NotAuthorizedException {
		return getFactory().getLockManager().lock(timeout, lockInfo, this);
	}

	@Override
	public LockResult refreshLock(final String token)
			throws NotAuthorizedException {
		return getFactory().getLockManager().refresh(token, this);
	}

	@Override
	public void unlock(final String tokenId) throws NotAuthorizedException {
		getFactory().getLockManager().unlock(tokenId, this);
	}

	@Override
	public LockToken getCurrentLock() {
		if (getFactory().getLockManager() != null) {
			return getFactory().getLockManager().getCurrentToken(this);
		} else {
			log.warn("getCurrentLock called, but no lock manager: file: "
					+ getIrodsFile().getAbsolutePath());
			return null;
		}
	}

	@Override
	public LockToken createAndLock(final String name,
			final LockTimeout lockTimeout, final LockInfo lockInfo)
			throws NotAuthorizedException {
		log.info("createAndLock()");
		log.info("name:{}", name);
		IRODSFile file;
		try {
			file = instanceIrodsFileFactory().instanceIRODSFile(
					getIrodsFile().getAbsolutePath(), name);
			// file.createNewFile();
			log.info("new file created");
		} catch (JargonException e) {
			log.error("error in create file operation", e);
			throw new WebDavRuntimeException("unable to create file", e);
		}
		IrodsFileResource newRes = new IrodsFileResource(host, getFactory(),
				file, contentService);
		LockResult res = newRes.lock(lockTimeout, lockInfo);
		return res.getLockToken();
	}

}
