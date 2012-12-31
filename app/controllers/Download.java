package controllers;

import com.openseedbox.Config;
import com.openseedbox.code.Util;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.jdbc.StreamUtils;
import play.jobs.Job;
import play.mvc.Http.Response;

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
		File path = new File(Config.getTorrentsCompletePath(), hash);
		if (!path.exists()) {
			notFound();
		}
		if (name == null) {
			name = hash;
		}
		await(new StreamZipJob(path, name, response).now());
	}

	protected static class StreamZipJob extends Job {

		private File baseDirectory;
		private String zipFileName;
		private Response response;

		public StreamZipJob(File baseDirectory, String zipFileName, Response response) {
			this.baseDirectory = baseDirectory;
			this.zipFileName = zipFileName;
			this.response = response;
		}

		@Override
		public void doJob() {
			try {
				response.setContentTypeIfNotSet("application/zip");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + Util.URLDecode(zipFileName) + ".zip\"");
				ZipOutputStream zip = new ZipOutputStream(new MyOutputStream(response));
				zip.setLevel(0); //no compression, we just want it as fast as possible						
				addDirectory(zip, baseDirectory, "/");
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
				StreamUtils.copy(new BufferedInputStream(new FileInputStream(f)), zip);
				zip.closeEntry();
			}
		}
	}

	protected static class MyOutputStream extends ByteArrayOutputStream {

		private Response r;

		public MyOutputStream(Response r) {
			super(4096);
			this.r = r;
		}

		@Override
		public synchronized void write(int b) {
			super.write(b);
			if (count >= 4096) {
				flushBuffer();
			}
		}

		@Override
		public void close() throws IOException {
			flushBuffer(); //get the last few bytes
		}

		private void flushBuffer() {
			r.writeChunk(this.toByteArray());
			reset();
		}
	}
}
