package controllers;

import com.openseedbox.code.Util;

public class Backend extends Base {

	public static void start() {
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
		getBackend().restart();
		result(Util.convertToMap(new Object[] {
			"restarted", true
		}));		
	}	
	
}
