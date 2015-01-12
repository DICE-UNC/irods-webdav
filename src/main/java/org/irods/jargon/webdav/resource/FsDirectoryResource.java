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
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.XmlWriter;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.LockedException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.PreConditionFailedException;
import io.milton.http.fs.FsFileResource;
import io.milton.http.fs.FsResource;
import io.milton.resource.CollectionResource;
import io.milton.resource.CopyableResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
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
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.exception.WebDavRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a directory in a physical file system.
 * 
 */
public class FsDirectoryResource extends BaseResource implements
		MakeCollectionableResource, PutableResource, CopyableResource,
		DeletableResource, MoveableResource, PropFindableResource,
		LockingCollectionResource, GetableResource {

	private static final Logger log = LoggerFactory
			.getLogger(FsDirectoryResource.class);

	private final IrodsFileContentService contentService;
	private final IRODSFile dir;
	private final String host;

	public FsDirectoryResource(String host,
			IrodsFileSystemResourceFactory factory, IRODSFile dir,
			IrodsFileContentService contentService) {
		super(factory, factory.getIrodsAccessObjectFactory(), factory
				.getWebDavConfig());
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
		return new FsDirectoryResource(host, getFactory(), fnew, contentService);
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
		ArrayList<FsResource> list = new ArrayList<FsResource>();
		File[] files = this.dir.listFiles();
		if (files != null) {
			for (File fchild : files) {
				FsResource res = getFactory().resolveFile(this.host,
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

	/**
	 * Will redirect if a default page has been specified on the factory
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public String checkRedirect(Request request) {
		if (getFactory().getDefaultPage() != null) {
			return request.getAbsoluteUrl() + "/"
					+ getFactory().getDefaultPage();
		} else {
			return null;
		}
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
		contentService.setFileContent(dest, in);
		return getFactory().resolveFile(this.host, dest);

	}

	@Override
	public LockToken createAndLock(String name, LockTimeout timeout,
			LockInfo lockInfo) throws NotAuthorizedException {
		IRODSFile dest;
		try {
			dest = this.instanceIrodsFileFactory().instanceIRODSFile(
					dir.getAbsolutePath(), name);
		} catch (JargonException e) {
			log.error("unable to create IRODSFile", e);
			throw new WebDavRuntimeException("unable to create new file", e);
		}
		createEmptyFile(dest);
		FsFileResource newRes = new FsFileResource(host, getFactory(), dest,
				contentService);
		LockResult res = newRes.lock(timeout, lockInfo);
		return res.getLockToken();
	}

	private void createEmptyFile(IRODSFile file) {
		try {
			file.createNewFile();
		} catch (IOException e) {
			log.error("unable to create new empty IRODSFile", e);
			throw new WebDavRuntimeException("unable to create empty file", e);
		}
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

	@Override
	public Long getMaxAgeSeconds(Auth auth) {
		return null;
	}

	@Override
	public String getContentType(String accepts) {
		return "text/html";
	}

	@Override
	public Long getContentLength() {
		return null;
	}

	private String buildHref(String uri, String name) {
		String abUrl = uri;

		if (!abUrl.endsWith("/")) {
			abUrl += "/";
		}
		if (ssoPrefix == null) {
			return abUrl + name;
		} else {
			// This is to match up with the prefix set on
			// SimpleSSOSessionProvider in MyCompanyDavServlet
			String s = insertSsoPrefix(abUrl, ssoPrefix);
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

	/**
	 * @return the webDavConfig
	 */
	@Override
	public WebDavConfig getWebDavConfig() {
		return webDavConfig;
	}

	/**
	 * @param webDavConfig
	 *            the webDavConfig to set
	 */
	@Override
	public void setWebDavConfig(WebDavConfig webDavConfig) {
		this.webDavConfig = webDavConfig;
	}

	@Override
	public Object authenticate(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockToken getCurrentLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockResult lock(LockTimeout arg0, LockInfo arg1)
			throws NotAuthorizedException, PreConditionFailedException,
			LockedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockResult refreshLock(String arg0) throws NotAuthorizedException,
			PreConditionFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unlock(String arg0) throws NotAuthorizedException,
			PreConditionFailedException {
		// TODO Auto-generated method stub

	}

	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveTo(CollectionResource arg0, String arg1)
			throws ConflictException, NotAuthorizedException,
			BadRequestException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() throws NotAuthorizedException, ConflictException,
			BadRequestException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyTo(CollectionResource arg0, String arg1)
			throws NotAuthorizedException, BadRequestException,
			ConflictException {
		// TODO Auto-generated method stub

	}
}