package controllers;

import com.openseedbox.Config;
import com.openseedbox.backend.ITorrentBackend;
import play.Play;
import play.mvc.Before;

public abstract class Base extends BaseController {
			
	@Before
	public static void checkApiKey() {
		String allowedKey = Play.configuration.getProperty("backend.base.api_key");
		String currentKey = getApiKey() != null ? getApiKey() : "";
		if (!currentKey.equals(allowedKey)) {
			forbidden("Invalid API key");
		}
	}
	
	protected static String getApiKey() {
		return request.params.get("api_key");
	}	
	
	protected static ITorrentBackend getBackend() {	
		String backendClass = Config.getBackendClassName();
		try {
			return getTorrentBackend();
		}	catch (ClassNotFoundException ex) {			
			resultError("Unable to find backend class: " + backendClass);
		} catch (InstantiationException ex) {
			resultError("Unable to instantiate backend class: " + backendClass);
		} catch (IllegalAccessException ex) {
			resultError("Unable to instantiate backend class: " + backendClass);
		}
		return null;
	}
	
	protected static ITorrentBackend getTorrentBackend() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class c = Class.forName(Config.getBackendClassName());
		return (ITorrentBackend) c.newInstance();			
	}
}
