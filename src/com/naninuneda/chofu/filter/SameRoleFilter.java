package com.naninuneda.chofu.filter;

import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

public class SameRoleFilter extends EstimateFilter {

	public SameRoleFilter(EstimateFilter upperFilter,Map<Agent,Role> coMap,Role myRole){
		super(upperFilter);
		if(coMap.containsValue(myRole)){
			for(Agent agent:coMap.keySet()){
				if(coMap.get(agent).equals(myRole) && !map.containsKey(agent)){
					//上書きしない
					map.put(agent, FilterResult.INCREDIBLE);
				}
			}
		}
	}
}
