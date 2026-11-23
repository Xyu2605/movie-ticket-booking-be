
-- KEYS[1]: Redis key
-- ARGV[1]: MAX_FAILURES
-- ARGV[2]: FAILURE_WINDOW (milliseconds)
-- ARGV[3]: LOCK_DURATION (milliseconds)

local key = KEYS[1]

local maxFailures = tonumber(ARGV[1])
local failureWindow = tonumber(ARGV[2])
local lockDuration = tonumber(ARGV[3])

-- Lấy thời gian trực tiếp từ Redis Server
local time = redis.call('TIME')
local now = tonumber(time[1]) * 1000
          + math.floor(tonumber(time[2]) / 1000)

local attempts = tonumber(
    redis.call('HGET', key, 'failedAttempts') or '0'
)

local windowStartedAt = tonumber(
    redis.call('HGET', key, 'windowStartedAt') or '0'
)

local lockedUntil = tonumber(
    redis.call('HGET', key, 'lockedUntil') or '0'
)

-- Kiểm tra tài khoản đang bị khóa
if lockedUntil > now then
    return {1, attempts, lockedUntil}
end

-- Nếu khóa đã hết hạn thì reset trạng thái khóa
if lockedUntil > 0 then
    redis.call('HDEL', key, 'lockedUntil')
    attempts = 0
    windowStartedAt = 0
end

-- Kiểm tra failure window
if windowStartedAt == 0
    or now - windowStartedAt >= failureWindow then

    attempts = 1
    windowStartedAt = now
else
    attempts = attempts + 1
end

-- Đạt ngưỡng khóa
if attempts >= maxFailures then

    lockedUntil = now + lockDuration

    redis.call('HSET', key,
        'failedAttempts', attempts,
        'windowStartedAt', windowStartedAt,
        'lockedUntil', lockedUntil
    )

    redis.call('PEXPIRE', key, lockDuration)

    return {1, attempts, lockedUntil}
end

-- Lưu trạng thái bình thường
redis.call('HSET', key,
    'failedAttempts', attempts,
    'windowStartedAt', windowStartedAt
)

-- Key tự hết hạn khi kết thúc failure window
redis.call('PEXPIRE', key,
    failureWindow - (now - windowStartedAt)
)

return {0, attempts, 0}
