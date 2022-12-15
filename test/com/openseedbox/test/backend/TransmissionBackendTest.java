package com.openseedbox.test.backend;

import com.openseedbox.backend.transmission.TransmissionBackend;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

public class TransmissionBackendTest extends UnitTest {
	
	private TransmissionBackend backend = new TransmissionBackend();
	
	@Before
	public void setUp() {

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
	
}
