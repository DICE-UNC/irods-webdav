package org.irods.jargon.webdav.resource;

import io.milton.resource.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.webdav.config.IrodsAuthService;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class IrodsDirectoryResourceTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsDirectoryResourceTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new AssertionHelper();
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testCreateCollection() throws Exception {
		String testTargetColl = "testCreateCollection";

		String rootColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(rootColl);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsColl);

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				"/", manager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito
				.mock(IrodsFileContentService.class);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host",
				factory, rootCollection, service);

		resource.createCollection(testTargetColl);

		Assert.assertTrue("did not create subcoll", targetCollection.exists());
	}

	@Test
	public void testGetChild() throws Exception {
		String testTargetColl = "testGetChild";

		String rootColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(rootColl);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsColl);

		targetCollection.mkdirs();

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				"/", manager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito
				.mock(IrodsFileContentService.class);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host",
				factory, rootCollection, service);

		Resource child = resource.child(testTargetColl);

		Assert.assertEquals("did not find target col", testTargetColl,
				child.getName());
	}

	@Test
	public void testGetChildren() throws Exception {
		String testTargetColl = "testGetChildren";

		int count = 3;

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsColl);

		targetCollection.mkdirs();

		String myTarget = "";
		IRODSFile irodsFile;
		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsColl + "/f" + (10000 + i) + ".txt";
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
		}

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsColl + "/c" + (10000 + i);
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
					.instanceIRODSFile(myTarget);
			irodsFile.mkdirs();
		}

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				"/", manager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito
				.mock(IrodsFileContentService.class);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host",
				factory, targetCollection, service);

		List<? extends Resource> actual = resource.getChildren();
		Assert.assertFalse("no children returned", actual.isEmpty());

	}

	@Test
	public void testCreateNew() throws Exception {
		String testFileName = "testCreateNew.txt";

		String rootColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(rootColl);

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				"/", manager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host",
				factory, rootCollection, service);

		long fileLength = 100L;

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName,
						fileLength);
		File localFile = new File(localFilePath);
		FileInputStream fileInputStream = new FileInputStream(localFile);

		resource.createNew(testFileName, fileInputStream, fileLength,
				"text/html");

	}

}
