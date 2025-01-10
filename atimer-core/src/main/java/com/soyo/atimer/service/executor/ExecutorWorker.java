package com.soyo.atimer.service.executor;

import com.soyo.api.dto.atimer.NotifyHTTPParam;
import com.soyo.api.dto.atimer.TimerDTO;
import com.soyo.atimer.enums.TaskStatus;
import com.soyo.atimer.enums.TimerStatus;
import com.soyo.atimer.exception.BusinessException;
import com.soyo.atimer.exception.ErrorCode;
import com.soyo.atimer.mapper.TaskMapper;
import com.soyo.atimer.mapper.TimerMapper;
import com.soyo.atimer.model.TaskModel;
import com.soyo.atimer.model.TimerModel;
import com.soyo.atimer.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class ExecutorWorker {

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    TimerMapper timerMapper;

    public void work(String timerIDUnixKey) {

        List<Long> longSet = TimerUtils.splitTimerIDUnix(timerIDUnixKey);
        if (longSet.size() != 2) {
            log.error("splitTimerIDUnix 错误, timerIDUnix:" + timerIDUnixKey);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "splitTimerIDUnix 错误, timerIDUnix:" + timerIDUnixKey);
        }
        Long timerId = longSet.get(0);
        Long unix = longSet.get(1);
        TaskModel task = taskMapper.getTasksByTimerIdUnix(timerId, unix);
        if (task.getStatus() != TaskStatus.NotRun.getStatus()) {
            log.warn("重复执行任务： timerId" + timerId + ",runtimer:" + unix);
            return;
        }

        // 执行回调
        executeAndPostProcess(task, timerId, unix);
    }

    private void executeAndPostProcess(TaskModel taskModel, Long timerId, Long unix) {
        TimerModel timerModel = timerMapper.getTimerById(timerId);
        if (timerModel == null) {
            log.error("执行回调错误，找不到对应的Timer。 timerId" + timerId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行回调错误，找不到对应的Timer。 timerId" + timerId);
        }

        if (timerModel.getStatus() != TimerStatus.Enable.getStatus()) {
            log.warn("Timer已经处于去激活状态。 timerId" + timerId);
            return;
        }

        // 触发时间的误差误差时间
        int gapTime = (int) (new Date().getTime() - taskModel.getRunTimer());
        taskModel.setCostTime(gapTime);

        // 执行http回调，通知业务放
        ResponseEntity<String> resp = null;
        try {
            resp = executeTimerCallBack(timerModel);
        } catch (Exception e) {
            log.error("执行回调失败，抛出异常e:" + e);
        }

        //后置处理，更新Timer执行结果
        if (resp == null) {
            taskModel.setStatus(TaskStatus.Failed.getStatus());
            taskModel.setOutput("resp is null");
        } else if (resp.getStatusCode().is2xxSuccessful()) {
            taskModel.setStatus(TaskStatus.Succeed.getStatus());
            taskModel.setOutput(resp.toString());
        } else {
            taskModel.setStatus(TaskStatus.Failed.getStatus());
            taskModel.setOutput(resp.toString());
        }

        taskMapper.update(taskModel);
    }

    private ResponseEntity<String> executeTimerCallBack(TimerModel timerModel) {
        TimerDTO timerDTO = TimerModel.objToVo(timerModel);
        NotifyHTTPParam httpParam = timerDTO.getNotifyHTTPParam();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resp = null;
        if ("POST".equals(httpParam.getMethod())) {
            resp = restTemplate.postForEntity(httpParam.getUrl(), httpParam.getBody(), String.class);
        } else {
            log.error("不支持的httpMethod");
        }
        HttpStatus statusCode = resp.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            log.error("http 回调失败：" + resp);
        }
        return resp;
    }
}
