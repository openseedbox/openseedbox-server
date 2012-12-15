package com.openseedbox;

import java.io.File;
import play.Play;

public class Config {
	
	public static String getBackendClassName() {
		return Play.configuration.getProperty("backend.class", "com.openseedbox.backend.transmission.TransmissionBackend");	
	}	
	
	public static String getBackendBasePath() {
		return Play.configuration.getProperty("backend.base.path", "/openseedbox");
	}
		
	public static String getTorrentsCompletePath() {
		return new File(Config.getBackendBasePath(), "complete").getAbsolutePath();
	}
	
	public static String getBackendDownloadScheme() {
		return Play.configuration.getProperty("backend.download.scheme", "http");
	}
	
	public static String getBackendDownloadPort() {
		return Play.configuration.getProperty("backend.download.port", "");
	}
	
}
