package com.soyo.atimer.controller;

import com.soyo.api.dto.atimer.TimerDTO;
import com.soyo.common.model.ResponseEntity;
import com.soyo.atimer.service.ATimerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/atimer")
@Slf4j
@AllArgsConstructor
public class AtimerWebController {

    private final ATimerService aTimerService;

    @PostMapping(value = "/createTimer")
    public ResponseEntity<Long> createTimer(@RequestBody TimerDTO timerDTO){
        Long timerId = aTimerService.createTimer(timerDTO);
        return ResponseEntity.ok(timerId);
    }

    @GetMapping(value = "/enableTimer")
    public ResponseEntity<String> enableTimer(@RequestParam(value = "app") String app,
                            @RequestParam(value = "timerId") Long timerId,
                            @RequestHeader MultiValueMap<String, String> headers){
        aTimerService.enableTimer(app,timerId);
        return ResponseEntity.ok("ok");
    }
}
