package com.naninuneda.chofu;

import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class HistoryManager {

	public static Map<Integer,Integer> scoreMap;
	public static Map<Integer,Map<Role,Float>> coProbability,seerResultEffectivity;

	static{
		scoreMap = new HashMap<Integer,Integer>();
		coProbability = new HashMap<Integer,Map<Role,Float>>();
		seerResultEffectivity = new HashMap<Integer,Map<Role,Float>>();
	}

	public HistoryManager(GameInfo gameInfo, GameSetting gameSetting){

	}

	public Integer getScore(Integer id){
		if(scoreMap.containsKey(id)){
			return scoreMap.get(id);
		}
		return 0;
	}

	public Float getCoProbability(Integer id, Role role){
		if(coProbability.containsKey(id)){
			Map<Role,Float> roleMap = coProbability.get(id);
			if(roleMap.containsKey(role)){
				return roleMap.get(role);
			}
		}
		return (float) 0;
	}

	public Float getSeerResultEffectivity(Integer id, Role role){
		if(seerResultEffectivity.containsKey(id)){
			Map<Role,Float> roleMap = seerResultEffectivity.get(id);
			if(roleMap.containsKey(role)){
				return roleMap.get(role);
			}
		}
		return (float) 0;
	}

}
