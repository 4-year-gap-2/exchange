//package com.exchange.order_completed.infrastructure.client;
//
//
//import com.exchange.order_completed.domain.postgresEntity.Chart;
//import com.exchange.order_completed.presentation.dto.TradeDataRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//@Service
//@RequiredArgsConstructor
//public class ChartClientAdapter {
//
//    private final WebClient.Builder webClientBuilder;
//
//    public void createPost(Chart chart) {
//        WebClient webClient = webClientBuilder.build();
//
//        Mono<String> response = webClient.post()
//                .uri("")
//                .bodyValue(createRequest(chart))
//                .retrieve()
//                .bodyToMono(String.class);
//
//    }
//
//    private TradeDataRequest createRequest(Chart chart) {
//        return new TradeDataRequest(
//                chart.getPair(),
//                chart.getTransactionType(),
//                chart.getPrice(),
//                chart.getAmount()
//        );
//    }
//}
