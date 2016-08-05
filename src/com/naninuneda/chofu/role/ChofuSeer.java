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
import com.naninuneda.chofu.filter.DivineInquestResultFilter;
import com.naninuneda.chofu.filter.FilterResult;
import com.naninuneda.chofu.filter.GameInfoFilter;
import com.naninuneda.chofu.filter.OnePersonCoFilter;
import com.naninuneda.chofu.filter.SameRoleFilter;

public class ChofuSeer extends ChofuBaseRole {

	Map<Agent,Species> result;	//最初のスタートとなる推理
	List<String> publicResult;	//発表した結果
	List<Agent> divined;
	Map<Agent,FilterResult> estimated;

	public ChofuSeer(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<String>();
		result = new HashMap<Agent,Species>();
		divined = new ArrayList<Agent>();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo,gameSetting);
		//自分は人間である
		result.put(getMe(), Species.HUMAN);
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
				result.put(judge.getTarget(), Species.HUMAN);
			}
			divined.add(judge.getTarget());
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

		GameInfoFilter gif = new GameInfoFilter(result, gameSetting, gameInfo);
		SameRoleFilter srf = new SameRoleFilter(gif, coMap, getMyRole(), getMe(), gameSetting, gameInfo);
		OnePersonCoFilter opcf = new OnePersonCoFilter(srf, coMap, gameSetting, gameInfo);
		DivineInquestResultFilter dirf = new DivineInquestResultFilter(opcf, talkList, coMap, gameSetting, gameInfo);

		estimated = dirf.getMap();
	}

	@Override
	public String talk() {
		if(!co){
			if(result.size() >= 1){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.SEER);
			}else{
				if(!isLoquacity(getMe())){
					List<String> willTalks = new ArrayList<String>();
					if(!result.isEmpty()){
						for(Agent agent:result.keySet()){
							if(voteTargets.contains(agent)){
								if(result.get(agent).equals(Species.WEREWOLF)){
									String str = TemplateTalkFactory.estimate(agent, Role.WEREWOLF);
									willTalks.add(str);
								}else{
									if(coMap.containsKey(agent)){
										if(coMap.get(agent).equals(getMyRole())){
											String str = TemplateTalkFactory.estimate(agent, Role.POSSESSED);
											willTalks.add(str);
										}else{
											String str = TemplateTalkFactory.estimate(agent, coMap.get(agent));
											willTalks.add(str);
										}
									}else{
										String str = TemplateTalkFactory.estimate(agent, Role.VILLAGER);
										willTalks.add(str);
									}
								}
							}
						}
					}
					if(!estimated.isEmpty()){
						for(Agent agent:estimated.keySet()){
							if(voteTargets.contains(agent)){
								if(estimated.get(agent).equals(FilterResult.INCREDIBLE)){
									String str = TemplateTalkFactory.estimate(agent, Role.WEREWOLF);
									willTalks.add(str);
								}else{
									if(coMap.containsKey(agent)){
										if(coMap.get(agent).equals(getMyRole())){
											String str = TemplateTalkFactory.estimate(agent, Role.POSSESSED);
											willTalks.add(str);
										}else{
											String str = TemplateTalkFactory.estimate(agent, coMap.get(agent));
											willTalks.add(str);
										}
									}else{
										String str = TemplateTalkFactory.estimate(agent, Role.VILLAGER);
										willTalks.add(str);
									}
								}
							}
						}
					}
					//順番に発表してゆく
					for(String str:willTalks){
						if(!publicResult.contains(str)){
							return str;
						}
					}

					//基本全て発表し終わったらもう一回だけどたまに他の発言も言うよ
					if(!willTalks.isEmpty() && random.nextInt(10) < 7){
						return willTalks.get(random.nextInt(willTalks.size()));
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
						if(alives.contains(target) && !target.equals(getMe())){
							//対象者が生きている場合
							if(result.get(target).equals(Species.WEREWOLF)){
								//黒判定が出ていたらそれを公表
								if(divined.contains(target)){
									String str = TemplateTalkFactory.divined(target, Species.WEREWOLF);
									willTalks.add(str);
								}else{
									String str = TemplateTalkFactory.estimate(target, Role.WEREWOLF);
									willTalks.add(str);
								}
							}else{
								//白判定ならば公表する
								if(divined.contains(target)){
									String str = TemplateTalkFactory.divined(target, Species.HUMAN);
									willTalks.add(str);
								}
								//白判定ならばその人の申告していたカミングアウトを信じる
								for(Talk talk:talkList){
									Utterance utterance = new Utterance(talk.getContent());
									if(utterance.getTopic().equals(Topic.COMINGOUT) && talk.getAgent().equals(target)){
										if(!utterance.getRole().equals(getMyRole()) && gameSetting.getRoleNum(getMyRole()) == 1){
											String comingout = TemplateTalkFactory.estimate(target, utterance.getRole());
											willTalks.add(comingout);
											break;
										}else{
											String comingout = TemplateTalkFactory.estimate(target, Role.POSSESSED);
											willTalks.add(comingout);
											break;
										}
									}
								}
							}
						}else{
							//死んでいる場合は占った場合については発表するのだ
							if(divined.contains(target)){
								String str = TemplateTalkFactory.divined(target, result.get(target));
								willTalks.add(str);
							}
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
		Map<Agent,FilterResult> estimatedAlive = new HashMap<Agent,FilterResult>();

		for(Agent agent : result.keySet()){
			if(voteTargets.contains(agent)){
				resultAlive.put(agent, result.get(agent));
			}
		}
		for(Agent agent : estimated.keySet()){
			if(voteTargets.contains(agent)){
				estimatedAlive.put(agent, estimated.get(agent));
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

		//推定の結果に基づいて
		if(estimatedAlive.containsValue(FilterResult.INCREDIBLE)){
			List<Agent> agents = new ArrayList<Agent>();
			for(Agent agent:estimatedAlive.keySet()){
				if(estimatedAlive.get(agent).equals(FilterResult.INCREDIBLE)){
					agents.add(agent);
				}
			}
			if(!agents.isEmpty()){
				return agents.get(random.nextInt(agents.size()));
			}
		}

		//COが多すぎるやつをとりあえず吊る
		Map<Role,Integer> roleNum = new HashMap<Role,Integer>();
		for(Agent agent:coMap.keySet()){
			if(roleNum.containsKey(coMap.get(agent))){
				int num = roleNum.get(agent);
				roleNum.put(coMap.get(agent), num + 1);
			}else{
				roleNum.put(coMap.get(agent), 1);
			}
		}
		List<Agent> coAgents = new ArrayList<>();
		for(Role role:roleNum.keySet()){
			//gameSettingよりもCO人数が多い場合場合
			if(roleNum.get(role) > gameSetting.getRoleNum(role)){
				//そのCOをしているやつを吊る候補にいれる
				for(Agent agent:coMap.keySet()){
					if(role.equals(coMap.get(agent))&& voteTargets.contains(agent)){
						coAgents.add(agent);
					}
				}
			}
		}
		if(!coAgents.isEmpty()){
			return coAgents.get(random.nextInt(coAgents.size()));
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
			if(!divined.contains(agent) && result.containsKey(agent)){
				agents.add(agent);
			}
		}
		if(!agents.isEmpty()){
			Agent agent = agents.get(random.nextInt(agents.size()));
			return agent;
		}

		//まだ占っていない自称霊媒師
		for(Agent agent:voteTargets){
			if(!divined.contains(agent) && coMap.containsKey(agent)){
				if(coMap.get(agent).equals(Role.MEDIUM)){
					agents.add(agent);
				}
			}
		}
		if(!agents.isEmpty()){
			Agent agent = agents.get(random.nextInt(agents.size()));
			return agent;
		}

		//投票先として,推定先として人気のあるエージェントの次に人気のあるエージェント
		Map<Agent,Integer> doubt = new HashMap<Agent,Integer>();
		for(Agent agent:voteTargets){
			doubt.put(agent, 0);
		}
		for(Talk talk:todayTalkList){
			Utterance utterance = new Utterance(talk.getContent());
			if((utterance.getTopic().equals(Topic.VOTE) || utterance.getTopic().equals(Topic.ESTIMATE))&&
					voteTargets.contains(utterance.getTarget())){
				int voteNum = doubt.get(utterance.getTarget());
				doubt.put(utterance.getTarget(),voteNum + 1);
			}
		}
		List<Agent> maxes = new ArrayList<Agent>();
		for(Agent agent:doubt.keySet()){
			if(maxes.isEmpty()){
				maxes.add(agent);
				continue;
			}
			if(!divined.contains(agent)){
				if(doubt.get(agent) > doubt.get(maxes.get(0))){
					maxes.clear();
					maxes.add(agent);
				}else if(doubt.get(agent) == doubt.get(maxes.get(0))){
					maxes.add(agent);
				}
			}
		}

		List<Agent> maxes2 = new ArrayList<Agent>();
		for(Agent agent:doubt.keySet()){
			if(maxes2.isEmpty()){
				maxes2.add(agent);
				continue;
			}
			if(!divined.contains(agent) && !maxes.contains(agent)){
				if(doubt.get(agent) > doubt.get(maxes2.get(0))){
					maxes2.clear();
					maxes2.add(agent);
				}else if(doubt.get(agent) == doubt.get(maxes2.get(0))){
					maxes2.add(agent);
				}
			}
		}


		if(!maxes2.isEmpty()){
			Agent agent = maxes2.get(random.nextInt(maxes2.size()));
			return agent;
		}

		//後は占ったことがない人をランダム
		for(Agent agent:voteTargets){
			if(!divined.contains(agent)){
				agents.add(agent);
			}
		}
		if(!agents.isEmpty()){
			Agent agent = agents.get(random.nextInt(agents.size()));
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
