package com.exchange.matching.domain.service;

import com.exchange.matching.application.enums.MatchingVersion;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Component
public class MatchingServiceRegistry {
    private final List<MatchingService> matchingServices;
    private final Map<MatchingVersion, MatchingService> serviceMap = new HashMap<>();

    public MatchingServiceRegistry(List<MatchingService> matchingServices) {
        this.matchingServices = matchingServices;
    }

    @PostConstruct
    public void init() {
        matchingServices.forEach(service ->
                serviceMap.put(service.getVersion(), service)
        );
    }

    public MatchingService getService(MatchingVersion version) {
        MatchingService service = serviceMap.get(version);
        if (service == null) {
            throw new IllegalArgumentException("No service found for version: " + version);
        }
        return service;
    }
}