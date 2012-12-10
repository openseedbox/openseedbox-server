package controllers;

import com.openseedbox.backend.ITorrent;
import com.openseedbox.code.Util;
import java.io.File;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.mvc.Before;

public class Torrents extends Base {
	
	@Before
	public static void checkBackendRunning() {
		if (!getBackend().isRunning()) {
			notFound("Backend isnt running.");
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
		if (StringUtils.isEmpty(url)) {
			if (torrent != null) {
				to = getBackend().addTorrent(torrent);
			}	
		} else {
			to = getBackend().addTorrent(url);
		}
		if (to != null) {					
			result(Util.convertToMap(new Object[] {
				"added", true,
				"torrent", to
			}));
		}
		resultError("You must specify the 'url' or 'torrent' parameter.");
	}
	
	/**
	 * Removes a torrent or torrents from the backend based on their hashes
	 * @param hash The torrent hash (should be ignored if @hashes isnt null)
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
	
	
	public static void list() {
		List<ITorrent> torrents = getBackend().listTorrents();
		result(torrents);
	}
	
}
