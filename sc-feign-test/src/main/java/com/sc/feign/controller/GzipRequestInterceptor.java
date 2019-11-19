package com.sc.feign.controller;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.encoding.HttpEncoding;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class GzipRequestInterceptor implements   RequestInterceptor {
	
	private final Logger logger = LoggerFactory.getLogger(GzipRequestInterceptor.class);

	@Override
	public void apply(RequestTemplate template) {
		Map<String, Collection<String>> headers = template.headers();
		if (headers.containsKey(HttpEncoding.CONTENT_ENCODING_HEADER)) {
			Collection<String> values = headers.get(HttpEncoding.CONTENT_ENCODING_HEADER);
			if(values.contains(HttpEncoding.GZIP_ENCODING)) {
				logger.info("request gzip wrapper.");
				ByteArrayOutputStream gzipedBody = new ByteArrayOutputStream();
				try {
					GZIPOutputStream gzip = new GZIPOutputStream(gzipedBody);
					gzip.write(template.body());
					gzip.flush();
					gzip.close();
					template.body(gzipedBody.toByteArray(), Charset.defaultCharset());
				}catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}



}
