package com.soyo.atimer.common.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MigratorAppConf {

    @Value("${migrator.workersNum}")
    private int workersNum;
    @Value("${migrator.migrateStepMinutes}")
    private int migrateStepMinutes;
    @Value("${migrator.migrateSuccessExpireMinutes}")
    private int migrateSuccessExpireMinutes;
    @Value("${migrator.migrateTryLockMinutes}")
    private int migrateTryLockMinutes;
    @Value("${migrator.timerDetailCacheMinutes}")
    private int timerDetailCacheMinutes;

}
