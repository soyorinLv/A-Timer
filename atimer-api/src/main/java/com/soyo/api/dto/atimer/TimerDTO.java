package com.soyo.api.dto.atimer;

import lombok.Data;

@Data
public class TimerDTO {
    /**
     * 定时任务ID
     */
    private Long timerId;

    /**
     * APP名称（所属业务）
     */
    private String app;

    /**
     * 定时任务-名称
     */
    private String name;

    /**
     * 定时任务-状态
     */
    private int status;

    /**
     *  定时任务-定时配置
     */
    private String cron;

    /**
     * Name 定时任务-回调参数配置
     */
    private NotifyHTTPParam notifyHTTPParam;

}
