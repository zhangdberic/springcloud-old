package com.sc.feign.domain;

public class UploadInfo {

	private String name;

	private String originalFilename;

	private long size;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "UploadInfo [name=" + name + ", originalFilename=" + originalFilename + ", size=" + size + "]";
	}

}
