package com.soyo.atimer.model;

import com.soyo.common.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskModel extends BaseModel implements Serializable {

    private Integer taskId;

    private String app;

    private Long timerId;

    private String output;

    private Long runTimer;

    private int costTime;

    private int status;
}
