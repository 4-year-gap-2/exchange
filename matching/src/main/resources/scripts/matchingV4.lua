-- 주문 정보 파싱 함수
local function parseOrderDetails(details)
    local pos = {}
    local val = {}
    local start = 1

    for i = 1, 3 do
        pos[i] = string.find(details, "|", start, true)
        val[i] = string.sub(details, start, pos[i] - 1)
        start = pos[i] + 1
    end

    val[4] = string.sub(details, start)

    return {
        timestamp = val[1],
        quantity = tonumber(val[2]),
        userId = val[3],
        orderId = val[4]
    }
end

-- 주문 정보 구성 함수
local function buildOrderDetails(timestamp, quantity, userId, orderId)
    return timestamp .. "|" .. quantity .. "|" .. userId .. "|" .. orderId
end

-- 주문 매칭 Lua 스크립트
-- KEYS[1]: 반대 주문 키 (매수면 SELL_ORDER_KEY, 매도면 BUY_ORDER_KEY)
-- KEYS[2]: 현재 주문 키 (매수면 BUY_ORDER_KEY, 매도면 SELL_ORDER_KEY)
-- ARGV[1]: 주문 타입 (BUY 또는 SELL)
-- ARGV[2]: 주문 가격
-- ARGV[3]: 주문 수량
-- ARGV[4]: 주문 상세 정보 (timestamp|quantity|userId|orderId 형식)

local oppositeOrderKey = KEYS[1]
local currentOrderKey = KEYS[2]
local orderType = ARGV[1]
local orderPrice = tonumber(ARGV[2])
local orderQuantity = tonumber(ARGV[3])
local orderDetails = ARGV[4]

-- 미리 계산된 상수
local isBuy = orderType == "BUY"
local oppositeType = isBuy and "SELL" or "BUY"

-- 반대 주문 가져오기
local oppositeOrders = redis.call(isBuy and "ZRANGE" or "ZREVRANGE", oppositeOrderKey, 0, 0, "WITHSCORES")

-- 반대 주문이 없는 경우: 빠른 종료
if #oppositeOrders == 0 then
    redis.call("ZADD", currentOrderKey, orderPrice, orderDetails)
    return {"false", "", "", "", ""}
end

-- 반대 주문 정보 처리
local oppositeOrderDetails = oppositeOrders[1]
local oppositeOrderPrice = tonumber(oppositeOrders[2])

-- 가격 조건 확인
local isPriceMatched = isBuy and (orderPrice >= oppositeOrderPrice) or (not isBuy and orderPrice <= oppositeOrderPrice)
if not isPriceMatched then
    redis.call("ZADD", currentOrderKey, orderPrice, orderDetails)
    return {"false", "", "", "", ""}
end

-- 양쪽 주문 정보 파싱
local oppositeOrder = parseOrderDetails(oppositeOrderDetails)
local currentOrder = parseOrderDetails(orderDetails)

-- 매칭 가능한 수량 계산
local matchedQuantity = math.min(orderQuantity, oppositeOrder.quantity)
local remainingOppositeQuantity = oppositeOrder.quantity - matchedQuantity
local remainingOrderQuantity = orderQuantity - matchedQuantity

-- 매칭 가격 결정 (반대 주문의 가격)
local matchPrice = oppositeOrderPrice

-- 항상 먼저 반대 주문 정보 제거
redis.call("ZREM", oppositeOrderKey, oppositeOrderDetails)

-- 결과 관련 변수 초기화
local updatedOppositeDetails = ""
local updatedCurrentDetails = ""

-- 반대 주문에 남은 수량이 있는 경우
if remainingOppositeQuantity > 0 then
    updatedOppositeDetails = buildOrderDetails(
            oppositeOrder.timestamp,
            remainingOppositeQuantity,
            oppositeOrder.userId,
            oppositeOrder.orderId
    )
    redis.call("ZADD", oppositeOrderKey, oppositeOrderPrice, updatedOppositeDetails)
end

-- 현재 주문에 남은 수량이 있는 경우
if remainingOrderQuantity > 0 then
    updatedCurrentDetails = buildOrderDetails(
            currentOrder.timestamp,
            remainingOrderQuantity,
            currentOrder.userId,
            currentOrder.orderId
    )
end

-- 결과 데이터 (필수 정보만 단순한 리스트로 반환)
return {
    "true",                            -- 1. 매칭 성공 여부
    oppositeOrderDetails,              -- 2. 원본 반대 주문 정보
    tostring(oppositeOrderPrice),      -- 3. 반대 주문 가격 (체결 가격)
    tostring(matchedQuantity),         -- 4. 체결 수량
    tostring(remainingOrderQuantity),  -- 5. 현재 주문 남은 수량
}