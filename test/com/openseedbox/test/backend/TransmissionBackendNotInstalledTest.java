package com.openseedbox.test.backend;

import com.openseedbox.backend.transmission.TransmissionBackend;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

public class TransmissionBackendNotInstalledTest extends UnitTest {

	private TransmissionBackend backend = new TransmissionBackendNotInstalled();

	@Before
	public void setUp() {

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

}
