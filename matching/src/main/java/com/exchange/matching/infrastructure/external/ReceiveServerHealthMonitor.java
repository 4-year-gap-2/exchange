package com.exchange.matching.infrastructure.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiveServerHealthMonitor {

    private final DiscoveryClient discoveryClient;
    private final WebClient webClient;
    private final ApplicationEventPublisher eventPublisher;

    // 현재 상태 캐싱
    private final AtomicBoolean isHealthy = new AtomicBoolean(true);
    private boolean previousStatus = true;

    // 5초마다 헬스체크
    @Scheduled(fixedDelay = 5000)
    public void checkHealth() {
        boolean currentStatus = performHealthCheck();
        isHealthy.set(currentStatus);

        // 상태가 변경되었을 때만 이벤트 발행
        if (previousStatus != currentStatus) {
            log.info("Receive 상태가 변경 완료: {} -> {}", previousStatus, currentStatus);
            eventPublisher.publishEvent(new HealthStatusChangeEvent(currentStatus));
            previousStatus = currentStatus;
        }
    }

    // 캐싱된 상태 반환
    public boolean isHealthy() {
        return isHealthy.get();
    }

    private boolean performHealthCheck() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances("RECEIVE-SERVICE");
            if (instances.isEmpty()) {
                log.warn("Receive 인스턴스틑 찾을수 없음");
                return false;
            }
            // 인스턴스 중 하나라도 정상이면 true
            for (ServiceInstance instance : instances) {
                if (checkInstance(instance)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("헬스 체크 실패", e);
            return false;
        }
    }

    private boolean checkInstance(ServiceInstance instance) {
        try {
            String healthUrl = instance.getUri() + "/actuator/health";
            ResponseEntity<Void> response = webClient.get()
                    .uri(healthUrl)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(2));
            return response != null && response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Instance {} 헬스 체크 실패: {}", instance.getInstanceId(), e.getMessage());
            return false;
        }
    }
}

