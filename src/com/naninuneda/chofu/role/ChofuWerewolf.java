package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.TemplateWhisperFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class ChofuWerewolf extends ChofuBaseRole {

	public List<Agent> wolfs,humans;
	public List<Talk> whisperList, todayWhisperList;
	public Agent attack;

	public boolean targetDeclareWhisper;


	public ChofuWerewolf() {
		super();
		wolfs = new ArrayList<Agent>();
		humans = new ArrayList<Agent>();
		todayWhisperList = new ArrayList<Talk>();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){

		super.initialize(gameInfo, gameSetting);

		Map<Agent,Role> roleMap = gameInfo.getRoleMap();
		for(Agent agent : gameInfo.getAgentList()){
			if(roleMap.containsKey(agent)){
				if(roleMap.get(agent).equals(Role.WEREWOLF)){
					wolfs.add(agent);
				}
			}
		}

		for(Agent agent : gameInfo.getAgentList()){
			if(!wolfs.contains(agent)){
				humans.add(agent);
			}
		}

		whisperList = gameInfo.getWhisperList();
	}

	@Override
	public void dayStart() {
		super.dayStart();
		todayWhisperList.clear();
		targetDeclareWhisper = false;
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		for(Talk talk : gameInfo.getWhisperList()){
			if(talk.getDay() == getDay() && !(todayWhisperList.contains(talk))){
				todayWhisperList.add(talk);
			}
		}

	}

	@Override
	public String whisper() {

		if(!todayWhisperList.isEmpty()){
			List<Agent> targetList = new ArrayList<Agent>();
			List<Agent> voteList = new ArrayList<Agent>();
			for(Talk talk : todayWhisperList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.ATTACK)){
					targetList.add(utterance.getTarget());
				}else if(utterance.getTopic().equals(Topic.VOTE)){
					voteList.add(utterance.getTarget());
				}
			}

			if(!targetList.isEmpty()){
				Random rnd = new Random();
				attack = targetList.get(rnd.nextInt(targetList.size()));
				if(!targetDeclareWhisper){
					targetDeclareWhisper = true;
					return TemplateWhisperFactory.attack(attack);
				}
			}

		}

		if(!targetDeclareWhisper){
			List<Agent> aliveHumans = new ArrayList<Agent>();
			for(Agent agent : humans){
				if(alives.contains(agent)){
					aliveHumans.add(agent);
				}
			}

			Map<Agent,Role> coMap = new HashMap<Agent, Role>();
			for(Agent agent : alives){
				for(Talk talk : talkList){
					if(talk.getAgent().equals(agent)){
						Utterance utterance = new Utterance(talk.getContent());
						if(utterance.getTopic().equals(Topic.COMINGOUT)){
							coMap.put(agent, utterance.getRole());
						}
					}
				}
			}

			List<Agent> targetList = new ArrayList<Agent>();
			if(!coMap.isEmpty()){
				for(Agent agent : coMap.keySet()){
					if(coMap.get(agent).equals(Role.SEER)){
						targetList.add(agent);
					}else if(coMap.get(agent).equals(Role.POSSESSED)){
						targetList.add(agent);
					}
				}
			}

			Random rnd = new Random();
			if(!targetList.isEmpty()){
				attack = targetList.get(rnd.nextInt(targetList.size()));
				targetDeclareWhisper = true;
				return TemplateWhisperFactory.attack(attack);
			}
			targetDeclareWhisper = true;
			attack = aliveHumans.get(rnd.nextInt(aliveHumans.size()));
			return TemplateWhisperFactory.attack(attack);
		}

		return TemplateWhisperFactory.over();

	}

	@Override
	public String talk() {

		if(!isMyTalkOneBefore()){
			return getRandomVoteTalk();
		}

		return TemplateTalkFactory.over();
	}

	@Override
	public Agent attack() {
		return attack;
	}

	@Override
	public Agent divine() {
		return null;
	}

	@Override
	public Agent guard() {
		return null;
	}

	@Override
	public Agent vote() {
		return getRandomVote();
	}

	@Override
	public void finish() {

	}

	@Override
	public String getRandomVoteTalk(){
		return TemplateTalkFactory.vote(getRandomVote());
	}

	@Override
	public Agent getRandomVote(){

		Random rnd = new Random();

		if(!voteTargets.isEmpty()){
			return voteTargets.get(rnd.nextInt(voteTargets.size()));
		}

		List<Agent> aliveHumans = new ArrayList<Agent>();
		for(Agent agent : humans){
			if(alives.contains(agent)){
				aliveHumans.add(agent);
			}
		}

		return aliveHumans.get(rnd.nextInt(aliveHumans.size()));

	}


}
