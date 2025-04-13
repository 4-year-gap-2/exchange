-- 주문 매칭 Lua 스크립트
-- KEYS[1]: 반대 주문 키 (매수면 SELL_ORDER_KEY, 매도면 BUY_ORDER_KEY)
-- KEYS[2]: 현재 주문 키 (매수면 BUY_ORDER_KEY, 매도면 SELL_ORDER_KEY)
-- ARGV[1]: 주문 타입 (BUY 또는 SELL)
-- ARGV[2]: 주문 가격
-- ARGV[3]: 주문 수량
-- ARGV[4]: 주문 상세 정보 (quantity|userId|orderId 형식)
-- ARGV[5]: 완전한 스코어 값 (price + timeStr)

local oppositeOrderKey = KEYS[1]
local currentOrderKey = KEYS[2]
local orderType = ARGV[1]
local orderPrice = tonumber(ARGV[2])
local orderQuantity = tonumber(ARGV[3])
local orderDetails = ARGV[4]
local orderScore = tonumber(ARGV[5])

-- 미리 계산된 상수
local isBuy = orderType == "BUY"
local divisor = 1e5 -- 과학적 표기법으로 100000 표현 (더 빠름)
local oppositeType = isBuy and "SELL" or "BUY" -- 반대 주문 타입

-- 반대 주문 가져오기 (조건에 따라 다른 명령 직접 호출)
local oppositeOrders
if isBuy then oppositeOrders = redis.call("ZRANGE", oppositeOrderKey, 0, 0, "WITHSCORES")
else oppositeOrders = redis.call("ZREVRANGE", oppositeOrderKey, 0, 0, "WITHSCORES") end

-- 반대 주문이 없는 경우: 빠른 종료 경로
if #oppositeOrders == 0 then
    redis.call("ZADD", currentOrderKey, orderScore, orderDetails)
    return {"false", "", "0", "0", "0", ARGV[3], orderType, ""} -- 원래 문자열 재사용
end

-- 반대 주문 정보 처리
local oppositeOrderDetails = oppositeOrders[1]
local oppositeOrderScore = tonumber(oppositeOrders[2])
local oppositePrice = math.floor(oppositeOrderScore / divisor)

-- 가격 조건 확인 (삼항 연산자 대신 간결한 조건식)
local isPriceMatched = isBuy and orderPrice >= oppositePrice or orderPrice <= oppositePrice

-- 체결 불가능 시 빠른 종료
if not isPriceMatched then
    redis.call("ZADD", currentOrderKey, orderScore, orderDetails)
    return {"false", "", "0", "0", "0", ARGV[3], orderType, ""} -- 원래 문자열 재사용
end

-- 문자열 처리를 위한 미리 계산된 상수
local pipePos = string.find(oppositeOrderDetails, "|", 1, true)
local oppUserId_start = pipePos + 1
local secondPipePos = string.find(oppositeOrderDetails, "|", oppUserId_start, true)
local oppUserId = string.sub(oppositeOrderDetails, oppUserId_start, secondPipePos - 1)
local oppOrderId = string.sub(oppositeOrderDetails, secondPipePos + 1)

-- 반대 주문의 수량 추출
local oppositeQuantity = tonumber(string.sub(oppositeOrderDetails, 1, pipePos - 1))

-- 매칭 가능한 수량 계산
local matchedQuantity = math.min(orderQuantity, oppositeQuantity)
local remainingOppositeQuantity = oppositeQuantity - matchedQuantity
local remainingOrderQuantity = orderQuantity - matchedQuantity

-- 남아있는 주문의 타입 결정
local remainingOrderType = ""
local remainingDetails = ""

-- 항상 먼저 기존 정보 제거 (수량이 달라지므로 value를 키값으로 업데이트 불가 제거 후 추가 필요)
redis.call("ZREM", oppositeOrderKey, oppositeOrderDetails)

-- 반대 주문에 남은 수량이 있는 경우
if remainingOppositeQuantity > 0 then
    -- 남은 주문 타입은 반대 주문 타입
    remainingOrderType = oppositeType
    -- 미리 형변환하여 문자열 연결 최소화
    local updatedOppositeDetails = remainingOppositeQuantity .. "|" .. oppUserId .. "|" .. oppOrderId
    redis.call("ZADD", oppositeOrderKey, oppositeOrderScore, updatedOppositeDetails)
    -- 남은 주문 상세 정보 저장
    remainingDetails = updatedOppositeDetails
end

-- 현재 주문에 남은 수량이 있는 경우
if remainingOrderQuantity > 0 then
    remainingOrderType = orderType

    -- 현재 주문 파싱 시 미리 계산된 상수 활용
    local orderPipePos = string.find(orderDetails, "|", 1, true)
    local orderUserId_start = orderPipePos + 1
    local secondOrderPipePos = string.find(orderDetails, "|", orderUserId_start, true)

    -- 미리 형변환하여 문자열 연결 최소화
    local updatedOrderDetails = remainingOrderQuantity .. "|" ..
                               string.sub(orderDetails, orderUserId_start, secondOrderPipePos - 1) .. "|" ..
                               string.sub(orderDetails, secondOrderPipePos + 1)

    redis.call("ZADD", currentOrderKey, orderScore, updatedOrderDetails)

    remainingDetails = updatedOrderDetails
end

-- 결과 반환
return {
    "true",                           -- 매칭 여부
    oppositeOrderDetails,             -- 매칭된 반대 주문 정보
    tostring(oppositeOrderScore),     -- 매칭된 반대 주문 점수
    tostring(oppositePrice),          -- 매칭 가격
    tostring(matchedQuantity),        -- 매칭된 수량
    tostring(remainingOrderQuantity), -- 남은 수량
    remainingOrderType,               -- 남은 주문 타입 (BUY/SELL)
    remainingDetails                  -- 남은 주문 상세 정보
}