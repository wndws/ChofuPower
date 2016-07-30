package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.HashMap;
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
	public List<Agent> voteTargets;
	public Random random;
	public GameInfo gameInfo;
	public GameSetting gameSetting;
	public int finishCount;
	public boolean co;
	public double entropy;

	public ChofuBaseRole(){
		todayTalkList = new ArrayList<Talk>();
		voteTargets = new ArrayList<Agent>();
		random = new Random();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		this.gameInfo = gameInfo;
		this.gameSetting = gameSetting;
		this.talkList = gameInfo.getTalkList();
		this.alives = gameInfo.getAliveAgentList();
		this.finishCount = 0;
		this.co = false;
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

		//疑惑先のエントロピーを求める
		Map<Agent,Integer> doubt = new HashMap<Agent,Integer>();
		for(Agent agent:gameInfo.getAgentList()){
			doubt.put(agent, 0);
		}
		int allVoteNum = 0;
		for(Talk talk:talkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.ESTIMATE) || utterance.getTopic().equals(Topic.VOTE)){
				int voteNum = doubt.get(utterance.getTarget());
				doubt.put(utterance.getTarget(),voteNum + 1);
				allVoteNum++;
			}
		}
		entropy = 0.0;
		if(allVoteNum != 0){
			for(Agent agent:gameInfo.getAgentList()){
				double p = (double)doubt.get(agent)/allVoteNum;
				entropy = entropy - (p)*(Math.log(p)/Math.log(2.0));
			}
		}

	}

	public void finish() {
		finishCount++;
		if(finishCount > 1){
			return;
		}
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

	public boolean isMyLoquacity(){

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
