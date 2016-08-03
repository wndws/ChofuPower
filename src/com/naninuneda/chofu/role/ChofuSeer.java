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

import com.naninuneda.chofu.ChofuPower;

public class ChofuSeer extends ChofuBaseRole {

	Map<Agent,Species> result;
	List<String> publicResult;	//発表した結果
	List<Agent> alreadyDivine;


	public ChofuSeer(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<String>();
		alreadyDivine = new ArrayList<Agent>();
		result = new HashMap<Agent,Species>();
	}

	@Override
	public void dayStart() {
		super.dayStart();
		//占い結果の反映．
		Judge judge = gameInfo.getDivineResult();
		if(judge != null){
			//占い結果人狼だった場合
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
		//ただ占い済みの人は結果を変えない
		for(Talk talk:talkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.DIVINED) || utterance.getTopic().equals(Topic.INQUESTED)){
				Agent target = utterance.getTarget();
				if(result.containsKey(target) && !talk.getAgent().equals(getMe()) && !alreadyDivine.contains(talk.getAgent())){
					if((utterance.getResult().equals(Species.HUMAN) && result.get(target).equals(Species.WEREWOLF)) ||
							(utterance.getResult().equals(Species.WEREWOLF) && result.get(target).equals(Species.HUMAN))){
						result.put(talk.getAgent(), Species.WEREWOLF);
					}
				}
			}
		}
		//自分以外の占い師COはブラックとする
		//ただ占い済みの人は結果を変えない
		if(gameSetting.getRoleNum(Role.SEER) == 1){
			for(Agent agent:coMap.keySet()){
				if(coMap.get(agent).equals(Role.SEER) &&
						!agent.equals(getMe()) &&
						!alreadyDivine.contains(agent)){
					result.put(agent, Species.WEREWOLF);
				}
			}
		}
	}

	@Override
	public String talk() {
		if(!co){
			if(result.size() >= gameSetting.getRoleNum(Role.WEREWOLF) - 1){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.SEER);
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
						if(!agents.isEmpty() && random.nextInt(10) < 8){
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
								String str = TemplateTalkFactory.divined(target, Species.WEREWOLF);
								willTalks.add(str);
							}else{
								//白判定ならば公表する
								String str = TemplateTalkFactory.divined(target, Species.HUMAN);
								willTalks.add(str);
								//白判定ならばその人の申告していたカミングアウトを信じる
								for(Talk talk:talkList){
									Utterance utterance = new Utterance(talk.getContent());
									if(utterance.getTopic().equals(Topic.COMINGOUT) && talk.getAgent().equals(target)){
										String comingout = TemplateTalkFactory.estimate(target, utterance.getRole());
										willTalks.add(comingout);
										break;
									}
								}
							}
						}else{
							//死んでいる場合も結果として発表
							String str = TemplateTalkFactory.divined(target, result.get(target));
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
					//基本全て発表し終わったらもう一回たまに言うよ
					if(!willTalks.isEmpty() && random.nextInt(10) < 5){
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

		for(Agent agent : result.keySet()){
			if(alives.contains(agent)){
				resultAlive.put(agent, result.get(agent));
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
	public Agent divine() {
		//まだ占っていなく，さらに疑惑のあるユーザーを優先的に．
		List<Agent> agents = new ArrayList<Agent>();
		for(Agent agent:voteTargets){
			if(!alreadyDivine.contains(agent) && result.containsKey(agent)){
				agents.add(agent);
			}
		}
		if(!agents.isEmpty()){
			Agent agent = agents.get(random.nextInt(agents.size()));
			alreadyDivine.add(agent);
			return agent;
		}

		//まだ占っていない自称霊媒師
		for(Agent agent:voteTargets){
			if(!alreadyDivine.contains(agent) && coMap.containsKey(agent)){
				if(coMap.get(agent).equals(Role.MEDIUM)){
					agents.add(agent);
				}
			}
		}
		if(!agents.isEmpty()){
			Agent agent = agents.get(random.nextInt(agents.size()));
			alreadyDivine.add(agent);
			return agent;
		}

		//投票先として人気のあるエージェントを見てみよう
		Map<Agent,Integer> doubt = new HashMap<Agent,Integer>();
		for(Agent agent:voteTargets){
			doubt.put(agent, 0);
		}
		for(Talk talk:todayTalkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.VOTE) &&
					voteTargets.contains(utterance.getTarget())){
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
		for(Agent agent:alreadyDivine){
			maxes.remove(agent);
		}

		if(!maxes.isEmpty()){
			Agent agent = maxes.get(random.nextInt(maxes.size()));
			alreadyDivine.add(agent);
			return agent;
		}

		//後は占ったことがない人をランダム
		for(Agent agent:voteTargets){
			if(!alreadyDivine.contains(agent)){
				agents.add(agent);
			}
		}
		if(!agents.isEmpty()){
			Agent agent = agents.get(random.nextInt(agents.size()));
			alreadyDivine.add(agent);
			return agent;
		}

		//後は本当にランダム自分・死んだ人以外
		return voteTargets.get(random.nextInt(voteTargets.size()));
	}

	@Override
	public String whisper() {
		// TODO 自動生成されたメソッド・スタブ
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

}
