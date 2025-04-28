
-- select create_hypertable('chart','created_at');


-- 1분 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_m1_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 minute', created_at) AS minute,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY minute, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_m1_trades',
                                       start_offset => INTERVAL '3 HOUR',
                                       end_offset => INTERVAL '1 MINUTE',
                                       schedule_interval => INTERVAL '1 MINUTE');

-- 3분 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_m3_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('3 minute', created_at) AS minute,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY minute, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_m3_trades',
                                       start_offset => INTERVAL '3 HOUR',
                                       end_offset => INTERVAL '3 MINUTE',
                                       schedule_interval => INTERVAL '3 MINUTE');

-- 5분 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_m5_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('5 minute', created_at) AS minute,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY minute, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_m5_trades',
                                       start_offset => INTERVAL '3 HOUR',
                                       end_offset => INTERVAL '5 MINUTE',
                                       schedule_interval => INTERVAL '5 MINUTE');

-- 15분 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_m15_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('15 minute', created_at) AS fifteen_minute,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY fifteen_minute, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_m15_trades',
                                       start_offset => INTERVAL '3 HOUR',
                                       end_offset => INTERVAL '15 MINUTE',
                                       schedule_interval => INTERVAL '15 MINUTE');

-- 30분 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_m30_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('30 minute', created_at) AS thirty_minute,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY thirty_minute, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_m30_trades',
                                       start_offset => INTERVAL '3 HOUR',
                                       end_offset => INTERVAL '30 MINUTE',
                                       schedule_interval => INTERVAL '30 MINUTE');

-- 1시간 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_h1_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', created_at) AS hour,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY hour, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_h1_trades',
                                       start_offset => INTERVAL '3 DAY',
                                       end_offset => INTERVAL '1 HOUR',
                                       schedule_interval => INTERVAL '1 HOUR');

-- 3시간 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_h3_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('3 hour', created_at) AS three_hour,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY three_hour, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_h3_trades',
                                       start_offset => INTERVAL '7 DAY',
                                       end_offset => INTERVAL '3 HOUR',
                                       schedule_interval => INTERVAL '3 HOUR');

-- 6시간 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_h6_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('6 hour', created_at) AS six_hour,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY six_hour, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_h6_trades',
                                       start_offset => INTERVAL '7 DAY',
                                       end_offset => INTERVAL '6 HOUR',
                                       schedule_interval => INTERVAL '6 HOUR');

-- 12시간 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_h12_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('12 hour', created_at) AS twelve_hour,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY twelve_hour, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_h12_trades',
                                       start_offset => INTERVAL '14 DAY',
                                       end_offset => INTERVAL '12 HOUR',
                                       schedule_interval => INTERVAL '12 HOUR');

-- 1일 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_d1_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 day', created_at) AS day,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY day, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_d1_trades',
                                       start_offset => INTERVAL '90 DAY',
                                       end_offset => INTERVAL '1 DAY',
                                       schedule_interval => INTERVAL '1 DAY');

-- 1주일 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_w1_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 week', created_at) AS week,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY week, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_w1_trades',
                                       start_offset => INTERVAL '365 DAY',
                                       end_offset => INTERVAL '1 WEEK',
                                       schedule_interval => INTERVAL '1 WEEK');

-- 1달 단위
CREATE MATERIALIZED VIEW if not exists BTCKRW_mon1_trades WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 month', created_at) AS month,
    pair,
    FIRST(price, created_at) AS first_price,
    last(price, created_at) AS last_price,
    MAX(price) AS max_price,
    MIN(price) AS min_price,
    SUM(amount) AS amount
FROM chart
WHERE pair = 'BTC/KRW'
GROUP BY month, pair;

SELECT add_continuous_aggregate_policy('BTCKRW_mon1_trades',
                                       start_offset => INTERVAL '3 YEAR',
                                       end_offset => INTERVAL '1 MONTH',
                                       schedule_interval => INTERVAL '1 MONTH');
