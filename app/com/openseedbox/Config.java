package com.openseedbox;

import java.io.File;
import play.Play;
import play.mvc.Http.Request;

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
	
	public static boolean isXSendfileEnabled() {
		String enabled = Play.configuration.getProperty("backend.download.xsendfile", "false");
		return Boolean.parseBoolean(enabled);
	}
	
	public static String getXSendfileHeader() {
		return Play.configuration.getProperty("backend.download.xsendfile.header", "X-Sendfile");
	}
	
	public static String getXSendfilePath() {
		return Play.configuration.getProperty("backend.download.xsendfile.path", "/protected");
	}
	
	public static String getServerBase() {
		String domain = Request.current().domain;
		String port = Request.current().port.toString();
		return String.format("%s://%s:%s", Config.getBackendDownloadScheme(), domain, port);
	}
	
	public static String getPeerBlocklistUrl() {
		return Play.configuration.getProperty("backend.blocklist.url");
	}
	
}
