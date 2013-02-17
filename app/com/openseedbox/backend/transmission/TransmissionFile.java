package com.openseedbox.backend.transmission;

import com.openseedbox.backend.IFile;
import com.openseedbox.code.MessageException;
import com.openseedbox.code.Util;
import org.apache.commons.lang.StringUtils;
import org.h2.store.fs.FileUtils;

public class TransmissionFile implements IFile {

	private int id;
	private int wanted;
	private long bytesCompleted;
	private long length;
	private int priority;
	private String name;
	private String torrentHash;

	public double getPercentComplete() {
		return ((double) bytesCompleted / length);		
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setWanted(int wanted) {
		this.wanted = wanted;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public boolean isWanted() {
		return (wanted == 1);
	}

	public String getName() {
		return FileUtils.getName(this.name);
	}

	public String getFullPath() {
		return this.name;
	}

	public long getBytesCompleted() {
		return this.bytesCompleted;
	}

	public long getFileSizeBytes() {
		return this.length;
	}

	public int getPriority() {
		return this.priority;
	}

	public boolean isCompleted() {
		return (bytesCompleted == length);
	}

	public String getDownloadLink() {
		if (StringUtils.isEmpty(torrentHash)) {
			throw new MessageException("You forgot to call setTorrentHash!");
		}		
		return String.format("/download/%s?name=%s", torrentHash, Util.URLEncode(name));
	}

	public void setTorrentHash(String hash) {
		this.torrentHash = hash;
	}

	public String getId() {
		return "" + id;
	}
}
