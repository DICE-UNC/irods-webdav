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
import io.milton.http.Range;
import io.milton.http.XmlWriter;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.MakeCollectionableResource;
import io.milton.resource.MoveableResource;
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
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a directory in a physical file system.
 * 
 */
// TODO: left out LockingCollectionResource, for now
public class IrodsDirectoryResource extends BaseResource implements
		CollectionResource, MakeCollectionableResource, PutableResource,
		CopyableResource, DeletableResource, MoveableResource, GetableResource {

	private static final Logger log = LoggerFactory
			.getLogger(IrodsDirectoryResource.class);

	private final IrodsFileContentService contentService;
	private final IRODSFile dir;
	private final String host;

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
		this.dir = dir;
		this.host = host;
	}

	@Override
	public CollectionResource createCollection(String name) {
		IRODSFile fnew;
		try {
			fnew = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath(), name);
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
	public Resource child(String name) {
		IRODSFile fchild;
		try {
			fchild = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath(), name);
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create file", e);
		}
		return getFactory().resolveFile(this.host, fchild);

	}

	@Override
	public List<? extends Resource> getChildren() {
		ArrayList<BaseResource> list = new ArrayList<BaseResource>();
		File[] files = this.dir.listFiles();
		if (files != null) {
			for (File fchild : files) {
				BaseResource res = getFactory().resolveFile(this.host,
						(IRODSFile) fchild);
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
	public Resource createNew(String name, InputStream in, Long length,
			String contentType) throws IOException {
		log.info("createNew()");
		IRODSFile dest;
		try {
			dest = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath(), name);
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create new file", e);
		}
		contentService.setFileContent(dest, in, retrieveIrodsAccount());
		return getFactory().resolveFile(this.host, dest);

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
	public void sendContent(OutputStream out, Range range,
			Map<String, String> params, String contentType) throws IOException,
			NotAuthorizedException {

		IRODSFile rootFile;
		try {
			rootFile = this.instanceIrodsFileFactory().instanceIRODSFile(
					this.getFactory().getRoot());
		} catch (JargonException e) {
			log.error("error getting root file", e);
			throw new WebDavRuntimeException("error getting root file", e);
		}

		String subpath = dir.getCanonicalPath()
				.substring(rootFile.getCanonicalPath().length())
				.replace('\\', '/');
		String uri = subpath;
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
		w.begin("h1").open().writeText(this.getName()).close();
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
	public Long getMaxAgeSeconds(Auth auth) {
		return getFactory().getMaxAgeSeconds();
	}

	@Override
	public String getContentType(String accepts) {
		return "text/html";
	}

	@Override
	public Long getContentLength() {
		return 0L;
	}

	private String buildHref(String uri, String name) {
		String abUrl = uri;

		if (!abUrl.endsWith("/")) {
			abUrl += "/";
		}
		if (this.getFactory().getSsoPrefix() == null) {
			return abUrl + name;
		} else {
			// This is to match up with the prefix set on
			// SimpleSSOSessionProvider in MyCompanyDavServlet
			String s = insertSsoPrefix(abUrl, this.getFactory().getSsoPrefix());
			return s += name;
		}
	}

	public static String insertSsoPrefix(String abUrl, String prefix) {
		// need to insert the ssoPrefix immediately after the host and port
		int pos = abUrl.indexOf("/", 8);
		String s = abUrl.substring(0, pos) + "/" + prefix;
		s += abUrl.substring(pos);
		return s;
	}

	@Override
	public Date getModifiedDate() {
		IRODSFile file;
		try {
			file = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath());
			return new Date(file.lastModified());
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create file", e);
		}
	}

	@Override
	public String getName() {
		IRODSFile file;
		try {
			file = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath());
			return file.getName();
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create file", e);
		}
	}

	@Override
	public String getUniqueId() {
		IRODSFile file;
		try {
			file = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath());
			return file.toString();
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create file", e);
		}
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

		IRODSFile file;
		IRODSFile destFile;
		try {
			file = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath());

			destFile = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath());

			DataTransferOperations dto = this.getIrodsAccessObjectFactory()
					.getDataTransferOperations(this.retrieveIrodsAccount());

			log.info("doing a move from source:{}", this.dir);
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
		log.info("of collection:{}", dir);
		IRODSFile file;
		try {
			file = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath());
			file.delete();
			log.info("delete successful");
		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}

	}

	@Override
	protected void doCopy(IRODSFile destFile) {
		log.info("doCopy()");
		if (destFile == null) {
			throw new IllegalArgumentException("null destFile");
		}

		log.info("desFilet:{}", destFile);

		IRODSFile file;
		try {
			file = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath());

			DataTransferOperations dto = this.getIrodsAccessObjectFactory()
					.getDataTransferOperations(this.retrieveIrodsAccount());

			log.info("doing a copy from source:{}", this.dir);
			dto.copy(file, destFile, null, null);
			log.info("copy completed");

		} catch (JargonException e) {
			log.error("error in move operation", e);
			throw new WebDavRuntimeException("unable to move directory", e);
		}

	}

	/**
	 * @return the dir
	 */
	public IRODSFile getDir() {
		return dir;
	}

}