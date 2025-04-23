package com.exchange.order_completed.domain.service;

import com.exchange.order_completed.application.TimeInterval;
import com.exchange.order_completed.application.service.TradeService;
import com.exchange.order_completed.domain.postgresEntity.TradeDataInfo;
import com.exchange.order_completed.infrastructure.postgesql.repository.ChartRepositoryReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final ChartRepositoryReader chartRepositoryReader;

    public List<TradeDataInfo> getTradeInfo(String pair, TimeInterval timeInterval) {
        String formattedPair = pair.toLowerCase().replace("/", "");
        String timeFormat = timeInterval.getShortCode();
        String viewName = String.format("%s_%s_trades", formattedPair, timeFormat);

        return chartRepositoryReader.searchDataFromView(viewName , timeInterval.getInterval());
    }
}
