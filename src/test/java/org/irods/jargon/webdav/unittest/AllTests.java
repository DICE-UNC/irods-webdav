package org.irods.jargon.webdav.unittest;

import org.irods.jargon.webdav.resource.FileContentServiceTest;
import org.irods.jargon.webdav.resource.IrodsDirectoryResourceTest;
import org.irods.jargon.webdav.resource.IrodsFileResourceTest;
import org.irods.jargon.webdav.resource.IrodsFileSystemResourceFactoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ IrodsFileResourceTest.class, IrodsDirectoryResourceTest.class,
	IrodsFileSystemResourceFactoryTest.class, FileContentServiceTest.class })
public class AllTests {

}
