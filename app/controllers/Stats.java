package controllers;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.code.Util;
import java.io.File;
import java.util.Map;

/**
 * Returns server statistics
 * @author Erin Drummond
 */
public class Stats extends Base {
	
	public static void index() throws Exception {
		String uptime = Util.executeCommand("uptime").trim();
		String free_space = Util.executeCommand("df -h | grep /dev/ | awk '{print $4}'");
		String total_space = Util.executeCommand("df -h | grep /dev/ | awk '{print $2}'");
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
		
		Map<String, Object> res = Util.convertToMap(new Object[] {
			"uptime", uptime,
			"free_space", free_space,
			"total_space", total_space,
			"backend-installed", installed,
			"base-dir", base_dir,
			"base-dir-writable", base_dir_writable,
			"backend-name", (backend != null) ? backend.getName() : null,
			"backend-version", (backend != null) ? backend.getVersion() : null,
			"backend-running", (backend != null) ? backend.isRunning() : false
		});
		result(res);
	}
	
}
