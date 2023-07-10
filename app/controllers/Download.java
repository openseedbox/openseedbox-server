package controllers;

import com.openseedbox.Config;
import com.openseedbox.code.Util;
import com.openseedbox.jobs.GenerateNginxManifestJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.exceptions.UnexpectedException;
import play.libs.MimeTypes;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Router.ActionDefinition;

public class Download extends BaseController {

	public static void downloadFile(String hash, String name, String type, String debug) throws IOException {
		if (type != null && type.equals("zip")) {
			downloadFileZip(hash, name);
			return;
		}
		File location = getBaseDirectory(hash);
		if (!(location.exists())) {
			notFound(String.format("Directory %s doesnt exist, it appears the download link was constructed incorrectly.", hash));
		}
		Path filePath = location.toPath().resolve(name).normalize();
		if (!filePath.startsWith(location.toPath())) {
			notFound("It appears the download link was constructed incorrectly.");
		}
		String fileName = filePath.getFileName().toString();
		if (Config.isXSendfileEnabled()) {
			filePath = Paths.get(Config.getXSendfilePath(), filePath.toString().replace(location.getPath(), hash)).normalize();
			response.setHeader("X-Accel-Charset", "utf-8");
			if (StringUtils.isEmpty(debug)) {
				setContentDispositionHeaderOnDemand(fileName);
				response.setContentTypeIfNotSet(MimeTypes.getContentType(filePath.toFile().getAbsolutePath()));
				response.setHeader(Config.getXSendfileHeader(), filePath.toString());
			} else {
				renderText("FilePath: " + filePath);
			}
		} else {
			setContentDispositionHeaderOnDemand(fileName);
			renderBinary(filePath.toFile(), fileName);
		}
	}
	
	public static void downloadZip(String hash, String name) throws IOException {
		if (StringUtils.isEmpty(hash)) {
			notFound();
		}
		File manifest = getNgxManifestForZip(hash);
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("hash", hash);
		args.put("name", name);
		args.put("type", "zip");
		ActionDefinition ad = Router.reverse("Download.downloadFile", args);
		ad.absolute();		
		if (request.secure) {
			ad = ad.secure();
		}
		String downloadLink = ad.url;
		if (manifest == null) {
			File incomplete = getNgxManifestFile(hash, false);
			if (!incomplete.exists()) {
				new GenerateNginxManifestJob(hash, incomplete, getBaseDirectory(hash)).now();
			}
			downloadLink = null;
		}
		result(Util.convertToMap(new Object[] {
			"percent-complete", Util.formatPercentage(getManifestCreationPercent(hash)),
			"hash", hash,
			"name", name,
			"download-link", downloadLink
		}));
	}

	protected static void downloadFileZip(String hash, String name) throws IOException {
		if (!Config.isNgxZipEnabled()) {
			renderText("Error: Zip files are not enabled.");
		}
		if (StringUtils.isEmpty(hash)) {
			notFound();
		}
		File baseDirectory = getBaseDirectory(hash);
		if (!baseDirectory.exists()) {
			notFound();
		}
		if (name == null) {
			name = hash;
		}		
		File manifest = getNgxManifestForZip(hash);
		if (manifest == null) {
			notFound("No manifest found for file, please generate it first.");
		}
		if (!Config.isNgxZipManifestOnly()) {
			response.setHeader("X-Archive-Files", "zip");
			setContentDispositionHeaderOnDemand(name + ".zip", true);
		}
		response.setHeader("Last-Modified", Util.getLastModifiedHeader(baseDirectory.lastModified())); //this is so nginx mod_zip will resume the zip file
		renderText(FileUtils.readFileToString(getNgxManifestForZip(hash)));
	}
	
	private static double getManifestCreationPercent(String torrentHash) throws IOException {
		if (getNgxManifestForZip(torrentHash) != null) {
			return 100d;
		}
		double filesInTorrent = FileUtils.listFiles(getBaseDirectory(torrentHash), null, true).size();
		filesInTorrent -= 1; //exclude the manifest file
		double completedFiles = FileUtils.readLines(getNgxManifestFile(torrentHash, false)).size();
		if (filesInTorrent <= 0 || completedFiles <= 0) {
			return 0d;
		}
		if (filesInTorrent == completedFiles) {
			return 100d;
		}
		return (completedFiles / filesInTorrent) * 100;		
	}
	
	private static File getNgxManifestForZip(String torrentHash) {
		File f = getNgxManifestFile(torrentHash, true);
		return (f.exists()) ? f : null;		
	}
	
	private static File getNgxManifestFile(String torrentHash, boolean isFinished) {
		String filename = (isFinished) ? "_ngx_manifest.txt" : "_ngx_manifest.incomplete";
		return new File(getBaseDirectory(torrentHash), filename);
	}
	
	private static File getBaseDirectory(String torrentHash) {
		Path complete = Paths.get(Config.getTorrentsCompletePath()).normalize();
		Path baseDirectory = complete.resolve(torrentHash).normalize();
		if (!baseDirectory.startsWith(complete)) {
			notFound("Bad hash value!");
		}
		return baseDirectory.toFile();
	}

	private static void setContentDispositionHeaderOnDemand(String fileName) {
		setContentDispositionHeaderOnDemand(fileName, false);
	}

	private static void setContentDispositionHeaderOnDemand(String fileName, Boolean always) {
		String urlEncodedFileName = Util.URLEncode(fileName);
		if (urlEncodedFileName.contains("+") || always) {
			String encoding = Http.Response.current().encoding;
			String contentDisposition = "%1$s; filename*=" + encoding + "''%2$s; filename=\"%2$s\"";
			response.setHeader("Content-Disposition", String.format(contentDisposition, "attachment", urlEncodedFileName.replace("+","%20")));
		}
	}
}
