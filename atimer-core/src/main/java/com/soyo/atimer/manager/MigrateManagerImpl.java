package com.soyo.atimer.manager;

import com.soyo.common.redis.ReentrantDistributeLock;
import com.soyo.atimer.common.conf.MigratorAppConf;
import com.soyo.atimer.enums.TaskStatus;
import com.soyo.atimer.enums.TimerStatus;
import com.soyo.atimer.exception.BusinessException;
import com.soyo.atimer.exception.ErrorCode;
import com.soyo.atimer.mapper.TaskMapper;
import com.soyo.atimer.mapper.TimerMapper;
import com.soyo.atimer.model.TaskModel;
import com.soyo.atimer.model.TimerModel;
import com.soyo.atimer.redis.TaskCache;
import com.soyo.atimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MigrateManagerImpl implements MigratorManager{

    @Autowired
    private TimerMapper timerMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    private MigratorAppConf migratorAppConf;

    @Autowired
    private TaskCache taskCache;

    @Override
    public void migrateTimer(TimerModel timerModel) {
        // 2. 校验状态
        if(timerModel.getStatus() != TimerStatus.Enable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Enable状态，迁移失败，timerId:"+timerModel.getTimerId());
        }

        // 3.取得批量的执行时机
        CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(timerModel.getCron());
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析cron表达式失败："+timerModel.getCron());
        }
        Date now = new Date();
        Date end = TimerUtils.getForwardTwoMigrateStepEnd(now,migratorAppConf.getMigrateStepMinutes());

        List<Long> executeTimes = TimerUtils.getCronNextsBetween(cronExpression,now,end);
        if (CollectionUtils.isEmpty(executeTimes) ){
            log.warn("获取执行时机 executeTimes 为空");
            return;
        }
        // 执行时机加入数据库
        List<TaskModel> taskList = batchTasksFromTimer(timerModel,executeTimes);
        // 基于 timer_id + run_timer 唯一键，保证任务不被重复插入
        taskMapper.batchSave(taskList);

        // 执行时机加入 redis ZSet
        boolean cacheRes = taskCache.cacheSaveTasks(taskList);
        if(!cacheRes){
            log.error("Zset存储taskList失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ZSet存储taskList失败，timerId:"+timerModel.getTimerId());
        }
    }

    private List<TaskModel> batchTasksFromTimer(TimerModel timerModel, List<Long> executeTimes){
        if(timerModel == null || CollectionUtils.isEmpty(executeTimes)){
            return null;
        }

        List<TaskModel> taskList = new ArrayList<>();
        for (Long runTimer:executeTimes) {
            TaskModel task = new TaskModel();
            task.setApp(timerModel.getApp());
            task.setTimerId(timerModel.getTimerId());
            task.setRunTimer(runTimer);
            task.setStatus(TaskStatus.NotRun.getStatus());
            taskList.add(task);
        }
        return taskList;
    }
}
