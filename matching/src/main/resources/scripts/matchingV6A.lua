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

-- 맵을 평면화하는 함수 (Redis XADD 명령에 사용)
local function flattenMap(map)
    local result = {}
    for k, v in pairs(map) do
        table.insert(result, k)
        table.insert(result, v)
    end
    return result
end

-- 주문 정보 구성 함수
local function buildOrderDetails(timestamp, quantity, userId, orderId)
    return timestamp .. "|" .. quantity .. "|" .. userId .. "|" .. orderId
end

-- 주문 매칭 Lua 스크립트
-- KEYS[1]: 반대 주문 키 (매수면 SELL_ORDER_KEY, 매도면 BUY_ORDER_KEY)
-- KEYS[2]: 현재 주문 키 (매수면 BUY_ORDER_KEY, 매도면 SELL_ORDER_KEY)
-- KEYS[3]: 매칭 Stream 키 (v6a:stream:matches)
-- KEYS[4]: 미체결 Stream 키 (v6a:stream:unmatched)
-- KEYS[5]: 멱등성 체크를 위한 키 (v6a:idempotency:orders)
-- ARGV[1]: 주문 타입 (BUY 또는 SELL)
-- ARGV[2]: 주문 가격
-- ARGV[3]: 주문 수량
-- ARGV[4]: 주문 상세 정보 (timestamp|quantity|userId|orderId 형식)
-- ARGV[5]: 거래 쌍 (trading pair)
-- ARGV[6]: 주문 ID
-- ARGV[7]: 부분 체결을 위한 ID

local oppositeOrderKey = KEYS[1]
local currentOrderKey = KEYS[2]
local matchStreamKey = KEYS[3]
local unmatchStreamKey = KEYS[4]
local partialMatchedStreamKey = KEYS[5]
local idempotencyKey = KEYS[6]
local orderType = ARGV[1]
local orderPrice = tonumber(ARGV[2])
local orderQuantity = tonumber(ARGV[3])
local orderDetails = ARGV[4]
local tradingPair = ARGV[5]
local orderId = ARGV[6]
local partialOrderId = ARGV[7]

-- 멱등성 체크: 이미 처리된 주문인지 확인
if redis.call("SISMEMBER", idempotencyKey, orderId) == 1 then
    -- 이미 처리된 주문이면 early return
    return true
end

-- 미리 계산된 상수
local isBuy = orderType == "BUY"

-- 반대 주문 가져오기
local oppositeOrders = redis.call(isBuy and "ZRANGE" or "ZREVRANGE", oppositeOrderKey, 0, 0, "WITHSCORES")

-- 반대 주문이 없는 경우: 미체결 주문으로 처리
if #oppositeOrders == 0 then
    -- 현재 주문을 저장
    redis.call("ZADD", currentOrderKey, orderPrice, orderDetails)

    -- 주문 정보 파싱
    local orderInfo = parseOrderDetails(orderDetails)

    -- 미체결 Stream에 발행
    local unmatchFields = {
        ["orderId"] = orderId,
        ["userId"] = orderInfo.userId,
        ["tradingPair"] = tradingPair,
        ["orderType"] = orderType,
        ["price"] = tostring(orderPrice),
        ["quantity"] = tostring(orderQuantity),
        ["timestamp"] = orderInfo.timestamp,
        ["status"] = "UNMATCHED"
    }
    redis.call("XADD", unmatchStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(unmatchFields)))

    -- 멱등성 키추가
    redis.call("SADD", idempotencyKey, orderId)
    redis.call("EXPIRE", idempotencyKey, 86400)

    return true
end

-- 반대 주문 정보 처리
local oppositeOrderDetails = oppositeOrders[1]
local oppositeOrderPrice = tonumber(oppositeOrders[2])

-- 반대 주문과 매칭 가격 조건 확인
local isPriceMatched = isBuy and (orderPrice >= oppositeOrderPrice) or (not isBuy and orderPrice <= oppositeOrderPrice)
if not isPriceMatched then
    -- 현재 주문을 저장
    redis.call("ZADD", currentOrderKey, orderPrice, orderDetails)

    -- 주문 정보 파싱
    local orderInfo = parseOrderDetails(orderDetails)

    -- 미체결 Stream에 발행
    local unmatchFields = {
        ["orderId"] = orderId,
        ["userId"] = orderInfo.userId,
        ["tradingPair"] = tradingPair,
        ["orderType"] = orderType,
        ["price"] = tostring(orderPrice),
        ["quantity"] = tostring(orderQuantity),
        ["timestamp"] = orderInfo.timestamp,
    }
    redis.call("XADD", unmatchStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(unmatchFields)))

    -- 멱등성 키추가
    redis.call("SADD", idempotencyKey, orderId)
    redis.call("EXPIRE", idempotencyKey, 86400)

    return true
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

    -- 남은 수량을 미체결 Stream에 발행
    local partialMatchedFields = {
        ["orderId"] = partialOrderId,
        ["userId"] = currentOrder.userId,
        ["tradingPair"] = tradingPair,
        ["orderType"] = orderType,
        ["price"] = tostring(orderPrice),
        ["quantity"] = tostring(remainingOrderQuantity),
        ["timestamp"] = currentOrder.timestamp,
    }
    redis.call("XADD", partialMatchedStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(partialMatchedFields)))
end

-- 매칭 결과를 Stream에 발행
local buyOrder = isBuy and currentOrder or oppositeOrder
local sellOrder = isBuy and oppositeOrder or currentOrder

local matchFields = {
    ["buyUserId"] = buyOrder.userId,
    ["sellUserId"] = sellOrder.userId,
    ["tradingPair"] = tradingPair,
    ["executionPrice"] = tostring(matchPrice),
    ["matchedQuantity"] = tostring(matchedQuantity),
    ["timestamp"] = tostring(redis.call("TIME")[1])
}

-- Redis Stream에 매칭 정보 추가
redis.call("XADD", matchStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(matchFields)))

-- 멱등성 키추가
redis.call("SADD", idempotencyKey, orderId)
redis.call("EXPIRE", idempotencyKey, 86400)

-- 결과 데이터 (필수 정보만 단순한 리스트로 반환)
return true