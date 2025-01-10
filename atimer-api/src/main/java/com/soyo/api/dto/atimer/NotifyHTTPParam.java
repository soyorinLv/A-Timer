package com.soyo.api.dto.atimer;

import lombok.Data;

import java.util.Map;

/**
 * @author: lkl
 * @date: 2025/3/11 12:51
 */
@Data
public class NotifyHTTPParam {
    private String method;
    private String url;
    private Map<String,String> header;
    private String body;
}
