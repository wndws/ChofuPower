package com.naninuneda.chofu.filter;

import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class GameInfoFilter extends EstimateFilter {

	public GameInfoFilter(Map<Agent,Species> resultMap,GameSetting gameSetting,GameInfo gameInfo){
		super();
		//霊媒師，占い師として占った結果，襲撃先は白より
		for(Agent agent:resultMap.keySet()){
			if(resultMap.get(agent).equals(Species.HUMAN)){
				map.put(agent, FilterResult.CREDIBLE);
			}
			map.put(agent, FilterResult.INCREDIBLE);
		}
		//さらに役職の人数から特定できる物に関して
		int wolf = 0;
		for(Agent agent:map.keySet()){
			if(map.get(agent).equals(FilterResult.INCREDIBLE)){
				wolf++;
			}
		}
		if(wolf == gameSetting.getRoleNum(Role.WEREWOLF)){
			for(Agent agent:gameInfo.getAgentList()){
				if(map.containsKey(agent)){
					map.put(agent, FilterResult.INCREDIBLE);
				}
			}
		}
	}

}