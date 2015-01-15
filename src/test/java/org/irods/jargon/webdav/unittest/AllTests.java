package org.irods.jargon.webdav.unittest;

import org.irods.jargon.webdav.resource.IrodsDirectoryResourceTest;
import org.irods.jargon.webdav.resource.IrodsFileResourceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ IrodsFileResourceTest.class, IrodsDirectoryResourceTest.class })
public class AllTests {

}
