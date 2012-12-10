package com.openseedbox.backend.transmission;

import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITracker;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import play.mvc.Http.Request;

public class TransmissionTorrent implements ITorrent {

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
	private List<TransmissionTrackerStats> trackerStats;
	
	private void fixFiles() {
		//set the id and wanted fields on each file
		//since the rpc response doesnt have them
		//but its way easier to program when they are present
		List<TransmissionFile> newList = new ArrayList<TransmissionFile>();
		for (int x = 0; x < files.size(); x++) {
			TransmissionFile f = files.get(x);
			f.id = x;
			f.wanted = this.wanted.get(x); 
			f.priority = priorities.get(x);
			newList.add(f);
		}
		this.files = newList;
	}
	
	public boolean isComplete() {
		return (this.percentDone == 1.0);
	}
	
	public List<TreeNode> getFilesAsTree() {
		fixFiles();
		List<TreeNode> mapTree = new ArrayList<TreeNode>();
		for (TransmissionFile f : files) {
			String[] paths = f.name.split("/");
			//Logger.info("paths: %s", paths.length);
			List<TreeNode> parent = mapTree;
			for (int x = 0; x < paths.length; x++) {
				String path = paths[x];
				TreeNode n = getTreeNode(parent, path);
				TreeNode newTn;
				if (n == null) {
					newTn = new TreeNode();
					newTn.name = path;
					//only set the file on the final node so earlier nodes
					//dont keep getting overwritten when multiple files match
					if (paths.length - 1 == x) {
						newTn.file = f;
					}
					newTn.level = x;
					newTn.fullPath = getFullPath(paths, x);
					parent.add(newTn);
					//Logger.info("added:%s", newTn.name);
				} else {
					newTn = n;
				}
				Collections.sort(newTn.children);
				parent = newTn.children;
			}
		}
		return mapTree;
	}

	private TreeNode getTreeNode(List<TreeNode> t, String name) {
		for (TreeNode tn : t) {
			if (tn.name.equals(name)) {
				return tn;
			}
		}
		return null;
	}
	
	private String getFullPath(String[] path, int level) {
		return StringUtils.join(path, "/", 0, level + 1);
	}

	public String getName() {
		return name;
	}

	public boolean isRunning() {
		throw new UnsupportedOperationException("Not supported yet.");
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

	public boolean hasErrorOccured() {
		return !StringUtils.isEmpty(this.errorString);
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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<IFile> getFiles() {
		if (this.files != null) {
			fixFiles();
		}
		return new ArrayList<IFile>(this.files);		
	}

	public List<IPeer> getPeers() {
		return new ArrayList<IPeer>(this.peers);	
	}

	public List<ITracker> getTrackers() {
		return new ArrayList<ITracker>(this.trackerStats);
	}

	public class TreeNode implements Comparable {

		public String name = "";
		public TransmissionFile file = null;
		public List<TreeNode> children = new ArrayList<TreeNode>();
		public int level = 0;
		public String fullPath = "";

		@Override
		public String toString() {
			return String.format("TreeNode, name: %s, children:%s", name, children.size());
		}

		@Override
		public int compareTo(Object t) {
			if (t instanceof TreeNode) {
				TreeNode tn = (TreeNode) t;
				return this.name.compareTo(tn.name);
			}
			return -1;
		}
		
		public boolean isAnyChildWanted() {
			boolean wanted = false;
			for (TreeNode tn : this.children) {
				if (tn.file != null && tn.file.isWanted()) {
					wanted = true; break;
				}
				wanted = tn.isAnyChildWanted();
				if (wanted) { break; }
			}
			return wanted;
		}
		
		public boolean isAnyChildIncomplete() {
			boolean complete = false;
			for (TreeNode tn : this.children) {
				if (tn.file != null && !tn.file.isCompleted()) {
					complete = true; break;
				}
				complete = tn.isAnyChildIncomplete();
				if (complete) { break; }
			}			
			return complete;
		}
		
		public long getTotalSize() {
			long total = 0l;
			if (this.file != null) {
				total += this.file.length;
			} else {
				for (TreeNode child : this.children) {
					total += child.getTotalSize();
				}
			}
			return total;
		}
		
		public String getNiceTotalSize() {
			long ts = getTotalSize();
			return Util.getBestRate(ts);
		}
		
	}	

	public class TransmissionFile implements IFile {

		private int id;
		private int wanted;
		private long bytesCompleted;
		private long length;
		private int priority;
		private String name;	
		private String torrentHash;		
		
		public String getPercentComplete() {
			double percent = ((double) bytesCompleted / length) * 100;
			return String.format("%.2f", percent);
		}
		
		public boolean isWanted() {
			return (wanted == 1);
		}
		
		public String getName() {
			return this.name;
		}

		public String getFullPath() {
			return this.name;
		}

		public long getBytesCompleted() {
			return this.bytesCompleted;
		}

		public long getFileSizeBytes() {
			return this.length;
		}

		public int getPriority() {
			return this.priority;
		}		

		public boolean isCompleted() {
			return (bytesCompleted == length);
		}

		public String getDownloadLink() {
			if (StringUtils.isEmpty(torrentHash)) {
				throw new MessageException("You forgot to call setTorrentHash!");
			}
			String domain = Request.current().domain;
			try {
				return String.format("https://%s/download/%s/%s", domain,
						  URLEncoder.encode(torrentHash, "UTF-8"), URLEncoder.encode(name, "UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				//fuck off java you retarded fuck
				return "Platform doesnt support UTF-8 encoding??";
			}
		}
		
		public void setTorrentHash(String hash) {
			this.torrentHash = hash;
		}

		public String getId() {
			return "" + id;
		}
	}

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

	public class TransmissionTrackerStats implements ITracker {

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
		private int lastScrapeTimedOut;
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
	}
}
