package controllers;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrentBackend;
import java.io.File;

public class Check extends Base {
	
	public static void index() {
		response.contentType = "text/html";		
		writeLine("<b>Checking setup</b>");		
		checkPath(Config.getBackendBasePath());
		checkPath(Config.getTorrentsCompletePath());		
		write("Checking backend class '" + Config.getBackendClassName() + "' can be instantiated...");
		ITorrentBackend backend;
		try {
			backend = getTorrentBackend();
		} catch (Exception ex) {
			htmlError("Couldnt instantiate backend: " + ex.getMessage()); return;
		}
		htmlSuccess("it can!");
		write("Checking backend software is installed...");
		if (backend.isInstalled()) {
			htmlSuccess("it is!");
		} else {
			htmlError("it isnt. Please install the software appropriate for the backend you are using.");
		}		
	}
	
	private static void checkPath(String path) {
		File bp = new File(path);
		write("Checking path ('" + path + "') exists...");
		if (!bp.isDirectory()) {
			write("it doesnt, creating...");
			if (bp.mkdir()) {
				htmlSuccess("done.");
			} else {
				htmlError("couldnt create, probably no permission."); return;
			}
		} else {
			htmlSuccess("it does, great!");
		}			
	}
	
	protected static void htmlError(String message) {
		writeLine("<span style='color:red;'>" + message + "</span>");
	}
	
	protected static void htmlSuccess(String message) {
		writeLine("<span style='color:green;'>" + message + "</span>");
	}	
	
}
