package com.naninuneda.chofu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class HistoryManager {

	private static int gameCount;
	private static Map<Integer,Integer> scoreMap;
	private static Map<Integer,Map<Role,Float>> seerResultEffectivity,selfish;
	private static Map<Integer,Map<Role,Map<Role,Float>>> coProbability;
	private Map<Integer,Integer> seerResultEffectCountMap,seerCount,selfishCountMap,voteCount;
	private Map<Integer,List<Role>> coCountMap;

	static{
		gameCount = 1;
		scoreMap = new HashMap<Integer,Integer>();
		coProbability = new HashMap<Integer,Map<Role,Map<Role,Float>>>();
		selfish = new HashMap<Integer,Map<Role,Float>>();
		seerResultEffectivity = new HashMap<Integer,Map<Role,Float>>();
	}

	public HistoryManager(GameInfo gameInfo, GameSetting gameSetting){

		seerResultEffectCountMap = new HashMap<Integer,Integer>();
		seerCount = new HashMap<Integer,Integer>();
		selfishCountMap = new HashMap<Integer,Integer>();
		voteCount = new HashMap<Integer,Integer>();

		if(scoreMap.isEmpty()){
			for(Agent agent:gameInfo.getAgentList()){
				if(!agent.equals(gameInfo.getAgent())){
					scoreMap.put(agent.getAgentIdx(), 0);
				}
			}
		}
		if(seerResultEffectivity.isEmpty()){
			for(Agent agent:gameInfo.getAgentList()){
				if(!agent.equals(gameInfo.getAgent())){
					Map<Role,Float> roleMap = new HashMap<Role,Float>();
					seerResultEffectivity.put(agent.getAgentIdx(), roleMap);
				}
			}
		}
		if(selfish.isEmpty()){
			for(Agent agent:gameInfo.getAgentList()){
				if(!agent.equals(gameInfo.getAgent())){
					Map<Role,Float> roleMap = new HashMap<Role,Float>();
					selfish.put(agent.getAgentIdx(), roleMap);
				}
			}
		}
		if(coProbability.isEmpty()){
			for(Agent agent:gameInfo.getAgentList()){
				if(!agent.equals(gameInfo.getAgent())){
					Map<Role,Map<Role,Float>> roleMap = new HashMap<Role,Map<Role,Float>>();
					coProbability.put(agent.getAgentIdx(), roleMap);
				}
			}
		}

		for(Agent agent:gameInfo.getAgentList()){
			if(!agent.equals(gameInfo.getAgent())){
				seerResultEffectCountMap.put(agent.getAgentIdx(), 0);
				seerCount.put(agent.getAgentIdx(), 0);
				selfishCountMap.put(agent.getAgentIdx(), 0);
				voteCount.put(agent.getAgentIdx(), 0);
				List<Role> roleList = new ArrayList<Role>();
				coCountMap.put(agent.getAgentIdx(), roleList);
			}
		}
	}

	public static int getGameCount(){
		return gameCount;
	}

	public static Integer getScore(Integer id){
		if(scoreMap.containsKey(id)){
			return scoreMap.get(id);
		}
		return 0;
	}

	public static Float getCoProbability(Integer id, Role realRole, Role coRole){
		if(coProbability.containsKey(id)){
			Map<Role,Map<Role,Float>> realRoleMap = coProbability.get(id);
			if(realRoleMap.containsKey(realRole)){
				Map<Role,Float> coRoleMap = realRoleMap.get(realRole);
				if(coRoleMap.containsKey(coRole)){
					return coRoleMap.get(coRole);
				}
			}
		}
		return (float) 0;
	}

	public static Float getSeerResultEffectivity(Integer id, Role role){
		if(seerResultEffectivity.containsKey(id)){
			Map<Role,Float> roleMap = seerResultEffectivity.get(id);
			if(roleMap.containsKey(role)){
				return roleMap.get(role);
			}
		}
		return (float) 0;
	}

	public static Float getSelfish(Integer id, Role role){
		if(seerResultEffectivity.containsKey(id)){
			Map<Role,Float> roleMap = seerResultEffectivity.get(id);
			if(roleMap.containsKey(role)){
				return roleMap.get(role);
			}
		}
		return (float) 0;
	}

	public void countSeerEffect(Integer id){
		int count = seerResultEffectCountMap.get(id);
		seerResultEffectCountMap.put(id, count+1);
	}

	public void countSelfish(Integer id){
		int count = selfishCountMap.get(id);
		selfishCountMap.put(id, count+1);
	}

	public void countCo(Integer id,Role role){
		coCountMap.get(id).add(role);
	}

	public void countSeer(Integer id){
		int count = seerCount.get(id);
		seerCount.put(id, count + 1);
	}

	public void countVote(Integer id){
		int count = voteCount.get(id);
		voteCount.put(id, count + 1);
	}

	public void aggregate(GameInfo gameInfo){

		Map<Agent,Role> lastRoleMap = gameInfo.getRoleMap();

		for(Agent agent : lastRoleMap.keySet()){

			int id = agent.getAgentIdx();

			//seer
			Map<Role,Float> roleMap = seerResultEffectivity.get(id);
			float valueThisGame = (float) seerResultEffectCountMap.get(id) / seerCount.get(id);
			if(roleMap.containsKey(lastRoleMap.get(agent))){
				float valuePreviousGame = roleMap.get(lastRoleMap.get(agent));
				float result = (valuePreviousGame * gameCount + valueThisGame) / gameCount + 1;
				roleMap.put(lastRoleMap.get(agent), result);
			}else{
				roleMap.put(lastRoleMap.get(agent), valueThisGame);
			}

			//selfish
			roleMap = selfish.get(id);
			valueThisGame = (float) selfishCountMap.get(id) / voteCount.get(id);
			if(roleMap.containsKey(lastRoleMap.get(agent))){
				float valuePreviousGame = roleMap.get(lastRoleMap.get(agent));
				float result = (valuePreviousGame * gameCount + valueThisGame) / gameCount + 1;
				roleMap.put(lastRoleMap.get(agent), result);
			}else{
				roleMap.put(lastRoleMap.get(agent), valueThisGame);
			}

			//CO
			Map<Role,Float> roleRoleMap = coProbability.get(id).get(lastRoleMap.get(agent));
			for(Role role:coCountMap.get(id)){
				if(roleRoleMap.containsKey(role)){
					float valuePreviousGame = roleRoleMap.get(lastRoleMap.get(agent));
					float result = (valuePreviousGame * gameCount + 1) / gameCount + 1;
					roleRoleMap.put(lastRoleMap.get(agent), result);
				}else{
					roleRoleMap.put(lastRoleMap.get(agent), (float)1);
				}
			}
		}

		boolean humanWin = true;;
		for(Agent agent:gameInfo.getAliveAgentList()){
			if(lastRoleMap.get(agent).equals(Role.WEREWOLF)){
				humanWin = false;
				break;
			}
		}

		for(Agent agent : lastRoleMap.keySet()){
			int score = scoreMap.get(agent.getAgentIdx());
			if(lastRoleMap.get(agent).equals(Role.WEREWOLF)){
				if(!humanWin){
					scoreMap.put(agent.getAgentIdx(), score + 1);
				}
			}else{
				if(humanWin){
					scoreMap.put(agent.getAgentIdx(), score + 1);
				}
			}
		}

		gameCount++;
	}
}
