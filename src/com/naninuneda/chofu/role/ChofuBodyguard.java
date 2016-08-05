package com.naninuneda.chofu.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
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

public class ChofuBodyguard extends ChofuBaseRole {

	Map<Agent,Species> result;
	List<String> publicResult;	//発表した結果
	Agent guarded;
	Map<Agent, FilterResult> estimated;

	public ChofuBodyguard(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<String>();
		result = new HashMap<Agent,Species>();
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
			if(result.size() >= 3){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.BODYGUARD);
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
					if(!willTalks.isEmpty() && random.nextInt(10) < 8){
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
	public Agent guard() {
		List<Agent> agents = new ArrayList<Agent>();
		//白判定でさらに占い師もしくは霊媒師を抜き出す
 		if(!result.isEmpty()){
			for(Agent agent:result.keySet()){
				if(result.get(agent).equals(Species.HUMAN) && voteTargets.contains(agent)){
					if(coMap.containsKey(agent)){
						if(coMap.get(agent).equals(Role.SEER) || coMap.get(agent).equals(Role.MEDIUM)){
							agents.add(agent);
						}
					}
				}
			}
		}
 		if(!agents.isEmpty()){
 			guarded = agents.get(random.nextInt(agents.size()));
 			return guarded;
 		}

 		//白推定でさらに占い師もしくは霊媒師を抜き出す
 		if(!estimated.isEmpty()){
			for(Agent agent:estimated.keySet()){
				if(estimated.get(agent).equals(FilterResult.CREDIBLE) && voteTargets.contains(agent)){
					if(coMap.containsKey(agent)){
						if(coMap.get(agent).equals(Role.SEER) || coMap.get(agent).equals(Role.MEDIUM)){
							agents.add(agent);
						}
					}
				}
			}
		}
 		if(!agents.isEmpty()){
 			guarded = agents.get(random.nextInt(agents.size()));
 			return guarded;
 		}

 		//後は適当に
 		guarded = voteTargets.get(random.nextInt(voteTargets.size()));
		return guarded;
	}

	@Override
	public String whisper() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Agent divine() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Agent attack() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
