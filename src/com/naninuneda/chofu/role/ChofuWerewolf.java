package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.TemplateWhisperFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.naninuneda.chofu.ChofuPower;

public class ChofuWerewolf extends ChofuBaseRole {

	public Map<Agent,Species> species;
	public List<Agent> aliveHumans;
	public List<Talk> whisperList, todayWhisperList;
	public Agent attack;

	public boolean targetDeclareWhisper;


	public ChofuWerewolf(ChofuPower chofuPower) {
		super();
		species = new HashMap<Agent,Species>();
		aliveHumans = new ArrayList<Agent>();
		todayWhisperList = new ArrayList<Talk>();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo, gameSetting);
		Map<Agent,Role> roleMap = gameInfo.getRoleMap();
		for(Agent agent : gameInfo.getAgentList()){
			if(roleMap.containsKey(agent)){
				if(roleMap.get(agent).equals(Role.WEREWOLF)){
					species.put(agent,Species.WEREWOLF);
					continue;
				}
			}
			species.put(agent,Species.HUMAN);
		}
		whisperList = gameInfo.getWhisperList();
	}

	@Override
	public void dayStart() {
		super.dayStart();
		todayWhisperList.clear();
		targetDeclareWhisper = false;
		aliveHumans.clear();
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		for(Talk talk : gameInfo.getWhisperList()){
			if(talk.getDay() == getDay() && !(todayWhisperList.contains(talk))){
				todayWhisperList.add(talk);
			}
		}

		for(Agent agent : species.keySet()){
			if(alives.contains(agent) && species.get(agent).equals(Species.HUMAN)){
				aliveHumans.add(agent);
			}
		}

	}

	@Override
	public String whisper() {

		if(!isMyWhisperOneBefore()){
			if(attack != getRandomAttackTarget()){
				attack = getRandomAttackTarget();
				return TemplateWhisperFactory.attack(attack);
			}
		}

		return TemplateWhisperFactory.over();

	}

	@Override
	public String talk() {

		return TemplateTalkFactory.over();
	}

	@Override
	public Agent attack() {
		return attack;
	}

	@Override
	public Agent guard() {
		return null;
	}

	@Override
	public Agent vote() {
		return null;
	}

	public boolean isMyWhisperOneBefore(){

		if(todayWhisperList.isEmpty()){
			return false;
		}

		if(todayWhisperList.get(todayWhisperList.size() - 1).getAgent().equals(getMe())){
			return true;
		}

		return false;

	}

	public Agent getRandomAttackTarget(){

		List<Agent> targetList = new ArrayList<Agent>();

		if(!todayWhisperList.isEmpty()){
			for(Talk talk : todayWhisperList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.ATTACK)){
					targetList.add(utterance.getTarget());
				}
			}
		}

		if(!targetList.isEmpty()){
			return targetList.get(random.nextInt(targetList.size()));
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

		if(!coMap.isEmpty()){
			for(Agent agent : coMap.keySet()){
				if(coMap.get(agent).equals(Role.SEER)){
					targetList.add(agent);
				}else if(coMap.get(agent).equals(Role.POSSESSED)){
					targetList.add(agent);
				}
			}
		}

		if(!targetList.isEmpty()){
			return targetList.get(random.nextInt(targetList.size()));
		}

		return aliveHumans.get(random.nextInt(aliveHumans.size()));

	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public Agent divine() {
		return null;
	}


}
