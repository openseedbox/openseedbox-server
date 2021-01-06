package com.openseedbox.backend.transmission;

import com.openseedbox.backend.ITracker;

public class TransmissionTracker implements ITracker {

	private String announce;
	private int downloadCount;
	private boolean hasAnnounced;
	private boolean hasScraped;
	private String host;
	private int id;
	private boolean isBackup;
	private int lastAnnouncePeerCount;
	private String lastAnnounceResult;
	private int lastAnnounceStartTime;
	private boolean lastAnnounceSucceeded;
	private long lastAnnounceTime;
	private boolean lastAnnounceTimedOut;
	private String lastScrapeResult;
	private long lastScrapeStartTime;
	private boolean lastScrapeSucceeded;
	private long lastScrapeTime;
	private boolean lastScrapeTimedOut;
	private int leecherCount;
	private long nextAnnounceTime;
	private long nextScrapeTime;
	private String scrape;
	private int scrapeState;
	private int seederCount;
	private int tier;

	public int getDownloadCount() {
		return this.downloadCount;
	}

	public String getHost() {
		return this.host;
	}

	public int getLeecherCount() {
		return this.leecherCount;
	}

	public int getSeederCount() {
		return this.seederCount;
	}

	public String getAnnounceUrl() {
		return this.announce;
	}

	public String getLastAnnounceResult() {
		return this.lastAnnounceResult;
	}

	public boolean isLastAnnounceSuccessful() {
		return this.lastAnnounceSucceeded;
	}
}
