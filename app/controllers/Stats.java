package controllers;

import com.openseedbox.code.Util;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns server statistics
 * @author Erin Drummond
 */
public class Stats extends BackendController {
	
	public static void index() {
		String uptime = Util.executeCommand("uptime");
		
		Map<String, String> res = new HashMap<String, String>();
		res.put("uptime", uptime);
		renderJSON(res);
	}
	
}
