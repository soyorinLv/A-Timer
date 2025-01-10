package com.soyo.atimer.feign;

import com.soyo.api.dto.atimer.TimerDTO;
import com.soyo.api.feign.ATimerClient;
import com.soyo.atimer.service.ATimerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ATimerFeignController implements ATimerClient {

    @Autowired
    ATimerService aTimerService;

    @Override
    public Long createTimer(TimerDTO timerDTO) {
        return aTimerService.createTimer(timerDTO);
    }

    @Override
    public void enableTimer(String app, Long timerId, MultiValueMap<String, String> headers) {
        aTimerService.enableTimer(app, timerId);
    }
}
