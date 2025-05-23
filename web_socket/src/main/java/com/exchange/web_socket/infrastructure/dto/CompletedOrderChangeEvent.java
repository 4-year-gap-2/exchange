package com.exchange.web_socket.infrastructure.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CompletedOrderChangeEvent {

    private Long ts_ms;
    private String op;
    private Source source;
    private After after;

    @Getter
    @Setter
    public static class Source {
        private String version;
        private String connector;
        private String name;
        private Long ts_ms;
        private String snapshot;
        private String db;
        private Object sequence;
        private Long ts_us;
        private Long ts_ns;
        private String cluster;
        private String file;
        private Integer pos;
        private String keyspace;
        private String table;
    }

    @Getter
    @Setter
    public static class After {
        private UUIDValueWrapper user_id;
        private IntValueWrapper shard;
        private IntValueWrapper year_month_date;
        private UUIDValueWrapper idempotency_id;
        private LongValueWrapper created_at;
        private UUIDValueWrapper order_id;
        private StringValueWrapper order_type;
        private BigDecimalValueWrapper price;
        private BigDecimalValueWrapper quantity;
        private StringValueWrapper trading_pair;
        private Object _range_start;
        private Object _range_end;
    }

    @Getter
    @Setter
    public static class UUIDValueWrapper {
        private UUID value;
        private Object deletion_ts;
        private Boolean set;
    }

    @Getter
    @Setter
    public static class IntValueWrapper {
        private Integer value;
        private Object deletion_ts;
        private Boolean set;
    }

    @Getter
    @Setter
    public static class LongValueWrapper {
        private Long value;
        private Object deletion_ts;
        private Boolean set;
    }

    @Getter
    @Setter
    public static class BigDecimalValueWrapper {
        private BigDecimal value;
        private Object deletion_ts;
        private Boolean set;
    }

    @Getter
    @Setter
    public static class StringValueWrapper {
        private String value;
        private Object deletion_ts;
        private Boolean set;
    }
}
