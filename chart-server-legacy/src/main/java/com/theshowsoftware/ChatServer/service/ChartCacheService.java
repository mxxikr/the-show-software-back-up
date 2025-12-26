package com.theshowsoftware.ChatServer.service;

import com.theshowsoftware.ChatServer.dto.CandlePacketDTO;
import com.theshowsoftware.ChatServer.dto.TickPacketDTO;
import com.theshowsoftware.ChatServer.enums.ChartType;
import com.theshowsoftware.ChatServer.enums.SymbolType;
import com.theshowsoftware.ChatServer.enums.ErrorCode;
import com.theshowsoftware.ChatServer.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class ChartCacheService {

    // Tick 데이터 캐시
    private final Map<SymbolType, TreeMap<Instant, TickPacketDTO>> tickCache = new ConcurrentHashMap<>();

    // Candle 데이터 캐시
    private final Map<SymbolType, Map<ChartType, TreeMap<Instant, CandlePacketDTO>>> candleCache = new ConcurrentHashMap<>();

    // 활성 캔들 데이터
    private final Map<SymbolType, Map<ChartType, CandlePacketDTO>> activeCandles = new ConcurrentHashMap<>();

    // 심볼별 읽기/쓰기 잠금
    private final Map<SymbolType, ReadWriteLock> symbolLocks = new ConcurrentHashMap<>();

    // Tick 데이터 크기 제한
    private static final int TICK_CACHE_LIMIT = 20_000_000;

    // Candle 데이터 인터벌 별 최대 크기 제한
    private static final Map<ChartType, Integer> CANDLE_LIMITS = Map.ofEntries(
            Map.entry(ChartType.TICK, 604800), // 틱
            Map.entry(ChartType.ONE_SECOND, 604800), // 1초 - 1주
            Map.entry(ChartType.THREE_SECONDS, 201600), // 3초 - 1주
            Map.entry( ChartType.FIVE_SECONDS, 120960), // 5초 - 1주
            Map.entry(ChartType.TEN_SECONDS, 60480), // 10초 - 1주
            Map.entry(ChartType.THIRTY_SECONDS, 20160), // 30초 - 1주
            Map.entry(ChartType.ONE_MINUTE, 10080),// 1분 - 1주
            Map.entry(ChartType.THREE_MINUTES, 3360),// 3분 - 1주
            Map.entry(ChartType.FIVE_MINUTES, 2016),// 5분 - 1주
            Map.entry(ChartType.TEN_MINUTES, 1008), // 10분 - 1주
            Map.entry(ChartType.FIFTEEN_MINUTES, 672),// 15분
            Map.entry(ChartType.ONE_HOUR, 720), // 1시간 - 30일
            Map.entry(ChartType.THREE_HOURS, 1440), // 3시간 - 2개월
            Map.entry(ChartType.FIVE_HOURS, 1296), // 5시간 - 3개월
            Map.entry(ChartType.TWELVE_HOURS, 360), // 12시간 - 6개월
            Map.entry(ChartType.ONE_DAY, 365), // 1일 - 1년
            Map.entry(ChartType.THREE_DAYS, 122), // 3일 - 1년
            Map.entry(ChartType.ONE_WEEK, 52), // 1주 - 1년
            Map.entry(ChartType.ONE_MONTH, 12) // 1달 - 1년
    );

    public ChartCacheService() {
        for (SymbolType symbol : SymbolType.values()) {
            tickCache.put(symbol, new TreeMap<>());
            candleCache.put(symbol, new TreeMap<>());

            // ChartType 초기화
            Map<ChartType, TreeMap<Instant, CandlePacketDTO>> intervalMap = new ConcurrentHashMap<>();
            for (ChartType chartType : ChartType.values()) {
                intervalMap.put(chartType, new TreeMap<>());
            }
            candleCache.put(symbol, intervalMap);

            activeCandles.put(symbol, new ConcurrentHashMap<>());
            symbolLocks.put(symbol, new ReentrantReadWriteLock());
        }
    }

    // ==========================================================
    //                   Tick 데이터 관련 메서드
    // ==========================================================
    
    /**
     * Tick 데이터 추가
     */
    public void addTick(SymbolType symbol, TickPacketDTO tick) {
        if (symbol == null || tick == null) {
            throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
        }

        if (tick.getPrice() == null || tick.getPrice().compareTo(BigInteger.ZERO) <= 0) {
            throw new CustomException(ErrorCode.INVALID_TICK_PRICE);
        }

        Instant tickTimestamp = Instant.ofEpochMilli(tick.getTimestamp());
        if (tickTimestamp.isAfter(Instant.now().plusSeconds(60))) {
            log.warn("[ChartCache] 미래 타임스탬프 감지: {}", tickTimestamp);
            return;
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.writeLock().lock();

        try {
            // Tick 데이터 추가
            TreeMap<Instant, TickPacketDTO> ticks = tickCache.get(symbol);

            if (ticks == null) {
                log.error("[ChartCache] 심볼 [{}] 초기화가 안 되어 있습니다.", symbol);
                throw new CustomException(ErrorCode.CACHE_SYMBOL_NOT_FOUND);
            }

            // Tick 캐시 크기 제한 확인
            if (ticks.size() > TICK_CACHE_LIMIT) {
                ticks.pollFirstEntry();
                log.debug("[ChartCache] 오래된 Tick 데이터를 삭제했습니다. (심볼: {}, 현재 캐시 크기: {})", symbol, ticks.size());
            }
            ticks.put(tickTimestamp, tick);

            // 모든 ChartType에 대해 업데이트
            updateCandlesForAllIntervals(symbol, tick);

        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * Tick 데이터 List 추가
     */
    public void addTicks(SymbolType symbol, List<TickPacketDTO> ticks) {
        if (symbol == null || ticks == null || ticks.isEmpty()) {
            throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.writeLock().lock();
        try {
            List<TickPacketDTO> sortedTicks = ticks.stream()
                    .filter(p -> p != null
                            && p.getTimestamp() != null
                            && p.getPrice() != null
                            && p.getPrice().compareTo(BigInteger.ZERO) > 0)
                    .sorted(Comparator.comparing(TickPacketDTO::getTimestamp)) // 시간순 정렬
                    .toList();

            TreeMap<Instant, TickPacketDTO> symbolTicks = tickCache.get(symbol);

            for (TickPacketDTO tick : sortedTicks) {
                symbolTicks.put(Instant.ofEpochMilli(tick.getTimestamp()), tick);
                updateCandlesForAllIntervals(symbol, tick);
            }

            // 크기 제한 초과 시 오래된 데이터 삭제
            if (symbolTicks.size() > TICK_CACHE_LIMIT) {
                int removeCount = symbolTicks.size() - TICK_CACHE_LIMIT;
                Iterator<Instant> iterator = symbolTicks.keySet().iterator();

                for (int i = 0; i < removeCount && iterator.hasNext(); i++) {
                    iterator.next();
                    iterator.remove();
                }
                log.debug("[ChartCache] 초과된 Tick 데이터를 삭제했습니다. (심볼: {}, 삭제된 개수: {})", symbol, removeCount);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 최근 Tick 데이터를 반환
     */
    public synchronized List<TickPacketDTO> getTicks(SymbolType symbol, int count) {
        if (symbol == null || count <= 0) {
            throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.readLock().lock();

        try {
            TreeMap<Instant, TickPacketDTO> symbolTicks = tickCache.get(symbol);
            if (symbolTicks == null) {
                throw new CustomException(ErrorCode.CACHE_SYMBOL_NOT_FOUND);
            }

            List<TickPacketDTO> result = new ArrayList<>();
            Iterator<Map.Entry<Instant, TickPacketDTO>> it = symbolTicks.descendingMap().entrySet().iterator();

            for (int i = 0; i < count && it.hasNext(); i++) {
                result.add(it.next().getValue());
            }

            Collections.reverse(result); // 시간순으로 정렬
            return result;

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 특정 시간 범위 내의 Tick 데이터를 반환
     */
    public synchronized List<TickPacketDTO> getTicksBetween(SymbolType symbol, Instant startTime, Instant endTime) {
        if (symbol == null || startTime == null || endTime == null || startTime.isAfter(endTime)) {
            throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.readLock().lock();
        try {
            TreeMap<Instant, TickPacketDTO> symbolTicks = tickCache.get(symbol);
            if (symbolTicks == null) {
                throw new CustomException(ErrorCode.CACHE_SYMBOL_NOT_FOUND);
            }

            return new ArrayList<>(symbolTicks.subMap(startTime, true, endTime, true).values());

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 심볼의 최신 Tick 데이터를 반환
     */
    public TickPacketDTO getLatestTick(SymbolType symbol) {
        if (symbol == null) {
            return null;
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.readLock().lock();
        try {
            TreeMap<Instant, TickPacketDTO> ticks = tickCache.get(symbol); // 심볼에 해당하는 Tick 데이터 조회
            if (ticks == null || ticks.isEmpty()) {
                return null;
            }
            return ticks.lastEntry().getValue(); // 가장 최신 Tick 반환
        } finally {
            lock.readLock().unlock();
        }
    }

    // ==========================================================
    //                   Candle 데이터 관련 메서드
    // ==========================================================
    /**
     * 모든 인터벌에 대해 캔들을 업데이트
     */
    private void updateCandlesForAllIntervals(SymbolType symbol, TickPacketDTO tick) {
        Map<ChartType, CandlePacketDTO> symbolActiveCandles = activeCandles.get(symbol);
        Map<ChartType, TreeMap<Instant, CandlePacketDTO>> symbolCandleCache = candleCache.get(symbol);

        for (ChartType chartType : ChartType.values()) {
            updateCandleForInterval(symbol, chartType, tick, symbolActiveCandles, symbolCandleCache);
        }
    }

    /**
     * 단일 인터벌에 대해 활성 캔들과 완료된 캔들 업데이트
     */
    private void updateCandleForInterval(SymbolType symbol, ChartType chartType, TickPacketDTO tick,
                                         Map<ChartType, CandlePacketDTO> activeCandleMap,
                                         Map<ChartType, TreeMap<Instant, CandlePacketDTO>> candleCacheMap) {

        Instant candleStart = ChartType.truncateTime(Instant.ofEpochSecond(tick.getTimestamp()), chartType);
        Instant candleEnd = candleStart.plusMillis(chartType.getIntervalInMillis());

        // 활성 캔들 가져오기
        CandlePacketDTO activeCandle = activeCandleMap.get(chartType);

        if (activeCandle == null || !activeCandle.getCandleStartTime().equals(candleStart)) {
            // 기존 활성 캔들을 완성된 캔들로 마감
            if (activeCandle != null) {
                TreeMap<Instant, CandlePacketDTO> intervalCandles = candleCacheMap.get(chartType);
                intervalCandles.put(Instant.ofEpochMilli(activeCandle.getCandleStartTime()), activeCandle);

                // 크기 제한 초과 시 가장 오래된 캔들 제거
                int limit = CANDLE_LIMITS.getOrDefault(chartType, 1000);
                while (intervalCandles.size() > limit) {
                    intervalCandles.remove(intervalCandles.firstKey());
                }
            }

            // 새로운 활성 캔들 시작
            activeCandle = CandlePacketDTO.builder()
                    .candleStartTime(candleStart.toEpochMilli())
                    .candleEndTime(candleEnd.toEpochMilli())
                    .startPrice(tick.getPrice())
                    .highPrice(tick.getPrice())
                    .lowPrice(tick.getPrice())
                    .endPrice(tick.getPrice())
                    .quantity(tick.getQuantity() != null ? tick.getQuantity() : 0L)
                    .build();
            activeCandleMap.put(chartType, activeCandle);
        } else {
            // 기존 활성 캔들 업데이트
            activeCandle.setHighPrice(tick.getPrice().max(activeCandle.getHighPrice()));
            activeCandle.setLowPrice(tick.getPrice().min(activeCandle.getLowPrice()));
            activeCandle.setEndPrice(tick.getPrice());
            activeCandle.setTickCount(activeCandle.getTickCount() + 1);

            if (tick.getQuantity() != null) {
                activeCandle.setQuantity(activeCandle.getQuantity() + tick.getQuantity());
            } else {
                activeCandle.setQuantity(0L);
            }
        }
    }

    /**
     * Candle 데이터 추가
     */
    public void addCandle(SymbolType symbol, ChartType chartType, CandlePacketDTO candle) {
        if (symbol == null || chartType == null || candle == null) {
            throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.writeLock().lock();
        try {
            // 해당 심볼과 차트 타입의 데이터 가져옴
            Map<ChartType, TreeMap<Instant, CandlePacketDTO>> symbolCandleMap = candleCache.get(symbol);
            if (symbolCandleMap == null || !symbolCandleMap.containsKey(chartType)) {
                throw new CustomException(ErrorCode.CACHE_SYMBOL_NOT_FOUND);
            }

            TreeMap<Instant, CandlePacketDTO> intervalCandles = symbolCandleMap.get(chartType);

            // 새로운 캔들을 추가
            intervalCandles.put(Instant.ofEpochMilli(candle.getCandleStartTime()), candle);

            // 크기 제한 초과 시 오래된 데이터 삭제
            int limit = CANDLE_LIMITS.getOrDefault(chartType, 1000);
            while (intervalCandles.size() > limit) {
                intervalCandles.pollFirstEntry(); // 가장 오래된 데이터 삭제
            }

            log.debug("[addCandle] ChartType: {}, 현재 캐시 크기: {}, 크기 제한: {}", chartType, intervalCandles.size(), limit);

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 최근 Candle 데이터를 반환
     */
    public List<CandlePacketDTO> getCandles(SymbolType symbol, ChartType chartType, int count) {
        if (symbol == null || chartType == null || count <= 0) {
            throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.readLock().lock();
        try {
            // 전체 Candle 데이터 조회
            Map<ChartType, TreeMap<Instant, CandlePacketDTO>> candleMap = candleCache.get(symbol);
            if (candleMap == null || !candleMap.containsKey(chartType)) {
                throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
            }

            // 특정 ChartType의 완료된 캔들 가져옴
            TreeMap<Instant, CandlePacketDTO> intervalCandles = candleMap.get(chartType);
            LinkedList<CandlePacketDTO> result = new LinkedList<>();

            // 완성된 캔들 추가
            intervalCandles.descendingMap().values().stream()
                    .limit(count) // count 만큼 제한
                    .forEach(result::add);

            // 활성 캔들 추가 (최신 활성 캔들을 처음에 삽입)
            CandlePacketDTO activeCandle = activeCandles.get(symbol).get(chartType);
            if (activeCandle != null && result.size() < count) {
                result.addFirst(activeCandle); // 맨 앞에 삽입
            }

            // 결과를 정방향으로 정렬해서 반환
            Collections.reverse(result);
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 특정 시간 범위 내의 Candle 데이터를 반환
     */
    public List<CandlePacketDTO> getCandlesBetween(SymbolType symbol, ChartType chartType, Instant startTime, Instant endTime) {
        if (symbol == null || startTime == null || endTime == null || startTime.isAfter(endTime)) {
            throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.readLock().lock();
        try {
            // 심볼 데이터를 조회
            Map<ChartType, TreeMap<Instant, CandlePacketDTO>> intervalCandleMap = candleCache.get(symbol);
            if (intervalCandleMap == null || !intervalCandleMap.containsKey(chartType)) {
                throw new CustomException(ErrorCode.CACHE_DATA_IS_NULL);
            }

            TreeMap<Instant, CandlePacketDTO> candles = intervalCandleMap.get(chartType);
            if (candles == null || candles.isEmpty()) {
                return Collections.emptyList();
            }

            // 지정된 범위의 캔들 조회
            NavigableMap<Instant, CandlePacketDTO> subMap = candles.subMap(startTime, true, endTime, false);

            List<CandlePacketDTO> result = new ArrayList<>(subMap.values());

            // 활성 캔들을 확인하고 해당 범위에 포함되면 추가
            Map<ChartType, CandlePacketDTO> activeCandleMap = activeCandles.get(symbol);
            CandlePacketDTO activeCandle = activeCandleMap != null ? activeCandleMap.get(chartType) : null;

            if (activeCandle != null &&
                    !Instant.ofEpochMilli(activeCandle.getCandleStartTime()).isBefore(startTime) && // 시작 시간 이후
                    Instant.ofEpochMilli(activeCandle.getCandleStartTime()).isBefore(endTime)) {   // 종료 시간 미만
                result.add(activeCandle); // 활성 캔들 추가
            }

            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 심볼의 최신 Candle 데이터를 반환
     */
    public CandlePacketDTO getLatestCandle(SymbolType symbol) {
        if (symbol == null) {
            return null;
        }

        ReadWriteLock lock = symbolLocks.get(symbol);
        lock.readLock().lock();
        try {
            // 심볼에 해당하는 모든 차트 타입의 Candle 데이터를 가져옴
            Map<ChartType, TreeMap<Instant, CandlePacketDTO>> symbolCandles = candleCache.get(symbol);
            if (symbolCandles == null || symbolCandles.isEmpty()) {
                return null;
            }

            return symbolCandles.values().stream()
                    .filter(candles -> candles != null && !candles.isEmpty())
                    .map(TreeMap::lastEntry)
                    .filter(Objects::nonNull) // null Entry 필터링
                    .max(Comparator.comparing(entry -> entry.getKey())) // 가장 최신 Instant로 정렬
                    .map(Map.Entry::getValue)
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }
}