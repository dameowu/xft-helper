package com.unimeowu.xft.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;

class HttpInfo {
	private Map<String, String> headers = new HashMap<String, String>();
	private String url;
	private byte []body;
	public String getHeaders() {
		return JSON.toJSONString(headers);
	}
	public HttpInfo setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}
	public String getUrl() {
		return url;
	}
	public HttpInfo setUrl(String url) {
		this.url = url;
		return this;
	}
	public byte[] getBody() {
		return body;
	}
	public HttpInfo setBody(byte []body) {
		this.body = body;
		return this;
	}
}