package com.hhk.pathfinderbacked.cache;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BloomFilterService {

    private final RedissonClient redissonClient;

    public void init(String filterName, long expectedInsertions, double falseProbability) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(filterName);
        if (!bloomFilter.isExists()) {
            bloomFilter.tryInit(expectedInsertions, falseProbability);
        }
    }

    public void add(String filterName, String value) {
        redissonClient.getBloomFilter(filterName).add(value);
    }

    public boolean mightContain(String filterName, String value) {
        return redissonClient.getBloomFilter(filterName).contains(value);
    }
}
