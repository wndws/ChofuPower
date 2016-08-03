package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public abstract class ChofuBaseRole extends AbstractRole {

	public List<Agent> alives;
	public List<Talk> talkList, todayTalkList;
	public List<Agent> voteTargets;
	public Map<Agent,Role> coMap;
	public Random random;
	public GameInfo gameInfo;
	public GameSetting gameSetting;
	public int finishCount;
	public boolean co;
	public double entropy = 0.0,lastEntropy = 0.0;

	public ChofuBaseRole(){
		todayTalkList = new ArrayList<Talk>();
		voteTargets = new ArrayList<Agent>();
		coMap = new HashMap<Agent,Role>();
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
		for(Agent agent:alives){
			if(!agent.equals(getMe())){
				voteTargets.add(agent);
			}
		}
	}

	@Override
	public void update(GameInfo gameInfo){
		super.update(gameInfo);
		this.gameInfo = gameInfo;
		this.talkList = gameInfo.getTalkList();
		alives = gameInfo.getAliveAgentList();

		voteTargets.clear();
		for(Agent agent:alives){
			if(!agent.equals(getMe())){
				voteTargets.add(agent);
			}
		}

		for(Talk talk : gameInfo.getTalkList()){
			if(talk.getDay() == getDay() && !(todayTalkList.contains(talk))){
				//更新分にCOが存在する場合COマップを更新する
				if(new Utterance(talk.getContent()).getTopic().equals(Topic.COMINGOUT)){
					coMap.put(talk.getAgent(), new Utterance(talk.getContent()).getRole());
				}
				todayTalkList.add(talk);
			}
		}

		//疑惑先のエントロピーを求める
		Map<Agent,Double> doubt = new HashMap<Agent,Double>();
		for(Agent agent:gameInfo.getAgentList()){
			doubt.put(agent, 0.0);
		}
		int allVoteNum = 0;
		for(Talk talk:todayTalkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.VOTE)){
				double voteNum = doubt.get(utterance.getTarget());
				doubt.put(utterance.getTarget(),voteNum + 1);
				allVoteNum++;
			}
		}
		if(entropy > 0.0){
			lastEntropy = entropy;
		}
		entropy = 0.0;
		if(allVoteNum > 0){
			for(Agent agent:alives){
				double p = doubt.get(agent)/allVoteNum;
				if(p > 0){
					entropy = entropy - (p)*(Math.log(p)/Math.log(2.0));
				}
			}
		}
	}

	public void finish() {
		finishCount++;
		if(finishCount > 1){
			return;
		}
	}

	//役職に準じない適切な投票選択をする．怪しまれないことを第一目標としている．
	public Agent getProperVoteTarget() {
		//まず，疑惑先のエントロピーが，上昇した場合には候補からランダム，下降した場合には最大の疑惑先に投票する！
		double diff = entropy - lastEntropy;
		if(diff > 0.0){
			//上昇した場合
			List<Talk> talks = new ArrayList<Talk>();
			for(Talk talk:todayTalkList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.VOTE) &&
						voteTargets.contains(utterance.getTarget()) &&
						!talk.getAgent().equals(getMe())){
					talks.add(talk);
				}
			}
			if(!talks.isEmpty()){
				Talk talk = talks.get(random.nextInt(talks.size()));
				Utterance utterance = new Utterance(talk.getContent());
				return utterance.getTarget();
			}
			return voteTargets.get(random.nextInt(voteTargets.size()));
		}else{
			//下降した場合
			Map<Agent,Integer> doubt = new HashMap<Agent,Integer>();
			for(Agent agent:voteTargets){
				doubt.put(agent, 0);
			}
			for(Talk talk:todayTalkList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.VOTE) && voteTargets.contains(utterance.getTarget())){
					int voteNum = doubt.get(utterance.getTarget());
					doubt.put(utterance.getTarget(),voteNum + 1);
				}
			}
			List<Agent> maxes = new ArrayList<Agent>();
			maxes.add(voteTargets.get(random.nextInt(voteTargets.size())));
			for(Agent agent:doubt.keySet()){
				if(doubt.get(agent) > doubt.get(maxes.get(0))){
					maxes.clear();
					maxes.add(agent);
				}else if(doubt.get(agent) == doubt.get(maxes.get(0))){
					maxes.add(agent);
				}
			}
			return maxes.get(random.nextInt(maxes.size()));
		}
	}

	//饒舌であればtrue
	public boolean isLoquacity(Agent agent){
		List<Talk> talks = new ArrayList<Talk>();
		for(Talk talk:todayTalkList){
			if(talk.getAgent().equals(agent)){
				talks.add(talk);
			}
		}
		double talkPerPeople = (double) todayTalkList.size() / alives.size();

		if(talkPerPeople >= talks.size()){
			return false;
		}else{
			return true;
		}
	}

}
