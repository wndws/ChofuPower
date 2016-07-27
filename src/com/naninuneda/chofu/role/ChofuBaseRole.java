package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.naninuneda.chofu.HistoryManager;

public abstract class ChofuBaseRole extends AbstractRole {

	public List<Agent> alives;
	public List<Talk> talkList, todayTalkList;
	public List<Agent> voteTargets;
	public Random random;
	public HistoryManager history;
	public GameInfo gameInfo;
	public int finishCount;

	public ChofuBaseRole(){
		todayTalkList = new ArrayList<Talk>();
		voteTargets = new ArrayList<Agent>();
		random = new Random();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		this.gameInfo = gameInfo;
		history = new HistoryManager(gameInfo,gameSetting);
		talkList = gameInfo.getTalkList();
		alives = gameInfo.getAliveAgentList();
		finishCount = 0;
	}

	@Override
	public void dayStart() {
		todayTalkList.clear();
		voteTargets.clear();
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		alives = gameInfo.getAliveAgentList();

		for(Talk talk : gameInfo.getTalkList()){
			if(talk.getDay() == getDay() && !(todayTalkList.contains(talk))){
				todayTalkList.add(talk);
			}
		}

		for(Talk talk:todayTalkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.VOTE)){
				if(!utterance.getTarget().equals(getMe()) && alives.contains(utterance.getTarget())){
					voteTargets.add(utterance.getTarget());
				}
			}
		}

	}

	public void finish() {
		finishCount++;
		if(finishCount > 1){
			return;
		}
		history.addGameCount();
		System.out.println("\nゲームカウント" + history.getGameCount() + "\n");
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

	public Agent getRandomVoteTarget() {

		if(!voteTargets.isEmpty()){
			return voteTargets.get(random.nextInt(voteTargets.size()));
		}

		return alives.get(random.nextInt(alives.size()));
	}

	public String getRandomVoteTalk(){

		return TemplateTalkFactory.vote(getRandomVoteTarget());

	}

	public boolean isLoquacity(){

		List<Talk> myTalks = new ArrayList<Talk>();
		for(Talk talk:todayTalkList){
			if(talk.getAgent().equals(getMe())){
				myTalks.add(talk);
			}
		}

		double talkPerPeople = (double) todayTalkList.size() / alives.size();

		if(talkPerPeople >= myTalks.size()){
			return false;
		}else{
			return true;
		}
	}

}
