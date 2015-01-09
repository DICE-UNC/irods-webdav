/**
 * 
 */
package org.irods.jargon.webdav.resource;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.CollectionResource;
import io.milton.resource.MakeCollectionableResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.pub.io.IRODSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE Wraps iRODS files in milton resource semantics see
 *         https
 *         ://github.com/miltonio/milton2/tree/master/milton-server-ce/src/main
 *         /java/io/milton/http/fs
 */
public class FileResource extends AbstractResource implements
		MakeCollectionableResource {

	private IRODSFile irodsFile;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public FileResource(IRODSFile irodsFile) {
		super();
		if (irodsFile == null) {
			throw new IllegalArgumentException("null irodsFile");
		}
		this.irodsFile = irodsFile;

	}

	@Override
	public List<? extends FileResource> getChildren() {
		log.info("getChildren() of:{}", irodsFile);
		File[] children = irodsFile.listFiles();
		List<FileResource> files = new ArrayList<FileResource>();
		FileResource fileResource;
		for (File file : children) {
			files.add(new FileResource((IRODSFile) file));
		}
		return files;
	}

	@Override
	public CollectionResource createCollection(String newName)
			throws NotAuthorizedException, ConflictException,
			BadRequestException {

		log.info("createCollection()");
		if (newName == null || newName.isEmpty()) {
			throw new IllegalArgumentException("null or empty newName");
		}

		// TODO: mkdir with new name

		throw new UnsupportedOperationException(
				"mkdir and return new file here");
	}

	@Override
	public Resource child(String childName) {
		return ChildUtils.child(childName, getChildren());
	}

	@Override
	public String getName() {
		return irodsFile.getName();
	}

}