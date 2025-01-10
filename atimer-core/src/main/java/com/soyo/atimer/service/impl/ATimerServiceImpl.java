package com.soyo.atimer.service.impl;

import com.soyo.api.dto.atimer.TimerDTO;
import com.soyo.common.redis.ReentrantDistributeLock;
import com.soyo.atimer.enums.TimerStatus;
import com.soyo.atimer.exception.BusinessException;
import com.soyo.atimer.exception.ErrorCode;
import com.soyo.atimer.manager.MigratorManager;
import com.soyo.atimer.mapper.TimerMapper;
import com.soyo.atimer.model.TimerModel;
import com.soyo.atimer.service.ATimerService;
import com.soyo.atimer.utils.TimerUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor

public class ATimerServiceImpl implements ATimerService {

    private final TimerMapper timerMapper;

    private final ReentrantDistributeLock reentrantDistributeLock;

    private final MigratorManager migratorManager;

    private static final int DEFAULT_GAP_SECONDS = 3;

    @Override
    public Long createTimer(TimerDTO timerDTO) {
//        String lockToken = TimerUtils.GetTokenStr();
//        // 只加锁不解锁，只有超时解锁；超时时间控制频率；
//        boolean ok = reentrantDistributeLock.lock(
//                TimerUtils.GetCreateLockKey(timerDTO.getApp()),
//                lockToken,
//                defaultGapSeconds);
//        if(!ok){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建/删除操作过于频繁，请稍后再试！");
//        }

        boolean isValidCron = CronExpression.isValidExpression(timerDTO.getCron());
        if (!isValidCron) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "invalid cron");
        }

        TimerModel timerModel = TimerModel.voToObj(timerDTO);
        if (timerModel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        timerMapper.save(timerModel);
        return timerModel.getTimerId();
    }

    @Override
    public void deleteTimer(String app, long id) {
        String lockToken = TimerUtils.getTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.getCreateLockKey(app),
                lockToken,
                DEFAULT_GAP_SECONDS);
        if (!ok) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建/删除操作过于频繁，请稍后再试！");
        }

        timerMapper.deleteById(id);
    }

    @Override
    public void update(TimerDTO timerDTO) {
        TimerModel timerModel = TimerModel.voToObj(timerDTO);
        if (timerModel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        timerMapper.update(timerModel);
    }

    @Override
    public TimerDTO getTimer(String app, long id) {
        TimerModel timerModel = timerMapper.getTimerById(id);
        return TimerModel.objToVo(timerModel);
    }

    @Override
    public void enableTimer(String app, long id) {
        String lockToken = TimerUtils.getTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.getEnableLockKey(app),
                lockToken,
                DEFAULT_GAP_SECONDS);
        if (!ok) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "激活/去激活操作过于频繁，请稍后再试！");
        }

        // 激活逻辑
        doEnableTimer(id);
    }

    @Transactional
    public void doEnableTimer(long id) {
        // 1. 数据库获取Timer
        TimerModel timerModel = timerMapper.getTimerById(id);
        if (timerModel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "激活失败，timer不存在：timerId" + id);
        }
        // 2. 校验状态
        if (timerModel.getStatus() == TimerStatus.Enable.getStatus()) {
            log.warn("Timer非Unable状态，激活失败，timerId:" + timerModel.getTimerId());
        }
        // 修改 timer 状态为激活态
        timerModel.setStatus(TimerStatus.Enable.getStatus());
        timerMapper.update(timerModel);
        //迁移数据
        migratorManager.migrateTimer(timerModel);

    }


    @Override
    public void unEnableTimer(String app, long id) {
        String lockToken = TimerUtils.getTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.getEnableLockKey(app),
                lockToken,
                DEFAULT_GAP_SECONDS);
        if (!ok) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "激活/去激活操作过于频繁，请稍后再试！");
        }

        // 去激活逻辑
        doUnEnableTimer(id);
    }

    @Transactional
    public void doUnEnableTimer(Long id) {
        // 1. 数据库获取Timer
        TimerModel timerModel = timerMapper.getTimerById(id);
        // 2. 校验状态
        if (timerModel.getStatus() != TimerStatus.Enable.getStatus()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Timer非Enable状态，去激活失败，id:" + id);
        }
        timerModel.setStatus(TimerStatus.Unable.getStatus());
        timerMapper.update(timerModel);
    }


    @Override
    public List<TimerDTO> getAppTimers(String app) {
        return null;
    }
}
