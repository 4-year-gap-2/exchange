-- 맵을 평면화하는 함수 (Redis XADD 명령에 사용)
local function flattenMap(map)
    local result = {}
    for k, v in pairs(map) do
        table.insert(result, k)
        table.insert(result, v)
    end
    return result
end

-- 사용자 주문 해시키 생성 함수 (userId 기반)
local function getUserOrdersHashKey(userId)
    return "user:orders:" .. userId
end

-- 복합 정렬셋 멤버 생성 함수 (timestamp:userId:orderId 형식)
local function createSortedSetMember(userId, orderId, timestamp)
    return timestamp .. ":" .. userId .. ":" .. orderId
end

-- 정렬셋 멤버에서 정보 추출 함수
local function parseSortedSetMember(member)
    local parts = {}
    for part in string.gmatch(member, "[^:]+") do
        table.insert(parts, part)
    end

    -- timestamp, userId, orderId 반환
    return parts[1], parts[2], parts[3]
end

-- 사용자 해시에서 주문 상세 정보 가져오기
local function getOrderDetails(userId, orderId)
    local userOrdersHashKey = getUserOrdersHashKey(userId)
    -- 주문 필드를 orderId를 접두어로 사용하여 가져오기
    local orderFields = redis.call("HMGET", userOrdersHashKey,
        orderId..":timestamp",
        orderId..":quantity",
        orderId..":price",
        orderId..":type")

    if not orderFields[1] then
        return nil
    end

    return {
        timestamp = orderFields[1],
        quantity = tonumber(orderFields[2]),
        price = tonumber(orderFields[3]),
        type = orderFields[4],
        userId = userId,
        orderId = orderId
    }
end

-- 사용자 해시에 주문 상세 정보 저장
local function storeOrderDetails(userId, orderId, orderData)
    local userOrdersHashKey = getUserOrdersHashKey(userId)

    -- HSET을 사용하여 접두어가 있는 필드 이름으로 저장
    redis.call("HSET", userOrdersHashKey,
        orderId..":timestamp", orderData.timestamp,
        orderId..":quantity", orderData.quantity,
        orderId..":price", orderData.price,
        orderId..":type", orderData.type
    )

    -- 사용자 주문 해시에 합리적인 만료 시간 설정 (필요에 따라 조정)
    redis.call("EXPIRE", userOrdersHashKey, 604800) -- 7일
end

-- 주문 수량 업데이트 함수
local function updateOrderQuantity(userId, orderId, newQuantity)
    local userOrdersHashKey = getUserOrdersHashKey(userId)
    redis.call("HSET", userOrdersHashKey, orderId..":quantity", newQuantity)
end

-- 사용자 해시에서 주문 제거 함수
local function removeOrder(userId, orderId)
    local userOrdersHashKey = getUserOrdersHashKey(userId)
    redis.call("HDEL", userOrdersHashKey,
        orderId..":timestamp",
        orderId..":quantity",
        orderId..":price",
        orderId..":type")
end

-- 부동소수점 정규화 함수
local function normalizeDecimal(value, precision)
    precision = precision or 8  -- 기본값 8자리
    local multiplier = 10^precision
    return math.floor((value * multiplier) + 0.5) / multiplier
end

-- 주문 매칭 Lua 스크립트 시작
-- KEYS[1]: 반대 주문 키 (매수 v7:orders:sell:.., 매도면 v7:orders:buy:..)
-- KEYS[2]: 현재 주문 키 (매수: v7:orders:buy:.., 매도 v7:orders:sell:..)
-- KEYS[3]: 매칭 Stream 키 (v7:stream:matches)
-- KEYS[4]: 미체결 Stream 키 (v7:stream:unmatched)
-- KEYS[5]: 부분 체결 Stream 키
-- KEYS[6]: 멱등성 체크를 위한 키 (v7:idempotency:orders)
-- ARGV[1]: 주문 타입 (BUY 또는 SELL)
-- ARGV[2]: 주문 가격
-- ARGV[3]: 주문 수량
-- ARGV[4]: 포맷팅된 타임스탬프
-- ARGV[5]: 사용자 ID
-- ARGV[6]: 주문 ID
-- ARGV[7]: 부분 체결을 위한 ID
-- ARGV[8]: 거래 쌍 (trading pair)
-- ARGV[9]: 정렬 키 (원래 코드의 orderSortKey와 동일)

local oppositeOrderKey = KEYS[1]
local currentOrderKey = KEYS[2]
local matchStreamKey = KEYS[3]
local unmatchStreamKey = KEYS[4]
local partialMatchedStreamKey = KEYS[5]
local idempotencyKey = KEYS[6]
local orderType = ARGV[1]
local orderPrice = tonumber(ARGV[2])
local orderQuantity = tonumber(ARGV[3])
local orderTimestamp = ARGV[4]
local userId = ARGV[5]
local orderId = ARGV[6]
local partialOrderId = ARGV[7]
local tradingPair = ARGV[8]

-- 멱등성 체크: 이미 처리된 주문인지 확인
if redis.call("SISMEMBER", idempotencyKey, orderId) == 1 then
    -- 이미 처리된 주문이면 early return
    return true
end

-- 미리 계산된 상수
local isBuy = orderType == "BUY"

-- 복합 멤버 키 생성 (정렬셋에 사용)
local sortedSetMember = createSortedSetMember(userId, orderId, orderTimestamp)

-- 반대 주문 가져오기
local oppositeOrders
if isBuy then
    -- 매수주문의 경우 매도 주문 중 가장 낮은 가격(ZRANGE) 선택
    oppositeOrders = redis.call("ZRANGE", oppositeOrderKey, 0, 0, "WITHSCORES")
else
    -- 매도주문의 경우 매수 주문 중 가장 높은 가격(ZREVRANGE) 선택
    oppositeOrders = redis.call("ZREVRANGE", oppositeOrderKey, 0, 0, "WITHSCORES")
end

-- 반대 주문이 없는 경우: 미체결 주문으로 처리
if #oppositeOrders == 0 then
    -- 복합 키를 사용하여 가격을 점수로 하는 SortedSet에 저장
    redis.call("ZADD", currentOrderKey, orderPrice, sortedSetMember)

    -- 주문 상세 정보를 사용자 해시에 저장
    local orderData = {
        timestamp = orderTimestamp,
        quantity = orderQuantity,
        price = orderPrice,
        type = orderType
    }
    storeOrderDetails(userId, orderId, orderData)

    -- 미체결 Stream에 발행
    local unmatchFields = {
        ["orderId"] = orderId,
        ["userId"] = userId,
        ["tradingPair"] = tradingPair,
        ["orderType"] = orderType,
        ["price"] = tostring(orderPrice),
        ["quantity"] = tostring(orderQuantity),
        ["timestamp"] = orderTimestamp,
        ["status"] = "UNMATCHED"
    }
    redis.call("XADD", unmatchStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(unmatchFields)))

    -- 멱등성 키추가 (만료 기간 설정)
    redis.call("SADD", idempotencyKey, orderId)
    redis.call("EXPIRE", idempotencyKey, 86400)

    return true
end

-- 반대 주문 정보 처리
local oppositeSortKey = oppositeOrders[1]
local oppositeOrderPrice = tonumber(oppositeOrders[2])

-- 정렬키에서 사용자 ID와 주문 ID 추출
local oppositeTimestamp, oppositeUserId, oppositeOrderId = parseSortedSetMember(oppositeSortKey)

-- 반대 주문 상세 정보 가져오기
local oppositeOrder = getOrderDetails(oppositeUserId, oppositeOrderId)
oppositeOrder.sortKey = oppositeSortKey  -- 정렬키도 저장

-- 반대 주문이 있고 이제는 가격을 비교
local isPriceMatched = isBuy and (orderPrice >= oppositeOrderPrice) or (not isBuy and orderPrice <= oppositeOrderPrice)
if not isPriceMatched then
    -- 복합 키를 사용하여 가격을 점수로 하는 SortedSet에 저장
    redis.call("ZADD", currentOrderKey, orderPrice, sortedSetMember)

    -- 주문 상세 정보를 사용자 해시에 저장
    local orderData = {
        timestamp = orderTimestamp,
        quantity = orderQuantity,
        price = orderPrice,
        type = orderType
    }
    storeOrderDetails(userId, orderId, orderData)

    -- 미체결 Stream에 발행
    local unmatchFields = {
        ["orderId"] = orderId,
        ["userId"] = userId,
        ["tradingPair"] = tradingPair,
        ["orderType"] = orderType,
        ["price"] = tostring(orderPrice),
        ["quantity"] = tostring(orderQuantity),
        ["timestamp"] = orderTimestamp,
        ["status"] = "UNMATCHED"
    }
    redis.call("XADD", unmatchStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(unmatchFields)))

    -- 멱등성 키추가 (만료 기간 설정)
    redis.call("SADD", idempotencyKey, orderId)
    redis.call("EXPIRE", idempotencyKey, 86400)

    return true
end

-- 매칭 가능한 수량 계산 (정규화 적용)
local matchedQuantity = math.min(orderQuantity, oppositeOrder.quantity)
local remainingOppositeQuantity = normalizeDecimal(oppositeOrder.quantity - matchedQuantity)
local remainingOrderQuantity = normalizeDecimal(orderQuantity - matchedQuantity)

-- 매칭 가격 결정 (반대 주문의 가격)
local matchPrice = oppositeOrderPrice

-- 반대 주문의 수량을 업데이트
if remainingOppositeQuantity > 0 then
    -- 남은 수량이 있으면 해시에서 수량만 업데이트
    updateOrderQuantity(oppositeUserId, oppositeOrderId, remainingOppositeQuantity)
else
    -- 수량이 0이면 sorted set과 해시에서 관련 필드 모두 제거
    redis.call("ZREM", oppositeOrderKey, oppositeOrder.sortKey)
    removeOrder(oppositeUserId, oppositeOrderId)
end

-- 현재 주문에 남은 수량이 있는 경우
if remainingOrderQuantity > 0 then
    -- 복합 키를 사용하여 SortedSet에 저장
    redis.call("ZADD", currentOrderKey, orderPrice, sortedSetMember)

    -- 주문 상세 정보를 사용자 해시에 저장
    local orderData = {
        timestamp = orderTimestamp,
        quantity = remainingOrderQuantity,
        price = orderPrice,
        type = orderType
    }
    storeOrderDetails(userId, orderId, orderData)

    -- 부분 체결 Stream에 발행
    local partialMatchedFields = {
        ["orderId"] = partialOrderId,
        ["userId"] = userId,
        ["tradingPair"] = tradingPair,
        ["orderType"] = orderType,
        ["price"] = tostring(orderPrice),
        ["quantity"] = tostring(remainingOrderQuantity),
        ["timestamp"] = orderTimestamp,
    }
    redis.call("XADD", partialMatchedStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(partialMatchedFields)))
end

-- 매칭 결과를 Stream에 발행
local buyOrder, sellOrder
if isBuy then
    buyOrder = {
        orderId = orderId,
        userId = userId,
        timestamp = orderTimestamp
    }
    sellOrder = {
        orderId = oppositeOrderId,
        userId = oppositeUserId,
        timestamp = oppositeTimestamp
    }
else
    buyOrder = {
        orderId = oppositeOrderId,
        userId = oppositeUserId,
        timestamp = oppositeTimestamp
    }
    sellOrder = {
        orderId = orderId,
        userId = userId,
        timestamp = orderTimestamp
    }
end

local matchFields = {
    ["buyOrderId"] = buyOrder.orderId,
    ["sellOrderId"] = sellOrder.orderId,
    ["buyUserId"] = buyOrder.userId,
    ["sellUserId"] = sellOrder.userId,
    ["tradingPair"] = tradingPair,
    ["executionPrice"] = tostring(matchPrice),
    ["matchedQuantity"] = tostring(matchedQuantity),
    ["buyTimestamp"] = buyOrder.timestamp,
    ["sellTimestamp"] = sellOrder.timestamp,
}

-- Redis Stream에 매칭 정보 추가
redis.call("XADD", matchStreamKey, "MAXLEN", "~", 100000, "*", unpack(flattenMap(matchFields)))

-- 멱등성 키추가 (만료 기간 설정)
redis.call("SADD", idempotencyKey, orderId)
redis.call("EXPIRE", idempotencyKey, 86400)

-- 결과 데이터
return true