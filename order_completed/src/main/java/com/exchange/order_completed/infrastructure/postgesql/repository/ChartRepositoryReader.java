package com.exchange.order_completed.infrastructure.postgesql.repository;

import com.exchange.order_completed.domain.postgresEntity.TradeDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ChartRepositoryReader {

    private final JdbcTemplate jdbcTemplate;

    public static RowMapper<TradeDataInfo> createGenericRowMapper(
            String minuteColumnName,
            String pairColumnName,
            String firstPriceColumnName,
            String lastPriceColumnName,
            String maxPriceColumnName,
            String minPriceColumnName,
            String amountColumnName) {
        return (rs, rowNum) -> {
            TradeDataInfo tradeData = new TradeDataInfo();
            tradeData.setMinute(rs.getObject(minuteColumnName, LocalDateTime.class));
            tradeData.setPair(rs.getString(pairColumnName));
            tradeData.setFirstPrice(rs.getBigDecimal(firstPriceColumnName));
            tradeData.setLastPrice(rs.getBigDecimal(lastPriceColumnName));
            tradeData.setMaxPrice(rs.getBigDecimal(maxPriceColumnName));
            tradeData.setMinPrice(rs.getBigDecimal(minPriceColumnName));
            tradeData.setAmount(rs.getBigDecimal(amountColumnName));
            return tradeData;
        };
    }

    public List<TradeDataInfo> searchDataFromView(String viewName, String timeColumnName) {

        System.out.println(jdbcTemplate.getDataSource().toString());

        String sql = "SELECT " + timeColumnName + ", pair, first_price, last_price, max_price, min_price, amount FROM " + viewName;

        RowMapper<TradeDataInfo> rowMapper = (rs, rowNum) -> {
            TradeDataInfo tradeData = new TradeDataInfo();
            tradeData.setMinute(rs.getObject(timeColumnName, LocalDateTime.class));
            tradeData.setPair(rs.getString("pair"));
            tradeData.setFirstPrice(rs.getBigDecimal("first_price"));
            tradeData.setLastPrice(rs.getBigDecimal("last_price"));
            tradeData.setMaxPrice(rs.getBigDecimal("max_price"));
            tradeData.setMinPrice(rs.getBigDecimal("min_price"));
            tradeData.setAmount(rs.getBigDecimal("amount"));
            return tradeData;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
}