package controllers;

import com.openseedbox.backend.ITorrentBackend;
import com.openseedbox.code.MessageException;
import play.Play;
import play.mvc.Before;

/**
 * A base class for torrent backend modules
 * @author Erin Drummond
 */
public class BackendController extends BaseController {
	
	@Before
	public static void checkBackendRunning() {
		if (!getBackend().isRunning()) {
			notFound("Backend isnt running.");
		}
	}
	
	@Before
	public static void checkApiKey() {
		String allowedKey = Play.configuration.getProperty("backend.api_key");
		String currentKey = getApiKey() != null ? getApiKey() : "";
		if (!currentKey.equals(allowedKey)) {
			forbidden("Invalid API key");
		}
	}
	
	protected static ITorrentBackend getBackend() {
		throw new MessageException("getBackend() needs to be overridden!");
	}	
	
	protected static String getApiKey() {
		return request.params.get("api_key");
	}	
}
