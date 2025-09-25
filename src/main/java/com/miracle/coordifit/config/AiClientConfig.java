//package com.miracle.coordifit.config;
//
//import io.netty.channel.ChannelOption;
//import io.netty.handler.timeout.ReadTimeoutHandler;
//import io.netty.handler.timeout.WriteTimeoutHandler;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
//import org.springframework.web.reactive.function.client.ExchangeStrategies;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.netty.http.client.HttpClient;
//
//import java.time.Duration;
//import java.util.concurrent.TimeUnit;
//
//@Configuration
//public class AiClientConfig {
//    @Value("${app.ai.base-url:http://yolo:8000}")
//    private String baseUrl;
//
//    @Value("${app.ai.connect-timeout-ms:2000}")
//    private int connectTimeoutMs;
//
//    
//    @Value("${app.google.gemini.timeout-ms:5000}")
//    private int timeoutMs;
//
//    ExchangeFilterFunction logRequest = ExchangeFilterFunction.ofRequestProcessor(req -> {
//        // 필요 시 헤더/바디 로깅도 가능(개인정보 주의)
//        System.out.println("[AI] --> " + req.method() + " " + req.url());
//        return reactor.core.publisher.Mono.just(req);
//    });
//
//    ExchangeFilterFunction logResponse = ExchangeFilterFunction.ofResponseProcessor(res -> {
//        System.out.println("[AI] <-- " + res.statusCode());
//        return reactor.core.publisher.Mono.just(res);
//    });
//    
//    @Bean(name = "aiClient")
//    public WebClient aiClient(WebClient.Builder builder) {
//        HttpClient httpClient = HttpClient.create()
//            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
//            .responseTimeout(Duration.ofMillis(timeoutMs))
//            .doOnConnected(c -> c
//                .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
//                .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)));
//
//        ExchangeStrategies strategies = ExchangeStrategies.builder()
//            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
//            .build();
//
//        return builder
//            .baseUrl(baseUrl)
//            .clientConnector(new ReactorClientHttpConnector(httpClient))
//            .exchangeStrategies(strategies)
//            .filter(logRequest)
//            .filter(logResponse)
//            .build();
//    }
//}