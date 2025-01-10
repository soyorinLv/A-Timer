package com.soyo.atimer.service;

import com.soyo.api.dto.atimer.TimerDTO;

import java.util.List;

public interface ATimerService {

    Long createTimer(TimerDTO timerDTO);

    void deleteTimer(String app, long id);

    void update(TimerDTO timerDTO);

    TimerDTO getTimer(String app, long id);

    void enableTimer(String app, long id);

    void unEnableTimer(String app, long id);

    List<TimerDTO> getAppTimers(String app);
}
