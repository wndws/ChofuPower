package com.naninuneda.chofu.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

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

	protected void estimateFromNumber(List<Agent> agentList,Map<Role,Integer> roleNumberMap){
		//さらに役職の人数から特定できる物に関して
		int wolf = 0,people = 0;
		for(Agent agent:map.keySet()){
			if(map.get(agent).equals(FilterResult.INCREDIBLE)){
				wolf++;
			}else{
				people++;
			}
		}
		if(wolf == roleNumberMap.get(Role.WEREWOLF)){
			for(Agent agent:agentList){
				if(!map.containsKey(agent)){
					map.put(agent, FilterResult.CREDIBLE);
				}
			}
		}else if(people == agentList.size() - roleNumberMap.get(Role.WEREWOLF)){
			for(Agent agent:agentList){
				if(!map.containsKey(agent)){
					map.put(agent, FilterResult.INCREDIBLE);
				}
			}
		}
	}

}
