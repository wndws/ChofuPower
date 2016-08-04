package com.naninuneda.chofu.filter;

import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

public class OnePersonCoFilter extends EstimateFilter {

	public OnePersonCoFilter(Map<Agent,Role> coMap){
		super();
		for(Agent agent:coMap.keySet()){
			if(map.containsKey(agent)){
				map.remove(agent);
			}else{
				map.put(agent, FilterResult.CREDIBLE);
			}
		}
	}

}
