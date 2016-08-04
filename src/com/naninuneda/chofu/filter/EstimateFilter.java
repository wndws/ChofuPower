package com.naninuneda.chofu.filter;

import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Agent;

public abstract class EstimateFilter {

	Map<Agent,FilterResult> map;

	public EstimateFilter(){
		map = new HashMap<Agent,FilterResult>();
	}

	public EstimateFilter(EstimateFilter upperFilter){
		map = upperFilter.getMap();
	}

	public Map<Agent,FilterResult> getMap(){
		return map;
	}

}
