package org.irods.jargon.webdav.resource;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.exception.FileSizeExceedsMaximumException;
import org.irods.jargon.webdav.unittest.TestCacheMx;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.milton.http.LockManager;
import junit.framework.Assert;

public class FileContentServiceTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "FileContentServiceTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@Before
	public void before() {
		TestCacheMx.clearCache();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testDownloadLessThanMax() throws Exception {
		// generate a local scratch file
		String testFileName = "testDownloadLessThanMax.txt";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 2);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsFile);

		dto.putOperation(localFile, destFile, null, null);

		IrodsSecurityManager manager = Mockito.mock(IrodsSecurityManager.class);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		WebDavConfig config = new WebDavConfig();
		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		service.setWebDavConfig(config);
		InputStream actual = service.getFileContent(destFile, irodsAccount);
		Assert.assertNotNull(actual);

	}

	@Test(expected = FileSizeExceedsMaximumException.class)
	public void testDownloadGreaterThanMax() throws Exception {
		// generate a local scratch file
		String testFileName = "testDownloadGreaterThanMax.txt";
		long max = 1;
		long myLength = (long) 2 * 1024 * 1024 * 1024;
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, myLength);

		String targetIrodsFile = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testFileName);
		File localFile = new File(localFileName);

		// now put the file

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsFile);

		dto.putOperation(localFile, destFile, null, null);

		IrodsSecurityManager manager = Mockito.mock(IrodsSecurityManager.class);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		WebDavConfig config = new WebDavConfig();
		config.setMaxDownloadInGb(max);
		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		service.setWebDavConfig(config);
		service.getFileContent(destFile, irodsAccount);

	}

}
