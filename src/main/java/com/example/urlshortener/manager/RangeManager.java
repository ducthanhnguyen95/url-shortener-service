package com.example.urlshortener.manager;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RangeManager {

    private final DistributedAtomicLong distributedGlobalCounter;
    
    private static final int RANGE_SIZE = 1000;
    
    private final AtomicLong currentCounter = new AtomicLong(0);

    private long maxCounter = 0;
    
    public RangeManager(CuratorFramework client) {
        this.distributedGlobalCounter = new DistributedAtomicLong(
                client,
                "/global-id-counter",
                new RetryNTimes(3, 100)
        );
    }

    public synchronized long getNextId() {
        if (currentCounter.get() < maxCounter) return currentCounter.incrementAndGet();
        fetchNewRange();
        return currentCounter.incrementAndGet();
    }

    private void fetchNewRange() {
        try {
            var result = distributedGlobalCounter.add((long) RANGE_SIZE);

            if (result.succeeded()) {
                var startOfRange = result.preValue(); 
                var endOfRange = result.postValue();  

                this.currentCounter.set(startOfRange);
                this.maxCounter = endOfRange;

                System.out.println("--------------------------------------------------");
                System.out.println("✅ RangeManager: Đã xin được dải số mới từ ZooKeeper");
                System.out.println("👉 Từ: " + startOfRange + " đến " + endOfRange);
                System.out.println("--------------------------------------------------");
            } else {
                throw new RuntimeException("RangeManager: Kết nối ZK thất bại, không lấy được dải số.");
            }
        } catch (Exception e) {
            throw new RuntimeException("RangeManager: Lỗi nghiêm trọng khi gọi Zookeeper", e);
        }
    }
}