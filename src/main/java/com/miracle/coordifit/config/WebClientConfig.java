package com.miracle.coordifit.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
	@Value("${app.google.gemini.base-url}")
	private String baseUrl;
	@Value("${app.google.gemini.timeout-ms}")
	private int timeoutMs;

	@Bean(name = "geminiWebClient")
	public WebClient geminiWebClient(WebClient.Builder builder) {
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
			.responseTimeout(Duration.ofMillis(timeoutMs))
			// @formatter:off
			.doOnConnected(c -> c
				.addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
				.addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
			);
			// @formatter:on
		return builder
			.baseUrl(baseUrl)
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.exchangeStrategies(ExchangeStrategies.builder()
				.codecs(cfg -> cfg
					.defaultCodecs()
					.maxInMemorySize(16 * 1024 * 1024))
				.build())
			.build();
	}
}
