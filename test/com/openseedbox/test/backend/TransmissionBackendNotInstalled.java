package com.openseedbox.test.backend;

import com.openseedbox.backend.transmission.TransmissionBackend;

public class TransmissionBackendNotInstalled extends TransmissionBackend {

	public TransmissionBackendNotInstalled() {
		binaryName = "non-existing-" + super.binaryName;
	}
}
