package com.soyo.api.feign;


import com.soyo.api.dto.atimer.TimerDTO;
import com.soyo.api.feign.interceptor.ContextFeignInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "Atimer",configuration = ContextFeignInterceptor.class)
public interface ATimerClient {

    @PostMapping(value = "/createTimer")
    public Long createTimer(@RequestBody TimerDTO timerDTO);

    @GetMapping(value = "/enableTimer")
    public void enableTimer(@RequestParam(value = "app") String app,
                            @RequestParam(value = "timerId") Long timerId,
                            @RequestHeader MultiValueMap<String, String> headers);

}
