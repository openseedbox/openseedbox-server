package controllers;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.code.Util;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class Backend extends Base {

	public static void start() {
		checkDownloadLocations();
		getBackend().start();
		result(Util.convertToMap(new Object[] {
			"started", true
		}));
	}
	
	public static void stop() {
		getBackend().stop();
		result(Util.convertToMap(new Object[] {
			"stopped", true
		}));		
	}
	
	public static void restart() {
		checkDownloadLocations();
		getBackend().restart();
		result(Util.convertToMap(new Object[] {
			"restarted", true
		}));		
	}	
	
	public static void cleanup() {
		ITorrentBackend backend = getBackend();
		if (!backend.isRunning()) {
			resultError("The backend must be running for cleanup to work!");
		}		
		File completeDir = new File(Config.getTorrentsCompletePath());		
		List<File> torrentsInCompleteDir = Arrays.asList(completeDir.listFiles());
		List<ITorrent> torrents = backend.listTorrents();	
		List<IOException> errorList = new ArrayList<IOException>();
		int count = 0;
		//remove any folders in 'complete' folder that do not match any of the listed torrents
		for (File f : torrentsInCompleteDir) {
			if (!f.isDirectory()) {
				FileUtils.deleteQuietly(f); //there should only be directories in the torrents complete dir, one directory per hash
				continue;
			}
			
			boolean found = false;
			for (ITorrent it : torrents) {				
				if (f.getName().toLowerCase().equals(it.getTorrentHash().toLowerCase())) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				try {
					FileUtils.deleteDirectory(f);				
				} catch (IOException ex) {
					errorList.add(ex);
				}
				count++;
			}
		}
		result(Util.convertToMap(new Object[] {
			"orphaned-directories-deleted", count,
			"errors", errorList
		}));
	}
	
}
