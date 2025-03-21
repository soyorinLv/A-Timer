package com.soyo.atimer.service.migrator;

import com.soyo.common.redis.ReentrantDistributeLock;
import com.soyo.atimer.common.conf.MigratorAppConf;
import com.soyo.atimer.enums.TimerStatus;
import com.soyo.atimer.manager.MigratorManager;
import com.soyo.atimer.mapper.TimerMapper;
import com.soyo.atimer.model.TimerModel;
import com.soyo.atimer.utils.TimerUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class MigratorWorker {

    private final TimerMapper timerMapper;

    private final MigratorAppConf migratorAppConf;

    private final MigratorManager migratorManager;

    private final ReentrantDistributeLock reentrantDistributeLock;

    @Scheduled(fixedRate = 10*1000) // 60*60*1000 一小时执行一次
    public void work() {
        log.info("开始迁移时间：" + LocalDateTime.now());
        Date startHour = getStartHour(new Date());
        String lockToken = TimerUtils.getTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.getMigratorLockKey(startHour),
                lockToken,
                60L*migratorAppConf.getMigrateTryLockMinutes());
        if(!ok){
            log.warn("migrator get lock failed！"+TimerUtils.getMigratorLockKey(startHour));
            return;
        }

        //迁移
        migrate();

        // 更新分布式锁过期时间
        reentrantDistributeLock.expireLock(
                TimerUtils.getMigratorLockKey(startHour),
                lockToken,
                60L*migratorAppConf.getMigrateSuccessExpireMinutes());
    }

    private Date getStartHour(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            return sdf.parse(sdf.format(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private void migrate(){
        List<TimerModel> timers= timerMapper.getTimersByStatus(TimerStatus.Enable.getStatus());
        if(CollectionUtils.isEmpty(timers)){
            log.info("migrate timers is empty");
            return;
        }

        for (TimerModel timerModel:timers) {
            migratorManager.migrateTimer(timerModel);
        }
    }
}

