package controllers;

import com.openseedbox.backend.IFile;
import com.openseedbox.backend.IPeer;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.ITracker;
import com.openseedbox.code.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import play.data.binding.As;
import play.mvc.Before;

public class Torrents extends Base {
	
	@Before
	public static void checkBackendRunning() {
		if (!getBackend().isRunning()) {
			resultError("Backend isnt running.");
		}
	}
	
	/**
	 * Adds a torrent to the backend based on a URL
	 * @param url A http, https or magnet url to the torrent
	 * @param file A file containing the raw torrent. Optional,
	 * if specified then @url should be ignored.
	 */	
	public static void add(String url, File torrent) {
		ITorrent to = null;
		if (torrent != null) {
			to = getBackend().addTorrent(torrent);
		} else if (!StringUtils.isEmpty(url)) {
			to = getBackend().addTorrent(url);
		} else {
			resultError("You must specify the 'url' or 'torrent' parameter.");	
		}		
		result(Util.convertToMap(new Object[] {
			"added", true,
			"torrent", to
		}));				
	}
	
	/**
	 * Removes a torrent or torrents from the backend based on their hashes
	 * @param hash The torrent hash
	 * @param hashes A list of torrent hashes
	 */
	public static void remove(String hash, List<String> hashes) {
		if (!StringUtils.isEmpty(hash)) {
			getBackend().removeTorrent(hash);
		} else if (hashes != null) {
			getBackend().removeTorrent(hashes);
		} else {
			resultError("You must specify the 'url' or 'torrent' parameter.");
		}
		result(Util.convertToMap(new Object[] {
			"removed", true
		}));
	}
	
	/**
	 * Lists all the torrents in the backend
	 * @param recentlyActive If true, shows only the recently used torrents instead of all of them
	 */
	public static void list(boolean recentlyActive) {
		ITorrentBackend backend = getBackend();
		List<ITorrent> torrents = (!recentlyActive)
				  ? backend.listTorrents()
				  : backend.listRecentlyActiveTorrents();
		result(torrents);
	}
	
	/**
	 * Starts a torrent or torrents in the backend
	 * @param hash The torrent hash
	 * @param hashes A list of torrent hashes
	 */
	public static void start(String hash, List<String> hashes) {
		if (hashes != null) {
			getBackend().startTorrent(hashes);
		} else if (!StringUtils.isEmpty(hash)) {
			getBackend().startTorrent(hash);
		} else {
			resultError("You must specify the 'hash' or 'hashes' parameter.");
		}
		result(Util.convertToMap(new Object[] {
			"started", true
		}));		
	}
	
	/**
	 * Stops a torrent or torrents in the backend
	 * @param hash The torrent hash
	 * @param hashes A list of torrent hashes
	 */
	public static void stop(String hash, List<String> hashes) {
		if (hashes != null) {
			getBackend().stopTorrent(hashes);
		} else if (!StringUtils.isEmpty(hash)) {
			getBackend().stopTorrent(hash);
		} else {
			resultError("You must specify the 'hash' or 'hashes' parameter.");
		}
		result(Util.convertToMap(new Object[] {
			"stopped", true
		}));			
	}
	
	/**
	 * Returns the current status of a torrent or torrents in the backend
	 * @param hash The torrent hash
	 * @param hashes A list of torrent hashes 
	 */
	public static void status(String hash, List<String> hashes) {
		List<ITorrent> statuses = new ArrayList<ITorrent>();
		if (hashes != null) {
			statuses = getBackend().getTorrentStatus(hashes);
		} else if (!StringUtils.isEmpty(hash)) {
			statuses.add(getBackend().getTorrentStatus(hash));			
		} else {
			resultError("You must specify the 'hash' or 'hashes' parameter.");
		}
		result(Util.convertToMap(new Object[] {
			"status", statuses
		}));			
	}
	
	/**
	 * Returns a list of individual files for the specified torrent or torrents
	 * in the backend
	 * @param hash The torrent hash
	 * @param hashes A list of torrent hashes 
	 */
	public static void files(String hash, List<String> hashes) {
		Map<String, List<IFile>> files = new HashMap<String, List<IFile>>();
		if (hashes != null) {
			files = getBackend().getTorrentFiles(hashes);
		} else if (!StringUtils.isEmpty(hash)) {
			files.put(hash, getBackend().getTorrentFiles(hash));
		} else {
			resultError("You must specify the 'hash' or 'hashes' parameter.");
		}
		result(Util.convertToMap(new Object[] {
			"files", files
		}));		
	}
	
	/**
	 * Returns a list of trackers for the specified torrent or torrents
	 * in the backend
	 * @param hash The torrent hash
	 * @param hashes A list of torrent hashes 
	 */	
	public static void trackers(String hash, List<String> hashes) {
		Map<String, List<ITracker>> trackers = new HashMap<String, List<ITracker>>();
		if (hashes != null) {
			trackers = getBackend().getTorrentTrackers(hashes);
		} else if (!StringUtils.isEmpty(hash)) {
			trackers.put(hash, getBackend().getTorrentTrackers(hash));
		} else {
			resultError("You must specify the 'hash' or 'hashes' parameter.");
		}
		result(Util.convertToMap(new Object[] {
			"trackers", trackers
		}));			
	}
	
	/**
	 * Returns a list of peers for the specified torrent or torrents
	 * in the backend
	 * @param hash The torrent hash
	 * @param hashes A list of torrent hashes 
	 */	
	public static void peers(String hash, List<String> hashes) {
		Map<String, List<IPeer>> peers = new HashMap<String, List<IPeer>>();
		if (hashes != null) {
			peers = getBackend().getTorrentPeers(hashes);
		} else if (!StringUtils.isEmpty(hash)) {
			peers.put(hash, getBackend().getTorrentPeers(hash));
		} else {
			resultError("You must specify the 'hash' or 'hashes' parameter.");
		}
		result(Util.convertToMap(new Object[] {
			"peers", peers
		}));			
	}
	
	/**
	 * Modifys a torrent on a per-torrent basis in the backend
	 * @param hash The torrent hash
	 * @param seedRatio The new seed ratio. 0 for infinite, -1 to ignore.
	 * @param uploadLimitBytes The new upload speed limit. 0 for infinite, -1 to ignore.
	 * @param downloadLimitBytes  The new download speed limit. 0 for infinite, -1 to ignore.
	 */
	public static void modify(String hash, Double seedRatio, Long uploadLimitBytes,
			  Long downloadLimitBytes) {
		if (!StringUtils.isEmpty(hash)) {
			getBackend().modifyTorrent(hash, seedRatio, uploadLimitBytes, downloadLimitBytes);
		} else {
			resultError("You must specify the 'hash' parameter!");
		}
		result(Util.convertToMap(new Object[] {
			"files-modified", true
		}));		
	}
	
	/**
	 * Modifys a torrents files on a per-torrent basis. Note: the
	 * @id, @priority and @wanted parameters should have the same amount
	 * of comma-separated items or the behaviour of this method is undefined.
	 * @param hash The torrent hash
	 * @param id A comma-separated list of file ids so the backend can identify the files (backend-specific)
	 * @param priority A comma-separated list of priorities (-1 for low, 0 for normal, 1 for high).
	 * Sets the download priority
	 * @param wanted  A comma-separated list of boolean strings (eg "true" or "false").
	 * Determines whether or not the backend should download this file.
	 */
	public static void modifyFiles(String hash, @As(",") List<String> id,
			  @As(",") List<Integer> priority, @As(",") List<Boolean> wanted) {
		if (!StringUtils.isEmpty(hash)) {			
			if (id != null) {
				List<IFile> files = new ArrayList<IFile>();
				for (int x = 0; x < id.size(); x++) {
					String i = id.get(x);
					boolean w = (wanted != null) ? wanted.get(x) : true;
					int p = (priority != null) ? priority.get(x) : 0;
					files.add(new IncomingTorrentFile(i, w, p));
				}
				getBackend().modifyTorrentFiles(hash, files);
			} else {
				resultError("You must supply at least one 'id'!");
			}
		} else {
			resultError("You must specify the 'hash' parameter!");
		}	
		result(Util.convertToMap(new Object[] {
			"files-modified", true
		}));			
	}
	
	protected static class IncomingTorrentFile implements IFile {
		
		private String id;
		private boolean wanted;
		private int priority;
		
		public IncomingTorrentFile(String id, boolean wanted, int priority) {
			this.id = id; this.wanted = wanted; this.priority = priority;
		}

		public String getName() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String getFullPath() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public boolean isWanted() {
			return wanted;
		}

		public boolean isCompleted() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public long getBytesCompleted() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public long getFileSizeBytes() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public int getPriority() {
			return priority;
		}

		public String getDownloadLink() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String getId() {
			return id;
		}
		
	}
	
}
