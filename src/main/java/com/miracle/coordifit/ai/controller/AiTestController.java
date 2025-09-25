//package com.miracle.coordifit.ai.controller;
//
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import reactor.core.publisher.Mono;
//
//@RestController
//@RequestMapping("/api/ai")
//public class AiTestController {
//    private final WebClient aiClient;
//
//    public AiTestController(@Qualifier("aiClient") WebClient aiClient) {
//        this.aiClient = aiClient;
//    }
//
//    @GetMapping("/hello")
//    public Mono<String> hello() {
//        return aiClient.get()
//            .uri("/hello")
//            .retrieve()
//            .bodyToMono(String.class);
//    }
//
//    @PostMapping("/infer")
//    public Mono<String> infer(@RequestParam String s3Url) {
//        return aiClient.post()
//            .uri("/infer")
//            .bodyValue(Map.of("s3_url", s3Url))
//            .retrieve()
//            .bodyToMono(String.class);
//    }
//}
