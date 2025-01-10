package com.soyo.atimer.service.scheduler;

import com.soyo.common.redis.ReentrantDistributeLock;
import com.soyo.atimer.common.conf.SchedulerAppConf;
import com.soyo.atimer.service.trigger.TriggerWorker;
import com.soyo.atimer.utils.TimerUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@AllArgsConstructor
public class SchedulerTask {

    private final ReentrantDistributeLock reentrantDistributeLock;

    private final SchedulerAppConf schedulerAppConf;

    private final TriggerWorker triggerWorker;

    @Async("schedulerPool")
    public void asyncHandleSlice(Date date, int bucketId) {
        log.info("start executeAsync");

        String lockToken = TimerUtils.getTokenStr();
        // 只加锁不解锁，只有超时解锁；超时时间控制频率；
        // 锁住横纵向切分后的：单个桶（分钟+bucketIndex）
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.getTimeBucketLockKey(date, bucketId),
                lockToken,
                schedulerAppConf.getTryLockSeconds());
        if (!ok) {
            log.info("asyncHandleSlice 获取分布式锁失败");
            return;
        }

        log.info("get scheduler lock success, key: %s", TimerUtils.getTimeBucketLockKey(date, bucketId));

        // 调用trigger进行处理
        triggerWorker.work(TimerUtils.getSliceMsgKey(date, bucketId));

        // 延长分布式锁的时间,避免重复执行分片
        reentrantDistributeLock.expireLock(
                TimerUtils.getTimeBucketLockKey(date, bucketId),
                lockToken,
                schedulerAppConf.getSuccessExpireSeconds());

        log.info("end executeAsync");
    }
}
