package com.naninuneda.chofu.filter;

import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class SameRoleFilter extends EstimateFilter {

	public SameRoleFilter(EstimateFilter upperFilter,Map<Agent,Role> coMap,Role myRole,GameSetting gameSetting,GameInfo gameInfo){
		super(upperFilter);
		if(coMap.containsValue(myRole)){
			for(Agent agent:coMap.keySet()){
				if(coMap.get(agent).equals(myRole) && !map.containsKey(agent)){
					//上書きしない
					map.put(agent, FilterResult.INCREDIBLE);
				}
			}
		}
		estimateFromNumber(gameInfo.getAgentList(),gameSetting.getRoleNumMap());
	}
}
