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

public class ChofuMedium extends ChofuBaseRole  {

	Map<Agent,Species> result;	//最初のスタートとなる推理
	List<String> publicResult;	//発表した結果
	List<Agent> inquested;
	Map<Agent,FilterResult> estimated;

	public ChofuMedium(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<String>();
		result = new HashMap<Agent,Species>();
		inquested = new ArrayList<Agent>();
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

		GameInfoFilter gif = new GameInfoFilter(result, gameSetting, gameInfo);
		SameRoleFilter srf = new SameRoleFilter(gif, coMap, getMyRole(), getMe(), gameSetting, gameInfo);
		OnePersonCoFilter opcf = new OnePersonCoFilter(srf, coMap, gameSetting, gameInfo);
		DivineInquestResultFilter dirf = new DivineInquestResultFilter(opcf, talkList, coMap, gameSetting, gameInfo);

		estimated = dirf.getMap();

	}

	@Override
	public String talk() {
		if(!co){
			if(estimated.size() >= 3){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.MEDIUM);
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
				if(!estimated.isEmpty()){
					//ランダムで対象者を選ぶ
					List<String> willTalks = new ArrayList<String>();	//発言候補のリスト
					for(Agent target:estimated.keySet()){
						if(alives.contains(target) && !target.equals(getMe())){
							//対象者が生きている場合
							if(estimated.get(target).equals(FilterResult.INCREDIBLE)){
								//黒判定が出ていたらそれを推定として公表
								String str = TemplateTalkFactory.estimate(target, Role.WEREWOLF);
								willTalks.add(str);
							}else{
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
							//死んでいる場合は霊媒結果か公然結果かどうか査定して霊媒結果のみ発表
							if(inquested.contains(target)){
								String str = TemplateTalkFactory.inquested(target, result.get(target));
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
				int num = roleNum.get(coMap.get(agent));
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
