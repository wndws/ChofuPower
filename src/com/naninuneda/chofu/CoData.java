package com.naninuneda.chofu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

public class CoData {

	//<ID>が<役>のCOを<役>のときに<何回>したか．1ゲームにつき1ロール1回を最大とする．

	private Map<Integer,Map<Role,Map<Role,Integer>>> data;

	public CoData(List<Agent> agents){

		data = new HashMap<Integer,Map<Role,Map<Role,Integer>>>();

		for(Agent agent : agents){
			data.put(agent.getAgentIdx(), new HashMap<Role,Map<Role,Integer>>());
			for(Role role : Role.values()){
				data.get(agent.getAgentIdx()).put(role,new HashMap<Role,Integer>());
			}
		}
	}

	public void inputResult(Map<Agent,Role> roleMap,Map<Agent,List<Role>> coMap){
		for(Agent agent:coMap.keySet()){
			Role real = roleMap.get(agent);
			List<Role> target = coMap.get(agent);
			for(Role role : target){
				Map<Role,Integer> map = data.get(agent.getAgentIdx()).get(role);
				if(map.containsKey(real)){
					int count = map.get(real);
					map.put(real, count + 1);
				}else{
					map.put(real, 1);
				}
			}
		}
	}

}
