package cn.joytur.common.mvc.dto;

import cn.joytur.common.mvc.constant.Enums;
import cn.joytur.common.mvc.constant.Enums.SortType;

/**
 * 排序
 * @author xuhang
 * @time 2019年1月7日
 */
public class Sort {

	private String name;
	private Enums.SortType sortType;
	
	public Sort(String name, SortType sortType) {
		super();
		this.name = name;
		this.sortType = sortType;
	}

	public String getName() {
		return name;
	}

	public Enums.SortType getSortType() {
		return sortType;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSortType(Enums.SortType sortType) {
		this.sortType = sortType;
	}
	
}
