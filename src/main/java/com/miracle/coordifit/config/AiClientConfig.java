package com.miracle.coordifit.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class AiClientConfig {
    @Value("${app.ai.base-url:http://yolo:8000}")
    private String baseUrl;

    @Value("${app.ai.connect-timeout-ms:2000}")
    private int connectTimeoutMs;

    @Value("${app.ai.response-timeout-ms:5000}")
    private int responseTimeoutMs;

    ExchangeFilterFunction logRequest = ExchangeFilterFunction.ofRequestProcessor(req -> {
        // 필요 시 헤더/바디 로깅도 가능(개인정보 주의)
        System.out.println("[AI] --> " + req.method() + " " + req.url());
        return reactor.core.publisher.Mono.just(req);
    });

    ExchangeFilterFunction logResponse = ExchangeFilterFunction.ofResponseProcessor(res -> {
        System.out.println("[AI] <-- " + res.statusCode());
        return reactor.core.publisher.Mono.just(res);
    });
    
    @Bean
    public WebClient aiClient(WebClient.Builder builder) {
        HttpClient http = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
            .responseTimeout(Duration.ofMillis(responseTimeoutMs))
            .doOnConnected(c -> c
                .addHandlerLast(new ReadTimeoutHandler(responseTimeoutMs, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(responseTimeoutMs, TimeUnit.MILLISECONDS)));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(4 * 1024 * 1024)) // 4MB
            .build();

        return builder
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(http))
            .exchangeStrategies(strategies)
            .filter(logRequest)
            .filter(logResponse)
            .build();
    }
    
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}