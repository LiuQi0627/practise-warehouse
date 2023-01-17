package com.messi.system.product.redis;

import com.messi.system.product.ProductApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 */
@SpringBootTest(classes = ProductApp.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RedisLockTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testTryLock() {
        String testKey = "testKey";
        RLock rLock = redissonClient.getLock(testKey);
        rLock.lock();   //  获取不到锁，一直等待
        System.out.println("加锁成功");
        rLock.unlock();
        System.out.println("解锁成功");
    }
}
