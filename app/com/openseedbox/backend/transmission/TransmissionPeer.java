package com.openseedbox.backend.transmission;

import com.openseedbox.backend.IPeer;

public class TransmissionPeer implements IPeer {

	private String address;
	private String clientName;
	private boolean clientIsChoked;
	private boolean clientIsInterested;
	private String flagStr;
	private boolean isDownloadingFrom;
	private boolean isEncrypted;
	private boolean isIncoming;
	private boolean isUploadingTo;
	private boolean isUTP;
	private boolean peerIsChoked;
	private boolean peerIsInterested;
	private int port;
	private double progress;
	private long rateToClient;
	private long rateToPeer;

	public String getClientName() {
		return this.clientName;
	}

	public boolean isDownloadingFrom() {
		return this.isDownloadingFrom;
	}

	public boolean isUploadingTo() {
		return this.isUploadingTo;
	}

	public boolean isEncryptionEnabled() {
		return this.isEncrypted;
	}

	public long getDownloadRateBytes() {
		return this.rateToClient;
	}

	public long getUploadRateBytes() {
		return this.rateToPeer;
	}

	public String getIpAddress() {
		return address;
	}

	public class TransmissionPeerFrom {

		public int fromCache;
		public int fromDht;
		public int fromIncoming;
		public int fromLpd;
		public int fromLtep;
		public int fromPex;
		public int fromTracker;
	}
}
