package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public abstract class ChofuBaseRole extends AbstractRole {

	public List<Agent> alives;
	public List<Talk> talkList, todayTalkList;
	public Agent vote;
	public List<Agent> voteTargets;

	public ChofuBaseRole(){
		todayTalkList = new ArrayList<Talk>();
		voteTargets = new ArrayList<Agent>();
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
		voteTargets.clear();
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		for(Talk talk : gameInfo.getTalkList()){
			if(talk.getDay() == getDay() && !(todayTalkList.contains(talk))){
				todayTalkList.add(talk);
			}
		}

		for(Talk talk:todayTalkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.VOTE)){
				voteTargets.add(utterance.getTarget());
			}
		}

	}

	public String getRandomVoteTalk(){

		Random rnd = new Random();

		if(!voteTargets.isEmpty()){
			return TemplateTalkFactory.vote(voteTargets.get(rnd.nextInt(voteTargets.size())));
		}

		return TemplateTalkFactory.vote(alives.get(rnd.nextInt(alives.size())));

	}

	public boolean isMyTalkOneBefore(){

		if(todayTalkList.isEmpty()){
			return false;
		}

		if(todayTalkList.get(todayTalkList.size() - 1).getAgent().equals(getMe())){
			return true;
		}

		return false;

	}

}
