package controllers;

import com.openseedbox.Config;
import com.openseedbox.code.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.jdbc.StreamUtils;

public class Download extends BaseController {

	public static void downloadFile(String hash, String name, String type) {
		if (type != null && type.equals("zip")) {
			downloadFileZip(hash, name);
			return;
		}
		String location = Config.getTorrentsCompletePath();
		if (!(new File(location).exists())) {
			notFound("File doesnt exist, it appears the download link was constructed incorrectly.");
		}
		if (Config.isXSendfileEnabled()) {			
			location = Config.getXSendfilePath();
		}		
		String filePath = String.format("%s/%s/%s", location, hash, Util.URLDecode(name));				
		if (Config.isXSendfileEnabled()) {						
			String fileName = new File(filePath).getName();			
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			response.setHeader(Config.getXSendfileHeader(), filePath);
		} else {
			File f = new File(filePath);		
			renderBinary(f, Util.URLDecode(f.getName()));
		}
	}
	
	protected static void downloadFileZip(String hash, String name) {
		if (StringUtils.isEmpty(hash)) {
			notFound();
		}
		File path = new File(Config.getTorrentsCompletePath() + "/" + hash);
		if (!path.exists()) {
			notFound();
		}
		if (name == null) {
			name = hash;
		}
		try {
			response.setContentTypeIfNotSet("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=" + Util.URLDecode(name) + ".zip");
			ZipOutputStream zip = new ZipOutputStream(response.out);
			zip.setLevel(0); //no compression, we just want it as fast as possible						
			addDirectory(zip, path, "/");
			zip.close();
		} catch (IOException ex) {
			error("Unable to create zip file!");			
		}		
	}
	
	private static void addDirectory(ZipOutputStream zip, File directory, String prefix) throws IOException {
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {				
				addDirectory(zip, f, prefix + f.getName() + "/");				
				continue;
			}			
			zip.putNextEntry(new ZipEntry(prefix + f.getName()));
			StreamUtils.copy(new FileInputStream(f), zip);
			zip.closeEntry();
		}			
	}
	
}
