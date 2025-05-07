package com.exchange.order_completed.infrastructure.postgesql.repository;

import com.exchange.order_completed.domain.postgres.entity.TradeDataInfo;

import java.util.List;

public interface ChartRepositoryReader {
    List<TradeDataInfo> searchDataFromView(String viewName, String timeColumnName);
}
