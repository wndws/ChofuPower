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
	private Map<Agent,List<Role>> possibilities;
	private Role myRole;

	public CaseManager(GameInfo gameInfo, GameSetting gameSetting){
		roleNumMap = gameSetting.getRoleNumMap();
		possibilities = new TreeMap<Agent,List<Role>>();
		myRole = gameInfo.getRole();

		if(myRole.equals(Role.WEREWOLF)){

			List<Agent> friend = new ArrayList<Agent>();
			Map<Agent,Role> roleMap = gameInfo.getRoleMap();
			for(Agent agent : gameInfo.getAgentList()){
				if(roleMap.containsKey(agent)){
					if(roleMap.get(agent).equals(Role.WEREWOLF)){
						friend.add(agent);
					}
				}
			}

			for(Agent agent : gameInfo.getAgentList()){
				List<Role> roles = new LinkedList<Role>();
				if(friend.contains(agent)){
					roles.add(Role.WEREWOLF);
				}else{
					roles.addAll(getAllHumanRolesExceptMyRole());
				}
				possibilities.put(agent, roles);
			}

		}else{

			for(Agent agent : gameInfo.getAgentList()){
				List<Role> roles = new LinkedList<Role>();
				roles.addAll(getAllHumanRolesExceptMyRole());
				possibilities.put(agent, roles);
			}

		}
	}

	/*


	private Map<Agent,Role> down(Map<Agent,Role> roleMap){

		List<Role> ngRole = getAllRoles();

		Map<Role, Integer> roleNumMap = new HashMap<Role, Integer>();

		for(Agent agent : roleMap.keySet()){

		}

	}

	*/




	private List<Role> getAllHumanRoles(){
		List<Role> roles = getAllRoles();
		if(roles.contains(Role.WEREWOLF)){
			roles.remove(Role.WEREWOLF);
		}
		return roles;
	}

	private List<Role> getAllRoles(){
		List<Role> roles = new LinkedList<Role>();
		for(Role role : roleNumMap.keySet()){
			if(roleNumMap.get(role) > 0){
				roles.add(role);
			}
		}
		return roles;
	}

	private List<Role> getAllHumanRolesExceptMyRole(){
		List<Role> roles = getAllRolesExceptMyRole();
		if(roles.contains(Role.WEREWOLF)){
			roles.remove(Role.WEREWOLF);
		}
		return roles;
	}

	private List<Role> getAllRolesExceptMyRole(){
		List<Role> roles = new LinkedList<Role>();
		Map<Role,Integer> map = new HashMap<Role,Integer>(roleNumMap);
		for(Role role : map.keySet()){
			if(role.equals(myRole)){
				int num = map.get(role);
				map.put(role, num - 1);
			}
			if(map.get(role) > 0){
				roles.add(role);
			}
		}
		return roles;
	}



}
