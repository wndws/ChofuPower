package com.naninuneda.chofu;

import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class HistoryManager {

	private static int gameCount;

	static{
		gameCount = 1;
	}

	public HistoryManager(GameInfo gameInfo, GameSetting gameSetting){

	}


	public int getGameCount(){
		return gameCount;
	}

	public void addGameCount(){
		gameCount = gameCount + 1;
	}


}
