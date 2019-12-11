package com.sc.zuul;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.exception.HystrixTimeoutException;

/**
 * 自定义回滚提供者
 * 注意：FallbackProvider是hystrix的Fallback，只有hystrix异常才会触发这个接口实现，
 * 不是所有的zuul异常都会调用这个接口。
 * @author zhangdb
 *
 */
@Component
public class MyFallbackProvider implements FallbackProvider {
	/** 日志 */
	private final Logger logger = LoggerFactory.getLogger(MyFallbackProvider.class);

	@Override
	public String getRoute() {
		// 表明为哪个微服务提供回退，* 表示所有微服务提供
		return "*";
	}

	@Override
	public ClientHttpResponse fallbackResponse(Throwable cause) {
		logger.error("错误", cause);
		// 注意，只有hystrix异常才会好触发这个接口
		if (cause instanceof HystrixTimeoutException) {
			return response(HttpStatus.GATEWAY_TIMEOUT);
		} else {
			return this.fallbackResponse();
		}
	}

	@Override
	public ClientHttpResponse fallbackResponse() {
		return this.response(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ClientHttpResponse response(final HttpStatus status) {
		return new ClientHttpResponse() {

			@Override
			public InputStream getBody() throws IOException {
				return new ByteArrayInputStream(("{\"code\":\"" + status.value() + "\",\"message\":\"服务不可用，请求稍后重试。\"}").getBytes());
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				MediaType mt = new MediaType("application", "json", Charset.forName("UTF-8"));
				headers.setContentType(mt);
				return headers;
			}

			@Override
			public HttpStatus getStatusCode() throws IOException {
				return status;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return status.value();
			}

			@Override
			public String getStatusText() throws IOException {
				return status.getReasonPhrase();
			}

			@Override
			public void close() {
			}

		};
	}

}
