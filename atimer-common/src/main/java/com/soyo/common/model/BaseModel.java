package com.soyo.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class BaseModel implements Serializable {

	/**
	 * 创建时间
	 */
	protected Date createTime;

	/**
	 * 更新时间
	 */
	protected Date modifyTime;

	@Override
	public String toString() {
		return "BaseModel{" + "createTime=" + createTime + ", updateTime=" + modifyTime + '}';
	}

}