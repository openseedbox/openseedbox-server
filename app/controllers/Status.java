package controllers;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.backend.NodeStatus;
import com.openseedbox.code.Util;
import java.io.File;

/**
 * Returns server statistics
 * @author Erin Drummond
 */
public class Status extends Base {
	
	public static void index() throws Exception {
		String uptime = Util.executeCommand("uptime").trim();
		String free_space = Util.executeCommand("df --block-size=1 | grep /dev/ | awk '{print $4}' | head -1");
		String total_space = Util.executeCommand("df --block-size=1 | grep /dev/ | awk '{print $2}' | head -1");
		String base_dir = Config.getBackendBasePath();
		boolean base_dir_writable = new File(base_dir).canWrite();
		
		Object installed;
		ITorrentBackend backend = null;
		try {
			backend = getBackend();
			installed = backend.isInstalled();
		} catch (Exception ex) {
			installed = ex.getMessage();
		}		
				

		result(new NodeStatus(uptime, Long.parseLong(free_space), Long.parseLong(total_space), base_dir, base_dir_writable, backend));
	}
	
}
