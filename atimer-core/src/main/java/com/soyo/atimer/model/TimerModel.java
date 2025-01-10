package com.soyo.atimer.model;

import com.soyo.api.dto.atimer.NotifyHTTPParam;
import com.soyo.api.dto.atimer.TimerDTO;
import com.soyo.common.model.BaseModel;
import com.soyo.atimer.utils.JSONUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
@EqualsAndHashCode(callSuper = true)
@Data
public class TimerModel extends BaseModel implements Serializable {

    private Long timerId;

    private String app;

    private String name;

    private int status;

    private String cron;

    private String notifyHTTPParam;

    /**
     * 包装类转对象
     *
     * @param timerDTO
     * @return
     */
    public static TimerModel voToObj(TimerDTO timerDTO) {
        if (timerDTO == null) {
            return null;
        }
        TimerModel timerModel = new TimerModel();
        timerModel.setApp(timerDTO.getApp());
        timerModel.setTimerId(timerDTO.getTimerId());
        timerModel.setName(timerDTO.getName());
        timerModel.setStatus(timerDTO.getStatus());
        timerModel.setCron(timerDTO.getCron());
        timerModel.setNotifyHTTPParam(JSONUtil.toJsonString(timerDTO.getNotifyHTTPParam()));
        return timerModel;
    }

    /**
     * 对象转包装类
     *
     * @param timerModel
     * @return
     */
    public static TimerDTO objToVo(TimerModel timerModel) {
        if (timerModel == null) {
            return null;
        }
        TimerDTO timerDTO = new TimerDTO();
        timerDTO.setApp(timerModel.getApp());
        timerDTO.setTimerId(timerModel.getTimerId());
        timerDTO.setName(timerModel.getName());
        timerDTO.setStatus(timerModel.getStatus());
        timerDTO.setCron(timerModel.getCron());

        NotifyHTTPParam httpParam = JSONUtil.parseObject(timerModel.getNotifyHTTPParam(),NotifyHTTPParam.class);
        timerDTO.setNotifyHTTPParam(httpParam);

        BeanUtils.copyProperties(timerModel, timerDTO);
        return timerDTO;
    }
}
