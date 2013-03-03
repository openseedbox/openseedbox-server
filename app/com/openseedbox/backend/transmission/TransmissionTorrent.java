package com.openseedbox.backend.transmission;

import com.openseedbox.backend.AbstractTorrent;
import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITracker;
import com.openseedbox.backend.TorrentState;
import com.openseedbox.backend.transmission.TransmissionPeer.TransmissionPeerFrom;
import com.openseedbox.code.Util;
import java.util.ArrayList;
import java.util.List;

public class TransmissionTorrent extends AbstractTorrent {

	private int id;
	private String name;
	private double percentDone;
	private long rateDownload;
	private long rateUpload;
	private String errorString;
	private String hashString;
	private long totalSize;
	private long downloadedEver;
	private long uploadedEver;
	private int status;
	private double metadataPercentComplete;
	private String downloadDir;
	private List<TransmissionFile> files;
	private List<Integer> wanted;
	private List<TransmissionPeer> peers;
	private TransmissionPeerFrom peersFrom;
	private List<Integer> priorities;
	private List<TransmissionTracker> trackerStats;
	
	private void fixFiles() {
		//set the id and wanted fields on each file
		//since the rpc response doesnt have them
		//but its way easier to program when they are present
		List<TransmissionFile> newList = new ArrayList<TransmissionFile>();
		for (int x = 0; x < files.size(); x++) {
			TransmissionFile f = files.get(x);
			f.setId(x);
			f.setWanted(this.wanted.get(x)); 
			f.setPriority(priorities.get(x));
			f.setTorrentHash(this.hashString);
			newList.add(f);
		}
		this.files = newList;
	}

	public String getName() {
		return name;
	}

	public double getMetadataPercentComplete() {
		return this.metadataPercentComplete;
	}

	public double getPercentComplete() {
		return this.percentDone;
	}

	public long getDownloadSpeedBytes() {
		return this.rateDownload;
	}

	public long getUploadSpeedBytes() {
		return this.rateUpload;
	}

	public String getTorrentHash() {
		return this.hashString;
	}

	public String getErrorMessage() {
		return this.errorString;
	}

	public long getTotalSizeBytes() {
		return this.totalSize;
	}

	public long getDownloadedBytes() {
		return this.downloadedEver;
	}

	public long getUploadedBytes() {
		return this.uploadedEver;
	}

	public TorrentState getStatus() {
		if (this.hasErrorOccured()) {
			return TorrentState.ERROR;
		}
		if (this.isMetadataDownloading() && status != 0) {
			return TorrentState.METADATA_DOWNLOADING;
		}
		switch(status) {
			case 0:		
				return TorrentState.PAUSED; //stopped
			case 1:
				return TorrentState.QUEUED; //waiting to check
			case 2:				
				return TorrentState.CHECKING; //checking
			case 3:				
				return TorrentState.QUEUED; //waiting to download
			case 4:
				return TorrentState.DOWNLOADING; //downloading
			case 5:				
				return TorrentState.QUEUED; //waiting to seed
			case 6:
				return TorrentState.SEEDING;
		}		
		return TorrentState.ERROR;
	}

	public List<IFile> getFiles() {
		if (this.files != null) {
			fixFiles();
			return new ArrayList<IFile>(this.files);		
		}
		return null;
	}

	public List<IPeer> getPeers() {
		if (this.peers != null) {
			return new ArrayList<IPeer>(this.peers);	
		}
		return null;
	}

	public List<ITracker> getTrackers() {
		if (this.trackerStats != null) {
			return new ArrayList<ITracker>(this.trackerStats);
		}
		return null;
	}

	public String getZipDownloadLink() {
		return String.format("/download/zip/%s?name=%s", getTorrentHash(), Util.URLEncode(getName()));
	}
	
}
