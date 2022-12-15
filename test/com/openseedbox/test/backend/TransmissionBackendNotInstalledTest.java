package com.openseedbox.test.backend;

import com.openseedbox.Config;
import com.openseedbox.backend.transmission.TransmissionBackend;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;

public class TransmissionBackendNotInstalledTest extends UnitTest {

	private TransmissionBackend backend = new TransmissionBackendNotInstalled();
	private final static File pidFile = new File(Config.getBackendBasePath(), "daemon.pid");

	@AfterClass
	public static void tearDown() {
		pidFile.delete();
	}

	@Test(expected = RuntimeException.class)
	public void testStart() {
		backend.stop();
		assertFalse(backend.isRunning());
		// boom
		backend.start();
	}

	@Test
	public void testNoStart() {
		try {
			backend.start();
		} catch (Exception e) {

		}
		assertFalse(backend.isRunning());
		backend.stop();
		assertFalse(backend.isRunning());
	}

	@Test
	public void testNotInstalled() {
		assertFalse(backend.isInstalled());
	}

	@Test
	public void testNotRunningWithStalePid() {
		backend.stop();
		assertFalse(backend.isRunning());
		try {
			FileUtils.writeStringToFile(pidFile, "1");
			assertEquals("1", FileUtils.readFileToString(pidFile));
		} catch (IOException e) {
		}
		assertFalse(backend.isRunning());
	}

}
