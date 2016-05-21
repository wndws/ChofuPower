package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public abstract class ChofuBaseRole extends AbstractRole {

	public List<Agent> alives;
	public List<Talk> talkList, todayTalkList;
	public Agent vote;

	public boolean voteDeclareTalk;

	public ChofuBaseRole(){
		todayTalkList = new ArrayList<Talk>();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){

		super.initialize(gameInfo, gameSetting);

		talkList = gameInfo.getTalkList();
		alives = gameInfo.getAliveAgentList();
	}

	@Override
	public void dayStart() {
		todayTalkList.clear();
		voteDeclareTalk = false;
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		for(Talk talk : gameInfo.getTalkList()){
			if(talk.getDay() == getDay() && !(todayTalkList.contains(talk))){
				todayTalkList.add(talk);
			}
		}

	}

}
