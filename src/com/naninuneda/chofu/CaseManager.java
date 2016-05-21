package com.naninuneda.chofu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class CaseManager {

	private Map<Role,Integer> roleNumMap;
	private Map<Agent,List<Role>> possibilitys;

	public CaseManager(GameInfo gameInfo, GameSetting gameSetting){
		roleNumMap = gameSetting.getRoleNumMap();
		possibilitys = new TreeMap<Agent,List<Role>>();
		if(gameInfo.getRole().equals(Role.WEREWOLF)){
			List<Agent> friends = new ArrayList<Agent>();
			for(Agent agent : gameInfo.getRoleMap().keySet()){
				if(gameInfo.getRoleMap().get(agent).equals(Role.WEREWOLF)){
					friends.add(agent);
				}
			}
			for(Agent agent: gameInfo.getAgentList()){
				if(agent.equals(gameInfo.getAgent())){
					List<Role> roles = new ArrayList<Role>();
					roles.add(Role.WEREWOLF);
					possibilitys.put(agent, roles);
					continue;
				}
				if(friends.contains(agent)){
					List<Role> roles = new ArrayList<Role>();
					roles.add(Role.WEREWOLF);
					possibilitys.put(agent, roles);
					continue;
				}
				possibilitys.put(agent, getAllHumanRoles());
			}
		}else{
			for(Agent agent: gameInfo.getAgentList()){
				if(agent.equals(gameInfo.getAgent())){
					List<Role> roles = new ArrayList<Role>();
					roles.add(gameInfo.getRole());
					possibilitys.put(agent, roles);
					continue;
				}
				possibilitys.put(agent, getAllRoles());
			}
		}
	}

	/*
	private List<Map<Agent,Role>> down(List<Map<Agent,Role>> ancestors){
		for(Map<Agent,Role> map : ancestors){

		}
	}
	*/



	private static List<Role> getAllHumanRoles(){
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.BODYGUARD);
		roles.add(Role.MEDIUM);
		roles.add(Role.POSSESSED);
		roles.add(Role.VILLAGER);
		roles.add(Role.SEER);
		return roles;
	}

	private static List<Role> getAllRoles(){
		List<Role> roles = new ArrayList<Role>();
		roles.add(Role.BODYGUARD);
		roles.add(Role.MEDIUM);
		roles.add(Role.POSSESSED);
		roles.add(Role.VILLAGER);
		roles.add(Role.SEER);
		roles.add(Role.WEREWOLF);
		return roles;
	}



}
