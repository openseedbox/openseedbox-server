package com.openseedbox.backend.transmission;

import com.google.gson.annotations.SerializedName;
import com.openseedbox.backend.ISessionStatistics;

public class TransmissionSessionStats implements ISessionStatistics {

	public int activeTorrentCount;
	public long downloadSpeed;
	public int pausedTorrentCount;
	public int torrentCount;
	public long uploadSpeed;
	@SerializedName("cumulative-stats")
	public TransmissionStats cumulativeStats;
	@SerializedName("current-stats")
	public TransmissionStats currentStats;

	public long getTotalUploadedBytes() {
		return cumulativeStats.uploadedBytes;
	}

	public long getTotalDownloadedBytes() {
		return cumulativeStats.downloadedBytes;
	}

	public long getTotalUploadSpeedBytes() {
		return uploadSpeed;
	}

	public long getTotalDownloadSpeedBytes() {
		return downloadSpeed;
	}

	public int getTorrentCount() {
		return torrentCount;
	}

	public class TransmissionStats {

		public long uploadedBytes;
		public long downloadedBytes;
		public int filesAdded;
		public int sessionCount;
		public long secondsActive;
	}
}
