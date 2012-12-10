package com.openseedbox.backend.transmission;

import com.openseedbox.code.MessageException;
import com.google.gson.*;
import com.openseedbox.backend.ITorrent;
import com.openseedbox.backend.ITorrentBackend;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.libs.WS;
import play.libs.WS.HttpResponse;

public class TransmissionBackend implements ITorrentBackend {
	

	public TransmissionConfig getConfig() {
		return null;
		/*
		Node n = getNode();
		WebRequest wr = new WebRequest(n.getIpAddress());
		TransmissionConfig ret = null;
		try {
			WebResponse res = wr.getResponse("control", getStandardParams("config"));
			ret = new Gson().fromJson(res.getResultJsonObject().get("config"), TransmissionConfig.class);
		} catch (WebRequest.WebRequestFailedException ex) {
			throw new MessageException(ex, "Unable to get transmission config for user %s on node %s (%s)",
					_account.getPrimaryUser().getEmailAddress(), n.getName(), n.getIpAddress());
		}
		return ret;*/
	}
	
	public void saveConfig(TransmissionConfig tc) {
		/*
		Node n = getNode();
		Map<String, String> params = getStandardParams("config-write");
		params.put("config", new Gson().toJson(tc));
		try {
			new WebRequest(n.getIpAddress()).postResponse("control", params);
		} catch (WebRequest.WebRequestFailedException ex) {
			throw new MessageException(ex, "Unable to store transmission config for user %s on node %s (%s)",
					_account.getPrimaryUser().getEmailAddress(), n.getName(), n.getIpAddress());
		}*/
	}
	
	private Map<String, String> getStandardParams(String action) throws MessageException {
		return null;/*
		Map<String, String> params = new HashMap<String, String>();
		User u = _account.getPrimaryUser();
		params.put("user_name", u.getEmailAddress());
//		params.put("password", _account.getTransmissionPassword());
		params.put("action", action);
//		params.put("port", String.valueOf(_account.getTransmissionPort()));
		return params;*/
	}

	public void start()  {
		/*
		Node n = getNode();
		try {
			WebResponse wr = new WebRequest(n.getIpAddress()).getResponse("control", getStandardParams("start"));
			if (!wr.isSuccessful()) {
				throw new MessageException(wr.getErrorMessage());
			}
		} catch (WebRequest.WebRequestFailedException ex) {
			throw new MessageException(ex, "Unable to start transmission for user %s on node %s (%s)",
					_account.getPrimaryUser().getEmailAddress(), n.getName(), n.getIpAddress());			
		}*/
	}

	public void stop() {
		/*
		Node n = getNode();
		try {
			WebResponse wr = new WebRequest(n.getIpAddress()).getResponse("control", getStandardParams("stop"));
			if (!wr.isSuccessful()) {
				throw new MessageException(wr.getErrorMessage());
			}
		} catch (WebRequest.WebRequestFailedException ex) {
			throw new MessageException(ex, "Unable to stop transmission for user %s on node %s (%s)",
					_account.getPrimaryUser().getEmailAddress(), n.getName(), n.getIpAddress());			
		}*/
	}
	
	public void restart() {
		/*
		Node n = getNode();
		try {
			WebResponse wr = new WebRequest(n.getIpAddress()).getResponse("control", getStandardParams("restart"));
			if (!wr.isSuccessful()) {
				throw new MessageException(wr.getErrorMessage());
			}
		} catch (WebRequest.WebRequestFailedException ex) {
			throw new MessageException(ex, "Unable to restart transmission for user %s on node %s (%s)",
					_account.getPrimaryUser().getEmailAddress(), n.getName(), n.getIpAddress());			
		}*/
	}
	
	public boolean isRunning() {
		return true;
		/*
		Node n = getNode();
		try {
			WebResponse wr = new WebRequest(n.getIpAddress())
					.getResponse("control", getStandardParams("is-running"));
			return wr.getResultBoolean();
		} catch (WebRequest.WebRequestFailedException ex) {
			throw new MessageException(ex, "Unable to check if transmission is running for user %s on node %s (%s)",
					_account.getPrimaryUser().getEmailAddress(), n.getName(), n.getIpAddress());
		}*/
	}
	
	/* RPC method wrappers */
	public TransmissionSessionStats getSessionStats() throws MessageException {
		RpcRequest req = new RpcRequest("session-stats");
		RpcResponse res = req.getResponse();
		if (res.success()) {
			return new Gson().fromJson(res.getArguments(), TransmissionSessionStats.class);
		} else {
			throw new MessageException("Unable to get session stats: " + res.getResultMessage());
		}
	}
	
	public ITorrent addTorrent(File f) {
		String name = f.getName();
		byte[] torrent;
		try {
			torrent = FileUtils.readFileToByteArray(f);
		} catch (IOException ex) {
			throw new MessageException("Unable to read torrent file!");
		}
		String contents = Base64.encodeBase64String(torrent);
		return addTorrent(name, contents, false);
	}
	
	public ITorrent addTorrent(String urlOrMagnet) {
		return addTorrent(urlOrMagnet, null, false);
	}
	
	private ITorrent addTorrent(String urlOrMagnet, String base64Contents, Boolean paused) {
		RpcRequest req = new RpcRequest("torrent-add");
		if (base64Contents != null) {
			req.addArgument("metainfo", base64Contents);
		} else {
			req.addArgument("filename", urlOrMagnet);
		}
		req.addArgument("paused", paused);
		RpcResponse res = req.getResponse();
		if (res.success()) {
			return null;
			//return new Gson().fromJson(res.getArguments().get("torrent-added"), TransmissionTorrent.class);
		} else {
			throw new MessageException("Unable to add torrent: " + res.getResultMessage());
		}
	}	
	
	public Boolean removeTorrent(String id, Boolean torrentOnly) throws MessageException {
		List<String> l = new ArrayList<String>();
		l.add(id);
		return removeTorrent(l, torrentOnly);
	}
	
	public Boolean removeTorrent(List<String> ids, Boolean torrentOnly) throws MessageException {
		RpcRequest r = new RpcRequest("torrent-remove", ids);
		r.addArgument("delete-local-data", !torrentOnly);
		RpcResponse res = r.getResponse();
		return res.successOrExcept();
	}
	
	public Boolean startTorrent(String torrentHash) throws MessageException {	
		List<String> s = new ArrayList<String>();
		s.add(torrentHash);
		return startTorrent(s);
	}
	
	public Boolean startTorrent(List<String> torrentHashes) throws MessageException {
		RpcRequest r = new RpcRequest("torrent-start");
		r.addArgument("ids", getTorrentIds(torrentHashes));
		RpcResponse res = r.getResponse();
		return res.successOrExcept();
	}
	
	public Boolean pauseTorrent(String torrentHash) throws MessageException {
		List<String> s = new ArrayList<String>();
		s.add(torrentHash);
		return pauseTorrent(s);		
	}
	
	public Boolean pauseTorrent(List<String> torrentHashes) throws MessageException {
		RpcRequest r = new RpcRequest("torrent-stop", torrentHashes);
		RpcResponse res = r.getResponse();
		return res.successOrExcept();		
	}
	
	public TransmissionTorrent getTorrent(String hashString) throws MessageException {
		return getTorrents(Arrays.asList(new String[] { hashString })).get(0);
	}
	
	public List<TransmissionTorrent> getAllTorrents() throws MessageException {
		return getTorrents(null);
	}
	
	public List<TransmissionTorrent> getTorrents(List<String> hashes) throws MessageException {
		List<String> fields = Arrays.asList(new String[] {
			"id", "name", "percentDone", "rateDownload", "rateUpload", "errorString",
			"hashString", "totalSize", "downloadedEver", "uploadedEver", "status",
			"metadataPercentComplete", "downloadDir", "files", "wanted", "peers",
			"peersFrom", "priorities", "trackerStats"
		});		
		RpcRequest req = new RpcRequest("torrent-get");
		req.addArgument("fields", fields);
		if (hashes != null) {
			req.addArgument("ids", getTorrentIds(hashes));
		}
		RpcResponse r = req.getResponse();
		JsonArray torrents = r.getArguments().getAsJsonArray("torrents");
		//Logger.info("Response: %s", torrents.toString());
		List<TransmissionTorrent> ret = new ArrayList<TransmissionTorrent>();
		Gson g = new Gson();
		for (JsonElement torrent : torrents) {
			ret.add(g.fromJson(torrent, TransmissionTorrent.class));
		}
		return ret;
	}
	
	public Boolean setFilesWanted(String torrentHash, List<String> ids, List<String> allIds) throws MessageException {
		RpcRequest rpc = new RpcRequest("torrent-set", torrentHash);
		rpc.addArgument("files-unwanted", getTorrentIds(allIds)); //unwant all files
		rpc.addArgument("files-wanted", getTorrentIds(ids));
		RpcResponse res = rpc.getResponse();
		return res.successOrExcept();
	}
	
	// priorities: high, normal, low
	public Boolean setFilePriority(String torrentHash, List<String> ids, String priority) throws MessageException {
		RpcRequest rpc = new RpcRequest("torrent-set", torrentHash);
		rpc.arguments.put(String.format("priority-%s", priority), getTorrentIds(ids));
		RpcResponse res = rpc.getResponse();
		return res.successOrExcept();
	}
	
	private List<Object> getTorrentIds(List<String> ids) {
		/* convert the List<String> into a List<Object>. Transmission-daemon wont
		 * remove torrents by id if the submitted id is a string and not an int.
		 * However, we might also want to remove torrents by hash in the same request.
		 * Therefore, we need both strings and ints to be in the ids array */		
		List<Object> actual_ids = new ArrayList<Object>();
		for (String eyedee : ids) {
			try {
				actual_ids.add(Integer.parseInt(eyedee));
			} catch (NumberFormatException ex) {
				actual_ids.add(eyedee);
			}
		}	
		return actual_ids;
	}	

	private String getTransmissionUrl() throws MessageException {
		//Node n = getNode();
		//if (n == null) { return null; }
		return null;
	//	return String.format("http://%s:%s/transmission/rpc", n.getIpAddress(), _account.getTransmissionPort());
	}
	
	public String getBackendName() {
		return "transmission-daemon";
	}
	
	public class RpcRequest {
		
		public String method;	
		public Map<String, Object> arguments = new HashMap<String, Object>();
		
		public RpcRequest() {}
		
		public RpcRequest(String method) {
			this.method = method;
		}
		
		public RpcRequest(String method, String torrentHash) {
			this(method);
			this.arguments.put("ids", getTorrentIds(Arrays.asList(new String[] { torrentHash })));
		}
		
		public RpcRequest(String method, List<String> torrentHashes) {
			this(method);
			this.arguments.put("ids", getTorrentIds(torrentHashes));
		}
		
		public void addArgument(String key, Object value) {
			this.arguments.put(key, value);
		}
		
		public RpcResponse getResponse() throws MessageException {
			String url = getTransmissionUrl();
			//Logger.info("Transmission RPC request, to url: %s", url);
			WS.WSRequest req = WS.url(url);
//			req.authenticate(_account.getPrimaryUser().emailAddress, _account.getTransmissionPassword());
			String body = new Gson().toJson(this);
			//Logger.info("Sending request: %s", body);
			req.body(body);
			WS.HttpResponse res = getResponse(req, null);
			RpcResponse rpc = new RpcResponse(res);
			return rpc;		
		}
		
		private WS.HttpResponse getResponse(WS.WSRequest req, String transmissionId) throws MessageException {
			if (!StringUtils.isEmpty(transmissionId)) {
				req.headers.put("X-Transmission-Session-Id", transmissionId);
			}
			WS.HttpResponse res = null;
			try {
				res = req.post();
			} catch (Exception ex) {
				Logger.info("Error in RPC call: %s", ex);
				if (ex.getMessage().contains("Connection refused")) {
					throw new MessageException("Unable to connect to backend transmission-daemon!");
				} else {
					throw new MessageException(ex.getMessage());
				}
			}
			if (res.getStatus() == 409) {
				String tid = res.getHeader("X-Transmission-Session-Id");
				return getResponse(req, tid);
			} else if (res.getStatus() == 401) {
				throw new MessageException("Invalid RPC credentials supplied!");
			}
			return res;
		}
	}
	
	public class RpcResponse {
		private String jsonData;
		
		public RpcResponse(HttpResponse r) {
			jsonData = r.getString();
		}
		
		public String getJsonData() {
			return jsonData;
		}
		
		public boolean successOrExcept() throws MessageException {
			boolean s = success();
			if (!s) {
				throw new MessageException(getResultMessage());
			}
			return s;
		}
		
		public boolean success() {
			return (getResultMessage().equals("success"));
		}
		
		public JsonObject getWholeResponse() {
			return new JsonParser().parse(jsonData).getAsJsonObject();
		}
		
		public JsonObject getArguments() {
			JsonObject wr = getWholeResponse();
			return wr.getAsJsonObject("arguments");
		}
		
		public String getResultMessage() {
			JsonObject ob = getWholeResponse();
			return ob.get("result").getAsString();
		}
		
		
		@Override
		public String toString() {
			if (success()) {
				return "Successful RPC response: " + jsonData;
			} else {
				return "Failed RPC Response: " + getResultMessage();
			}
		}
	}
	
}
