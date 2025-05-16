local zsetKey = KEYS[1]
local streamKey = KEYS[2]
local value = ARGV[1]
local tradingPair = ARGV[2]
local orderType = ARGV[3]

local score = redis.call('ZSCORE', zsetKey, value)

local removed = redis.call('ZREM', zsetKey, value)

if removed > 0 then
    -- value를 | 기준으로 분리
    local timestamp, quantity, userId, orderId = string.match(value, "([^|]+)|([^|]+)|([^|]+)|([^|]+)")
     redis.call('XADD', streamKey, '*',
             'orderId', orderId,
             'orderType', orderType,
             'price', score,
             'quantity', quantity,
             'status', 'CANCEL',
             'timestamp', timestamp,
             'tradingPair', tradingPair,
             'userId', userId
     )
    return value
else
    return nil
end