package com.naninuneda.chofu.filter;

import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class OnePersonCoFilter extends EstimateFilter {

	public OnePersonCoFilter(EstimateFilter upperFilter,Map<Agent,Role> coMap,GameSetting gameSetting,GameInfo gameInfo){
		super(upperFilter);
		for(Agent agent:coMap.keySet()){
			Role role = coMap.get(agent);
			boolean only = true;
			for(Agent agent2:coMap.keySet()){
				if(!agent2.equals(agent) && role.equals(coMap.get(agent2))){
					only = false;
					break;
				}
			}
			if(only){
				//上書きしない
				if(!map.containsKey(agent)){
					map.put(agent, FilterResult.CREDIBLE);
				}
			}
		}
		estimateFromNumber(gameInfo.getAgentList(),gameSetting.getRoleNumMap());
	}

}
