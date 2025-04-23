package com.exchange.order_completed.application;

public enum TimeInterval {
    ONE_MINUTE("m1","minute"),
    THREE_MINUTES("m3","minute"),
    FIVE_MINUTES("m5","minute"),
    FIFTEEN_MINUTES("m15","fifteen_minute"),
    THIRTY_MINUTES("m30","thirty_minute"),
    ONE_HOUR("h1","one_hour"),
    SIX_HOURS("h6","six_hour"),
    TWELVE_HOURS("h12","twelve_hour"),
    ONE_WEEK("w1","one_week"),
    ONE_MONTH("mon1","month"); // 예시

    private final String shortCode;
    private final String interval;


    TimeInterval(String shortCode, String interval) {
        this.shortCode = shortCode;
        this.interval = interval;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getInterval() {
        return interval;
    }

    public static String toShortCode(TimeInterval interval) {
        if (interval == null) {
            return null;
        }
        return interval.getShortCode();
    }

    // 필요하다면 역방향 변환 메서드도 추가 가능
    public static TimeInterval fromShortCode(String shortCode) {
        for (TimeInterval interval : TimeInterval.values()) {
            if (interval.shortCode.equalsIgnoreCase(shortCode)) {
                return interval;
            }
        }
        return null; // 또는 예외 처리
    }

    public static String convertToShortCode(String enumValue) {
        try {
            TimeInterval interval = TimeInterval.valueOf(enumValue.toUpperCase().replace("_", ""));
            return interval.getShortCode();
        } catch (IllegalArgumentException e) {
            return null; // 또는 예외 처리
        }
    }
}