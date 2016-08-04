package com.naninuneda.chofu.filter;

import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class GameInfoFilter extends EstimateFilter {

	public GameInfoFilter(Map<Agent,Species> resultMap,GameSetting gameSetting,GameInfo gameInfo){
		super();
		//霊媒師，占い師として占った結果，襲撃先は白よりresultMapの情報をそのままインプット
		for(Agent agent:resultMap.keySet()){
			if(resultMap.get(agent).equals(Species.HUMAN)){
				map.put(agent, FilterResult.CREDIBLE);
			}
			map.put(agent, FilterResult.INCREDIBLE);
		}
		estimateFromNumber(gameInfo.getAgentList(),gameSetting.getRoleNumMap());
	}

}