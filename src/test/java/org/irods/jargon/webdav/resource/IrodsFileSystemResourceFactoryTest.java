/**
 *
 */
package org.irods.jargon.webdav.resource;

import io.milton.http.LockManager;

import java.util.Properties;

import junit.framework.Assert;

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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Mike Conway - DICE
 *
 */
public class IrodsFileSystemResourceFactoryTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IrodsFileSystemResourceFactoryTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
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
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testResolveWhenRoot() throws Exception {
		String myPath = "/";

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.ROOT);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				manager);

		factory.setWebDavConfig(config);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		IRODSFile pathFile = factory.resolvePath(myPath);
		Assert.assertEquals("should have root", myPath,
				pathFile.getAbsolutePath());
	}

	@Test
	public void testResolvePathDirExistsWhenDirGivenConfiguredAsRoot()
			throws Exception {
		String testTargetColl = "testResolvePathDirExistsWhenRootGivenConfiguredAsRoot";

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

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
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.ROOT);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				manager);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.setWebDavConfig(config);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IRODSFile pathFile = factory.resolvePath(targetIrodsColl);
		Assert.assertEquals("did not find correct path", targetIrodsColl,
				pathFile.getAbsolutePath());
	}

	@Test
	public void testResolveUserHomeDirWhenConfiguredUserRoot() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IrodsSecurityManager manager = new IrodsSecurityManager();
		manager.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		WebDavConfig config = new WebDavConfig();
		config.setAuthScheme("STANDARD");
		config.setHost(irodsAccount.getHost());
		config.setPort(irodsAccount.getPort());
		config.setZone(irodsAccount.getZone());
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.USER_HOME);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				manager);

		factory.setWebDavConfig(config);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IRODSFile pathFile = factory.resolvePath("/");
		Assert.assertEquals("should have user home", MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount),
				pathFile.getAbsolutePath());
	}

	@Test
	public void testResolveSubdirUnderUserDirWhenConfiguredUserRoot()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testTargetColl = "testResolveSubdirUnderUserDirWhenConfiguredUserRoot";

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

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
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.USER_HOME);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				manager);

		factory.setWebDavConfig(config);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IRODSFile pathFile = factory.resolvePath(targetIrodsColl);
		Assert.assertEquals("should have dir under user home", targetIrodsColl,
				pathFile.getAbsolutePath());
	}

	@Test
	public void testResolveSubdirUnderUserDirWhenConfiguredUserRootGivingFullPath()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testTargetColl = "testResolveSubdirUnderUserDirWhenConfiguredUserRootGivingFullPath";

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

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
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.USER_HOME);
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				manager);

		factory.setWebDavConfig(config);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IRODSFile pathFile = factory.resolvePath(targetIrodsColl);
		Assert.assertEquals("should have dir under user home", targetIrodsColl,
				pathFile.getAbsolutePath());
	}

	@Test
	public void testResolveSubdirUnderProvidedDirWhenConfiguredUserRootGivingFullPath()
			throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		String testTargetColl = "testResolveSubdirUnderUserDirWhenConfiguredUserRootGivingFullPath";

		String targetIrodsColl = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
								+ testTargetColl);

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
		config.setDefaultStartingLocationEnum(DefaultStartingLocationEnum.PROVIDED);
		config.setProvidedDefaultStartingLocation(MiscIRODSUtils
				.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount));
		manager.setWebDavConfig(config);

		IrodsAuthService authService = new IrodsAuthService();
		authService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		authService.setWebDavConfig(config);
		manager.setIrodsAuthService(authService);

		IrodsFileSystemResourceFactory factory = new IrodsFileSystemResourceFactory(
				manager);

		factory.setWebDavConfig(config);
		LockManager lockManager = Mockito.mock(LockManager.class);
		factory.setLockManager(lockManager);

		factory.getSecurityManager().authenticate(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		IRODSFile pathFile = factory.resolvePath(targetIrodsColl);
		Assert.assertEquals("should have dir under user home", targetIrodsColl,
				pathFile.getAbsolutePath());
	}

}
