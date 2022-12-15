package com.openseedbox.test.backend;

import com.openseedbox.Config;
import com.openseedbox.backend.transmission.TransmissionBackend;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;

public class TransmissionBackendTest extends UnitTest {
	
	private TransmissionBackend backend = new TransmissionBackend();
	private final static File pidFile = new File(Config.getBackendBasePath(), "daemon.pid");

	@Before
	public void setUp() {

	}

	@AfterClass
	public static void shutDown() {
		new TransmissionBackend().stop();
	}

	@Test
	public void testInstalled() {
		assertTrue(backend.isInstalled());
	}

	@Test
	public void testStart() {
		backend.stop();
		assertFalse(backend.isRunning());
		backend.start();
		assertTrue(backend.isRunning());
	}
	
	@Test
	public void testStop() {
		backend.start();
		assertTrue(backend.isRunning());
		backend.stop();
		assertFalse(backend.isRunning());
	}
	
	@Test
	public void testRestart() {
		backend.start();
		assertTrue(backend.isRunning());
		backend.restart();
		assertTrue(backend.isRunning());
		backend.stop();
		assertFalse(backend.isRunning());
		backend.restart();
		assertTrue(backend.isRunning());
		backend.restart();
		assertTrue(backend.isRunning());
	}
	
	@Test
	public void testAddTorrentFromUrl() {
		backend.start();
		
	}
	
	@Test
	public void testAddTorrentFromMagnet() {
		backend.start();
		
	}
	
	@Test
	public void testAddTorrentFromFile() {
		backend.start();
		
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
