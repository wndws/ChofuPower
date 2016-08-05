package com.naninuneda.chofu.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class SameRoleFilter extends EstimateFilter {

	public SameRoleFilter(EstimateFilter upperFilter,Map<Agent,Role> coMap,Role myRole,Agent me,GameSetting gameSetting,GameInfo gameInfo){
		super(upperFilter);
		if(coMap.containsValue(myRole)){
			for(Agent agent:coMap.keySet()){
				if(!agent.equals(me)){
					//他人と自分の矛盾検知
					if(coMap.get(agent).equals(myRole) && !map.containsKey(agent) && gameSetting.getRoleNum(myRole) == 1){
						//上書きしない
						map.put(agent, FilterResult.INCREDIBLE);
					}
				}
			}
		}
		//上位フィルターによって白推定されていてCOしている人を抜き出しそしてその役職の人数が1
		List<Agent> agents = new ArrayList<Agent>();
		for(Agent agent:map.keySet()){
			if(map.get(agent).equals(FilterResult.CREDIBLE) &&
					coMap.containsKey(agent) &&
					!agent.equals(me)){
				if(gameSetting.getRoleNum(coMap.get(agent)) == 1){
					agents.add(agent);
				}
			}
		}
		//その役職の人数が1である時，推定のないエージェントは黒
		for(Agent agent1:agents){
			for(Agent agent2:coMap.keySet()){
				if(coMap.get(agent1).equals(coMap.get(agent2))){
					if(!map.containsKey(agent2)){
						map.put(agent2, FilterResult.INCREDIBLE);
					}
				}
			}
		}

		estimateFromNumber(gameInfo.getAgentList(),gameSetting.getRoleNumMap());
	}
}
