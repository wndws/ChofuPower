package com.naninuneda.chofu;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class Scenes implements Iterable<Scene> {

	private List<Scene> list;

	public Scenes(GameInfo gameInfo, GameSetting gameSetting){
		list = new LinkedList<Scene>();
		gameInfo.getAliveAgentList();

		int sceneMaxSize = 1;
		int num = gameSetting.getPlayerNum();
		for(Role role:gameSetting.getRoleNumMap().keySet()){
			sceneMaxSize = sceneMaxSize * Math.c
		}

	}

	@Override
	public Iterator<Scene> iterator() {
		return list.iterator();
	}



}
