package org.irods.jargon.webdav.resource;

import java.io.File;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.webdav.config.IrodsAuthService;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class IrodsFileResourceTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsFileResourceTest";
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
	public void testGetName() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetName.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFile);

		dto.putOperation(localFile, destFile, null, null);

		IrodsSecurityManager manager = Mockito.mock(IrodsSecurityManager.class);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				"/", manager);

		WebDavConfig config = new WebDavConfig();
		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito
				.mock(IrodsFileContentService.class);

		IrodsFileResource resource = new IrodsFileResource("host", factory,
				destFile, service);

		String actual = resource.getName();
		Assert.assertEquals("did not find name", testFileName, actual);
	}

	@Test
	public void testGetUniqueId() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetUniqueId.txt";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFile);

		dto.putOperation(localFile, destFile, null, null);

		IrodsSecurityManager manager = Mockito.mock(IrodsSecurityManager.class);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				"/", manager);

		WebDavConfig config = new WebDavConfig();
		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito
				.mock(IrodsFileContentService.class);

		IrodsFileResource resource = new IrodsFileResource("host", factory,
				destFile, service);

		String actual = resource.getUniqueId();
		Assert.assertEquals("did not find unique id", destFile.toString(),
				actual);
	}

	public void testGetContentType() throws Exception {
		// generate a local scratch file
		String testFileName = "testGetContentType.pdf";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFile);

		dto.putOperation(localFile, destFile, null, null);

		IrodsSecurityManager manager = Mockito.mock(IrodsSecurityManager.class);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				"/", manager);

		WebDavConfig config = new WebDavConfig();
		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito
				.mock(IrodsFileContentService.class);

		IrodsFileResource resource = new IrodsFileResource("host", factory,
				destFile, service);

		String actual = resource.getContentType("");
		Assert.assertNotNull("null content type", actual);
		Assert.assertEquals("did not find pdf", "application/pdf", actual);
	}

	@Test
	public void testMoveToCol() throws Exception {
		// generate a local scratch file
		String testFileName = "testMoveToCol.pdf";
		String testTargetColl = "testMoveToColTarget";
		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String sourceIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

		String targetIrodsFile = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testFileName);
		String targetIrodsFileMoveTo = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl + "/" + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);
		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsFile);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsColl);
		targetCollection.mkdirs();
		IRODSFile expectedFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(targetIrodsFileMoveTo);

		dto.putOperation(localFile, destFile, null, null);

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

		IrodsFileResource resource = new IrodsFileResource("host", factory,
				destFile, service);

		IrodsDirectoryResource collectionResource = new IrodsDirectoryResource(
				"host", factory, targetCollection, service);

		resource.moveTo(collectionResource, testFileName);

		Assert.assertTrue("did not move to target file", expectedFile.exists());

	}
}
