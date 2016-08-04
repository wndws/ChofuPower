package com.naninuneda.chofu.filter;

import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Agent;

public abstract class EstimateFilter implements Filter {

	Map<Agent,FilterResult> map;

	public EstimateFilter(){
		map = new HashMap<Agent,FilterResult>();
	}

	public Map<Agent,FilterResult> getMap(){
		return map;
	}

	@Override
	public FilterResult getResult(Agent agent) {
		if(map.containsKey(agent)){
			return map.get(agent);
		}
		return FilterResult.UNKNOWN;
	}

}
