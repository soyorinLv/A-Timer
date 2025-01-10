package com.soyo.atimer.common.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class TriggerAppConf {
    @Value("${trigger.zrangeGapSeconds}")
    private int zrangeGapSeconds;
    @Value("${trigger.workersNum}")
    private int workersNum;

    @Value("${trigger.pool.corePoolSize}")
    private int corePoolSize;

    @Value("${trigger.pool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${trigger.pool.queueCapacity}")
    private int queueCapacity;

    @Value("${trigger.pool.namePrefix}")
    private String namePrefix;

}