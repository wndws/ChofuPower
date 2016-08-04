package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import com.naninuneda.chofu.ChofuPower;

public class ChofuMedium extends ChofuBaseRole  {

	Map<Agent,Species> result,seer;	//自分の経験に基づく判断，嘘つきかそうでないか．
	List<String> publicResult;	//発表した結果
	List<Agent> trustSeers;
	List<Agent> inquested;

	public ChofuMedium(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<String>();
		trustSeers = new ArrayList<Agent>();
		result = new HashMap<Agent,Species>();
		seer = new HashMap<Agent,Species>();
		inquested = new ArrayList<Agent>();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo,gameSetting);
	}

	@Override
	public void dayStart() {
		super.dayStart();
		//霊媒結果の反映．
		Judge judge = gameInfo.getMediumResult();
		if(judge != null){
			if(!inquested.contains(judge.getTarget())){
				inquested.add(judge.getTarget());
			}
			//霊媒結果人狼だった場合
			if(judge.getResult().equals(Species.WEREWOLF)){
				result.put(judge.getTarget(), Species.WEREWOLF);
			}else{
				//もし怪しまれていた対象だった場合，狂人である可能性が高い．狂人の数を減らす→廃止
				result.put(judge.getTarget(), Species.HUMAN);
			}
		}
		//襲撃結果の反映 襲撃されたのは人間である．
		Agent attaked = gameInfo.getAttackedAgent();
		if(attaked != null){
			result.put(attaked, Species.HUMAN);
		}
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		//自分の結果と矛盾する占い，霊媒結果を出していた人間はブラックとする
		for(Talk talk:talkList){
			Utterance utterance = new Utterance(talk.getContent());

			if(utterance.getTopic().equals(Topic.DIVINED) || utterance.getTopic().equals(Topic.INQUESTED)){
				Agent target = utterance.getTarget();
				if(result.containsKey(target) && !talk.getAgent().equals(getMe())){
					if((utterance.getResult().equals(Species.HUMAN) && result.get(target).equals(Species.WEREWOLF)) ||
							(utterance.getResult().equals(Species.WEREWOLF) && result.get(target).equals(Species.HUMAN))){
						result.put(talk.getAgent(), Species.WEREWOLF);
					}
				}
			}
		}


		//自分以外の霊媒師COはブラックとする
		if(gameSetting.getRoleNum(Role.SEER) == 1){
			for(Agent agent:coMap.keySet()){
				if(coMap.get(agent).equals(Role.MEDIUM) && !agent.equals(getMe())){
					result.put(agent, Species.WEREWOLF);
				}
			}
		}

		//占い師COしている人間を列挙，resultと矛盾する結果の占い師をカット
		List<Agent> seers = new ArrayList<Agent>();
		for(Agent agent:coMap.keySet()){
			if(coMap.get(agent).equals(Role.SEER)){
				if(!result.containsKey(agent)){
					seers.add(agent);
				}else{
					if(!result.get(agent).equals(Species.WEREWOLF)){
						seers.add(agent);
					}
				}
			}
		}

		trustSeers.clear();
		//人数にあふれた占い師をランダムでカット
		if(gameSetting.getRoleNum(Role.SEER) < seers.size()){
			for(int i = 0;i < gameSetting.getRoleNum(Role.SEER);i++){
				trustSeers.add(seers.remove(random.nextInt(seers.size())));
			}
		}

		//信用している占い師が出した判断．
		if(!trustSeers.isEmpty()){
			for(Talk talk:talkList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.DIVINED) && trustSeers.contains(talk.getAgent())){
					seer.put(utterance.getTarget(), utterance.getResult());
				}
			}
		}


	}

	@Override
	public String talk() {
		if(!co){
			if(result.size() >=0){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.MEDIUM);
			}else{
				if(!isLoquacity(getMe())){
					//怪しい物に対して
					if(result.containsValue(Species.WEREWOLF)){
						List<Agent> agents = new ArrayList<Agent>();
						for(Agent agent:result.keySet()){
							if(result.get(agent).equals(Species.WEREWOLF) &&
									voteTargets.contains(agent)){
								agents.add(agent);
							}
						}
						if(!agents.isEmpty()){
							Agent agent = agents.get(random.nextInt(agents.size()));
							return TemplateTalkFactory.vote(agent);
						}
					}
					Agent agent = getProperVoteTarget();
					return TemplateTalkFactory.vote(agent);
				}
			}
		}else{
			if(!isLoquacity(getMe())){
				//発表していない結果があった場合はそれを発表
				if(!result.isEmpty()){
					//ランダムで対象者を選ぶ
					List<String> willTalks = new ArrayList<String>();	//発言候補のリスト
					for(Agent target:result.keySet()){
						if(alives.contains(target)){
							//対象者が生きている場合
							if(result.get(target).equals(Species.WEREWOLF)){
								//黒判定が出ていたらそれを公表
								String str = TemplateTalkFactory.estimate(target, Role.WEREWOLF);
								willTalks.add(str);
							}else{
								//白判定ならばその人の申告していたカミングアウトを信じる
								for(Talk talk:talkList){
									Utterance utterance = new Utterance(talk.getContent());
									if(utterance.getTopic().equals(Topic.COMINGOUT) && talk.getAgent().equals(target)){
										String str = TemplateTalkFactory.estimate(target, utterance.getRole());
										willTalks.add(str);
									}
								}
							}
						}else{
							//死んでいる場合は霊媒結果として発表
							String str = TemplateTalkFactory.inquested(target, result.get(target));
							willTalks.add(str);
						}
					}
					//発表していない項目から順に発表していくのである
					for(String str:willTalks){
						if(!publicResult.contains(str)){
							publicResult.add(str);
							return str;
						}
					}
					//基本全て発表し終わったらもう一回だけどたまに他の発言も言うよ
					if(!willTalks.isEmpty() && random.nextInt(10) < 7){
						return willTalks.get(random.nextInt(willTalks.size()));
					}
				}
				Agent agent = getProperVoteTarget();
				return TemplateTalkFactory.vote(agent);
			}
		}
		return TemplateTalkFactory.over();
	}

	@Override
	public Agent vote() {

		Map<Agent,Species> resultAlive = new HashMap<Agent,Species>();
		Map<Agent,Species> seerAlive = new HashMap<Agent,Species>();

		for(Agent agent : result.keySet()){
			if(alives.contains(agent)){
				resultAlive.put(agent, result.get(agent));
			}
		}
		for(Agent agent : seer.keySet()){
			if(alives.contains(agent)){
				seerAlive.put(agent, seer.get(agent));
			}
		}

		//人狼確定のエージェントについて投票する．
		if(resultAlive.containsValue(Species.WEREWOLF)){
			List<Agent> agents = new ArrayList<Agent>();
			for(Agent agent:resultAlive.keySet()){
				if(resultAlive.get(agent).equals(Species.WEREWOLF)){
					agents.add(agent);
				}
			}
			if(!agents.isEmpty()){
				return agents.get(random.nextInt(agents.size()));
			}
		}

		//信用している占い師の結果に基づいて
		if(seerAlive.containsValue(Species.WEREWOLF)){
			List<Agent> agents = new ArrayList<Agent>();
			for(Agent agent:seerAlive.keySet()){
				if(seerAlive.get(agent).equals(Species.WEREWOLF)){
					agents.add(agent);
				}
			}
			if(!agents.isEmpty()){
				return agents.get(random.nextInt(agents.size()));
			}
		}

		//白でない奴からランダム
		List<Agent> agents = new ArrayList<Agent>();
		for(Agent agent:voteTargets){
			if(result.containsKey(agent)){
				if(result.get(agent).equals(Species.WEREWOLF)){
					agents.add(agent);
				}
			}else{
				agents.add(agent);
			}
		}

		//あとはランダムで
		return getProperVoteTarget();
	}

	@Override
	public String whisper() {

		return null;
	}

	@Override
	public Agent attack() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Agent guard() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Agent divine() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}


}
