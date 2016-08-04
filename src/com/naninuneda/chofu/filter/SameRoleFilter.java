package com.naninuneda.chofu.filter;

import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

public class SameRoleFilter extends EstimateFilter {

	public SameRoleFilter(Map<Agent,Role> roleMap,Role myRole){

		super();

		if(roleMap.containsValue(myRole)){
			for(Agent agent:roleMap.keySet()){
				if(roleMap.get(agent).equals(myRole)){
					map.put(agent, FilterResult.INCREDIBLE);
				}
			}
		}
	}
}
