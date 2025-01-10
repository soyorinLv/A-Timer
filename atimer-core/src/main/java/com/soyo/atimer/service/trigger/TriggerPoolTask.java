package com.soyo.atimer.service.trigger;

import com.soyo.common.redis.ReentrantDistributeLock;
import com.soyo.atimer.model.TaskModel;
import com.soyo.atimer.service.executor.ExecutorWorker;
import com.soyo.atimer.utils.TimerUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class TriggerPoolTask {

    private final ReentrantDistributeLock reentrantDistributeLock;

    private final ExecutorWorker executorWorker;

    @Async("triggerPool")
    public void runExecutor(TaskModel task) {
        if(task == null){
            return;
        }
        log.info("start runExecutor");

        executorWorker.work(TimerUtils.unionTimerIDUnix(task.getTimerId(),task.getRunTimer()));

        log.info("end executeAsync");
    }
}
