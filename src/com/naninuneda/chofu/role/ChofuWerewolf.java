package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aiwolf.client.base.player.AbstractRole;
import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.TemplateWhisperFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class ChofuWerewolf extends AbstractRole {

	public List<Agent> wolfs,alives,humans;
	public List<Talk> talkList, todayTalkList, whisperList, todayWhisperList;
	public Agent target, vote;

	public boolean voteDeclareTalk,targetDeclareWhisper,voteDeclareWhisper;


	public ChofuWerewolf() {
		wolfs = new ArrayList<Agent>();
		humans = new ArrayList<Agent>();
		todayTalkList = new ArrayList<Talk>();
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

		talkList = gameInfo.getTalkList();
		whisperList = gameInfo.getWhisperList();
	}

	@Override
	public void dayStart() {
		todayTalkList.clear();
		todayWhisperList.clear();
		voteDeclareTalk = false;
		targetDeclareWhisper = false;
		voteDeclareWhisper = false;
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		for(Talk talk : gameInfo.getTalkList()){
			if(talk.getDay() == this.getDay() && !(todayTalkList.contains(talk))){
				todayTalkList.add(talk);
			}
		}

		for(Talk talk : gameInfo.getWhisperList()){
			if(talk.getDay() == this.getDay() && !(todayWhisperList.contains(talk))){
				todayWhisperList.add(talk);
			}
		}

		talkList = gameInfo.getTalkList();
		whisperList = gameInfo.getWhisperList();
		alives = gameInfo.getAliveAgentList();

	}

	@Override
	public String whisper() {

		if(!todayWhisperList.isEmpty()){
			//譛ｬ譌･縺ｮ縺輔＆繧・″縺後≠繧・
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
				//繧ｿ繝ｼ繧ｲ繝・ヨ豎ｺ螳壹↓髢｢縺吶ｋ縺輔＆繧・″縺後≠縺｣縺溷ｴ蜷・
				//繝ｩ繝ｳ繝繝豎ｺ螳夲ｼ郁ｦ∵隼蝟・ｼ・
				Random rnd = new Random();
				target = targetList.get(rnd.nextInt(targetList.size()));
				if(!targetDeclareWhisper){
					//縺輔＆繧・″縺ｧ縺ｮ謾ｻ謦・ｮ｣險繧偵∪縺縺励※縺・↑縺・
					targetDeclareWhisper = true;
					return TemplateWhisperFactory.attack(target);
				}
			}

			if(!voteList.isEmpty()){
				//謚慕･ｨ豎ｺ螳壹↓髢｢縺吶ｋ縺輔＆繧・″縺後≠縺｣縺溷ｴ蜷・
				//繝ｩ繝ｳ繝繝豎ｺ螳夲ｼ郁ｦ∵隼蝟・ｼ・
				Random rnd = new Random();
				vote = voteList.get(rnd.nextInt(voteList.size()));
				if(!voteDeclareWhisper){
					//縺輔＆繧・″縺ｧ縺ｮ謚慕･ｨ螳｣險繧偵∪縺縺励※縺・↑縺・
					voteDeclareWhisper = true;
					return TemplateWhisperFactory.vote(vote);
				}
			}

		}

		if(!targetDeclareWhisper){
			//縺ｾ縺謾ｻ謦・ｮ｣險繧偵＠縺ｦ縺・↑縺・・謾ｻ謦・岼讓吶ｒ豎ｺ繧√ｋ

			//逕溘″縺ｦ縺・ｋ莠ｺ髢薙・驟榊・繧剃ｽ懈・
			List<Agent> aliveHumans = new ArrayList<Agent>();
			for(Agent agent : humans){
				if(alives.contains(agent)){
					aliveHumans.add(agent);
				}
			}

			//驕主悉縺ｫCO繧偵＠縺ｦ縺・ｋ縺九・
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
				//繧ｫ繝溘Φ繧ｰ繧｢繧ｦ繝医′蟄伜惠縺吶ｋ譎ゅ・
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
				target = targetList.get(rnd.nextInt(targetList.size()));
				targetDeclareWhisper = true;
				return TemplateWhisperFactory.attack(target);
			}
			//譛蠕後・繝ｩ繝ｳ繝繝
			targetDeclareWhisper = true;
			target = aliveHumans.get(rnd.nextInt(aliveHumans.size()));
			return TemplateWhisperFactory.attack(target);
		}

		if(!voteDeclareWhisper){
			//縺ｾ縺謚慕･ｨ螳｣險繧偵＠縺ｦ縺・↑縺・・謚慕･ｨ逶ｮ讓吶ｒ豎ｺ繧√ｋ

			//逕溘″縺ｦ縺・ｋ莠ｺ髢薙・驟榊・繧剃ｽ懈・
			List<Agent> aliveHumans = new ArrayList<Agent>();
			for(Agent agent : humans){
				if(alives.contains(agent)){
					aliveHumans.add(agent);
				}
			}

			//莉頑律縺ｮ逋ｺ險縺ｧ諤ｪ縺励∪繧後※縺・ｋ莠ｺ縺後＞繧後・
			Map<Agent,Integer> voteMap = new HashMap<Agent, Integer>();
			for(Talk talk : talkList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.VOTE)){
					if(!wolfs.contains(utterance.getTarget())){
						if(voteMap.containsKey(utterance.getTarget())){
							int a = voteMap.get(utterance.getTarget());
							a++;
							voteMap.put(utterance.getTarget(), a);
						}else{
							voteMap.put(utterance.getTarget(), 1);
						}
					}
				}
			}

			if(!voteMap.isEmpty()){
				//諤ｪ縺励∪繧後※縺・ｋ莠ｺ縺悟ｭ伜惠縺吶ｋ譎ゅ・
				Agent maxAgent = null;
				int maxInt = 0;
				for(Agent agent : voteMap.keySet()){
					if(voteMap.get(agent) > maxInt){
						maxAgent = agent;
					}
				}
				if(maxAgent != null){
					vote = maxAgent;
					voteDeclareWhisper = true;
					return TemplateWhisperFactory.vote(vote);
				}
			}

			Random rnd = new Random();
			vote = aliveHumans.get(rnd.nextInt(aliveHumans.size()));
			voteDeclareWhisper =true;
			return TemplateWhisperFactory.vote(vote);

		}

		return TemplateWhisperFactory.over();

	}

	@Override
	public String talk() {

		if(!voteDeclareTalk){

			//縺ｾ縺謚慕･ｨ螳｣險繧偵＠縺ｦ縺・↑縺・・謚慕･ｨ逶ｮ讓吶ｒ豎ｺ繧√ｋ

			//逕溘″縺ｦ縺・ｋ莠ｺ髢薙・驟榊・繧剃ｽ懈・
			List<Agent> aliveHumans = new ArrayList<Agent>();
			for(Agent agent : humans){
				if(alives.contains(agent)){
					aliveHumans.add(agent);
				}
			}

			//莉頑律縺ｮ逋ｺ險縺ｧ諤ｪ縺励∪繧後※縺・ｋ莠ｺ縺後＞繧後・
			Map<Agent,Integer> targetMap = new HashMap<Agent, Integer>();
			for(Agent agent : alives){
				for(Talk talk : talkList){
					if(talk.getAgent().equals(agent)){
						Utterance utterance = new Utterance(talk.getContent());
						if(utterance.getTopic().equals(Topic.VOTE)){
							if(!wolfs.contains(utterance.getTarget())){
								if(targetMap.containsKey(utterance.getTarget())){
									int a = targetMap.get(utterance.getTarget());
									a++;
									targetMap.put(utterance.getTarget(), a);
								}else{
									targetMap.put(utterance.getTarget(), 1);
								}
							}
						}
					}
				}
			}

			if(!targetMap.isEmpty()){
				//諤ｪ縺励∪繧後※縺・ｋ莠ｺ縺悟ｭ伜惠縺吶ｋ譎ゅ・
				Agent maxAgent = null;
				int maxInt = 0;
				for(Agent agent : targetMap.keySet()){
					if(targetMap.get(agent) > maxInt){
						maxAgent = agent;
					}
				}
				if(maxAgent != null){
					vote = maxAgent;
					voteDeclareTalk = true;
					return TemplateTalkFactory.vote(vote);
				}
			}


			Random rnd = new Random();
			vote = aliveHumans.get(rnd.nextInt(aliveHumans.size()));
			voteDeclareTalk = true;
			return TemplateTalkFactory.vote(vote);

		}

		return TemplateTalkFactory.over();
	}

	@Override
	public Agent attack() {
		return target;
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
		return vote;
	}

	@Override
	public void finish() {

	}


}
