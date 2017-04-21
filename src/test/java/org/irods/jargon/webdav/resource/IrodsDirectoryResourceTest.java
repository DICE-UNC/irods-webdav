package org.irods.jargon.webdav.resource;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.webdav.authfilter.IrodsAuthService;
import org.irods.jargon.webdav.config.DefaultStartingLocationEnum;
import org.irods.jargon.webdav.config.WebDavConfig;
import org.irods.jargon.webdav.unittest.TestCacheMx;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.milton.http.LockManager;
import io.milton.principal.Principal;
import io.milton.resource.AccessControlledResource.Priviledge;
import io.milton.resource.Resource;

public class IrodsDirectoryResourceTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsDirectoryResourceTest";
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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Before
	public void before() {
		TestCacheMx.clearCache();
	}

	@Test
	public void testCreateCollection() throws Exception {
		String testTargetColl = "testCreateCollection";

		String rootColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH);

		String targetIrodsColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(rootColl);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsColl);

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito.mock(IrodsFileContentService.class);

		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, rootCollection, service);

		resource.createCollection(testTargetColl);

		Assert.assertTrue("did not create subcoll", targetCollection.exists());
	}

	@Test
	public void testGetChild() throws Exception {
		String testTargetColl = "testGetChild";

		String rootColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH);

		String targetIrodsColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(rootColl);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsColl);

		targetCollection.mkdirs();

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito.mock(IrodsFileContentService.class);
		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, rootCollection, service);

		Resource child = resource.child(testTargetColl);

		Assert.assertEquals("did not find target col", testTargetColl, child.getName());
	}

	@Test
	public void testGetChildrenViaFile() throws Exception {
		String testTargetColl = "testGetChildrenViaFile";

		int count = 3;

		String targetIrodsColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsColl);

		targetCollection.mkdirs();

		String myTarget = "";
		IRODSFile irodsFile;
		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsColl + "/f" + (10000 + i) + ".txt";
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
		}

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsColl + "/c" + (10000 + i);
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.mkdirs();
		}

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setCacheFileDemographics(false);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = Mockito.mock(IrodsFileContentService.class);
		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, targetCollection, service);

		List<? extends Resource> actual = resource.getChildren();
		Assert.assertFalse("no children returned", actual.isEmpty());

	}

	@Test
	public void testGetChildrenWithCacheUnderRoot() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile("/");

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setCacheFileDemographics(true);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);
		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsFileContentService service = Mockito.mock(IrodsFileContentService.class);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, targetCollection, service);

		List<? extends Resource> actual = resource.getChildren();
		Assert.assertFalse("no children returned", actual.isEmpty());

	}

	@Test
	public void testGetChildrenWithCache() throws Exception {
		String testTargetColl = "testGetChildrenWithCache";

		int count = 3;

		String targetIrodsColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + '/' + testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile targetCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(targetIrodsColl);

		targetCollection.mkdirs();

		String myTarget = "";
		IRODSFile irodsFile;
		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsColl + "/f" + (10000 + i) + ".txt";
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.createNewFile();
		}

		for (int i = 0; i < count; i++) {
			myTarget = targetIrodsColl + "/c" + (10000 + i);
			irodsFile = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(myTarget);
			irodsFile.mkdirs();
		}

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setCacheFileDemographics(true);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);
		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsFileContentService service = Mockito.mock(IrodsFileContentService.class);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, targetCollection, service);

		List<? extends Resource> actual = resource.getChildren();
		Assert.assertFalse("no children returned", actual.isEmpty());

	}

	@Test
	public void testCreateNewNormalStream() throws Exception {
		String testFileName = "testCreateNewNormalStream.txt";

		String rootColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(rootColl);

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setUsePackingStreams(false);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		service.setWebDavConfig(config);
		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, rootCollection, service);

		long fileLength = 100L;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);
		FileInputStream fileInputStream = new FileInputStream(localFile);

		resource.createNew(testFileName, fileInputStream, fileLength, "text/html");

	}

	@Test
	public void testCreateNewPackingStream() throws Exception {
		String testFileName = "testCreateNewPackingStream.txt";

		String rootColl = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(testingProperties,
				IRODS_TEST_SUBDIR_PATH);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(rootColl);

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setUsePackingStreams(true);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);
		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		service.setWebDavConfig(config);
		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, rootCollection, service);

		long fileLength = 100L;

		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFilePath = org.irods.jargon.testutils.filemanip.FileGenerator
				.generateFileOfFixedLengthGivenName(absPath, testFileName, fileLength);
		File localFile = new File(localFilePath);
		FileInputStream fileInputStream = new FileInputStream(localFile);

		resource.createNew(testFileName, fileInputStream, fileLength, "text/html");

	}

	@Test
	public void testGetAccessControlList() throws Exception {
		String testFileName = "testGetAccessControlList";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount));

		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(rootCollection.getAbsolutePath(), testFileName);
		destFile.deleteWithForceOption();

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.USER_HOME);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);

		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());

		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, rootCollection, service);

		resource.createCollection(testFileName);
		Map<Principal, List<Priviledge>> actual = resource.getAccessControlList();
		Assert.assertNotNull("no access control list found", actual);

		Object[] keys = actual.keySet().toArray();
		List<Priviledge> privs = actual.get(keys[0]);
		Assert.assertFalse("no priv", privs.isEmpty());
		Priviledge priv = privs.get(0);
		Assert.assertEquals("priv is not all", priv, Priviledge.ALL);

	}

	@Test
	public void testGetPrincipalURL() throws Exception {
		String testFileName = "testGetPrincipalURL";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount));

		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(rootCollection.getAbsolutePath(), testFileName);
		destFile.deleteWithForceOption();

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.USER_HOME);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);

		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());

		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, rootCollection, service);

		resource.createCollection(testFileName);
		String url = resource.getPrincipalURL();
		Assert.assertNotNull("no url returned", url);
		Assert.assertFalse("no url", url.isEmpty());
	}

	@Test
	public void testCreateCollectionUnderUserHome() throws Exception {
		String testFileName = "testCreateCollectionUnderUserHome";

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSFile rootCollection = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount));

		IRODSFile destFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(rootCollection.getAbsolutePath(), testFileName);
		destFile.deleteWithForceOption();

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.USER_HOME);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(manager);

		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		IrodsFileContentService service = new IrodsFileContentService();
		service.setIrodsAccessObjectFactory(irodsFileSystem.getIRODSAccessObjectFactory());

		authService.authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(), irodsAccount.getPassword());

		IrodsDirectoryResource resource = new IrodsDirectoryResource("host", factory, rootCollection, service);

		resource.createCollection(testFileName);

		Assert.assertTrue(destFile.exists());

	}

}
