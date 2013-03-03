package com.openseedbox.jobs;

import com.openseedbox.Config;
import com.openseedbox.code.Util;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import play.Logger;

public class GenerateNginxManifestJob extends GenericJob {

	private String hash;
	private File writeTo;
	private File baseDirectory;
	
	public GenerateNginxManifestJob(String hash, File writeTo, File baseDirectory) {
		this.hash = hash;
		this.writeTo = writeTo;
		this.baseDirectory = baseDirectory;
		if (!writeTo.exists()) {
			try {
				writeTo.createNewFile();
			} catch (IOException ex) {
				//fuck off java
			}
		}
		if (!writeTo.canWrite()) {
			throw new IllegalArgumentException("writeTo must be a writable file!");
		}
	}
	
	@Override
	protected Object doGenericJob() throws Exception {
		String nginxPath = String.format("%s/%s/", Config.getNgxZipPath(), hash);
		generateNgxZipFile(baseDirectory, nginxPath, "/", "");
		File destFile = new File(baseDirectory, "_ngx_manifest.txt");
		FileUtils.moveFile(writeTo, destFile);
		FileUtils.deleteQuietly(writeTo);
		return null;
	}

	@Override
	protected void onException(Exception ex) {
		super.onException(ex);
		Logger.error(ex, "Failed to generate nginx manifest for %s", hash);
	}
		
	
	private String generateNgxZipFile(File baseDirectory, String nginxPath, String prefix, String all) throws IOException {		
		for (File f : baseDirectory.listFiles()) {
			if (f.getAbsolutePath().equals(writeTo.getAbsolutePath())) {
				continue;
			}
			if (!prefix.endsWith("/")) {
				prefix += "/";
			}			
			if (f.isDirectory()) {
				all = generateNgxZipFile(f, nginxPath, prefix + f.getName(), all);
				continue;
			}					
			String name = prefix + f.getName();
			String path = nginxPath + name;
			String crc32 = "-";
			try {
				crc32 = Long.toHexString(FileUtils.checksumCRC32(f)).toUpperCase();
			} catch (IOException ex) {
				Logger.warn(ex, "Unable to generate CRC32 for file: %s", path);
			}
			all = addNgxZipEntry(crc32, path, name, f.length(), all);
		}
		return all;
	}
	
	private String addNgxZipEntry(String crc32, String path, String name, long sizeBytes, String all) throws IOException {
		String line = String.format("%s %s %s %s\n", crc32, sizeBytes, Util.URLEncode(path), name);
		FileUtils.writeStringToFile(writeTo, line, true);	
		return all += line;
	}	
	
}
