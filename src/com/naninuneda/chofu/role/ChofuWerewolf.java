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
import com.naninuneda.chofu.filter.DivineInquestResultFilter;
import com.naninuneda.chofu.filter.FilterResult;
import com.naninuneda.chofu.filter.GameInfoFilter;
import com.naninuneda.chofu.filter.OnePersonCoFilter;
import com.naninuneda.chofu.filter.SameRoleFilter;

public class ChofuWerewolf extends ChofuBaseRole {

	Map<Agent,Species> result;
	List<String> publicResult;	//発表した結果
	Agent guarded;
	Map<Agent, FilterResult> estimated;
	List<Agent> wolves,AttackTargets,AttackList;

	public ChofuWerewolf(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<String>();
		result = new HashMap<Agent,Species>();
		wolves = new ArrayList<Agent>();
		AttackTargets = new ArrayList<Agent>();
		AttackList = new ArrayList<Agent>();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo,gameSetting);
		//自分は人狼である
		result.put(getMe(), Species.WEREWOLF);
		//仲間
		for(Agent agent:gameInfo.getRoleMap().keySet()){
			if(gameInfo.getRoleMap().get(agent).equals(Role.WEREWOLF)){
				result.put(agent, Species.WEREWOLF);
				wolves.add(agent);
			}
		}
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

		AttackTargets.clear();
		for(Agent agent:voteTargets){
			if(!wolves.contains(agent)){
				AttackTargets.add(agent);
			}
		}

		for(Talk whisper:gameInfo.getWhisperList()){
			Utterance utterance = new Utterance(whisper.getContent());
			if(utterance.getTopic().equals(Topic.ATTACK) && !whisper.getAgent().equals(getMe())){
				AttackList.add(utterance.getTarget());
			}
		}

		GameInfoFilter gif = new GameInfoFilter(result, gameSetting, gameInfo);
		SameRoleFilter srf = new SameRoleFilter(gif, coMap, getMyRole(), getMe(), gameSetting, gameInfo);
		OnePersonCoFilter opcf = new OnePersonCoFilter(srf, coMap, gameSetting, gameInfo);
		DivineInquestResultFilter dirf = new DivineInquestResultFilter(opcf, talkList, coMap, gameSetting, gameInfo);

		estimated = dirf.getMap();

	}

	@Override
	public String talk() {
		if(!co){
			if(alives.size() == 3){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.VILLAGER);
			}else{
				if(!isLoquacity(getMe())){
					List<String> willTalks = new ArrayList<String>();
					if(!result.isEmpty()){
						for(Agent agent:result.keySet()){
							if(voteTargets.contains(agent)){
								if(result.get(agent).equals(Species.WEREWOLF)){
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
								//黒判定であれば狂人かその人のカミングアウト通りに推測してあげる
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
							}else{
								//白判定は人狼と推測する
								String str = TemplateTalkFactory.estimate(target, Role.WEREWOLF);
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

		//人気のある者に投票する
		Map<Agent,Integer> doubt = new HashMap<Agent,Integer>();
		for(Agent agent:AttackTargets){
			doubt.put(agent, 0);
		}
		for(Talk talk:todayTalkList){
			Utterance utterance = new Utterance(talk.getContent());
			if((utterance.getTopic().equals(Topic.VOTE) || utterance.getTopic().equals(Topic.ESTIMATE))&&
					AttackTargets.contains(utterance.getTarget())){
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
			if(doubt.get(agent) > doubt.get(maxes.get(0))){
				maxes.clear();
				maxes.add(agent);
			}else if(doubt.get(agent) == doubt.get(maxes.get(0))){
				maxes.add(agent);
			}
		}

		if(!maxes.isEmpty()){
			return maxes.get(random.nextInt(maxes.size()));
		}

		//あとはランダムで
		return getProperVoteTarget();
	}

	@Override
	public String whisper() {

		if(!isAlreadyWhisperOneBefore()){

			// まず占い師，霊媒師，狩人でCOに重複がない場合
			List<Agent> targets = new ArrayList<Agent>();
			for(Agent agent1:AttackTargets){
				if(coMap.containsKey(agent1)){
					if(coMap.get(agent1).equals(Role.BODYGUARD) ||
							coMap.get(agent1).equals(Role.SEER) ||
							coMap.get(agent1).equals(Role.MEDIUM)){
						boolean overlap = false;
						for(Agent agent2:AttackTargets){
							if(!agent1.equals(agent2) && coMap.get(agent1).equals(coMap.get(agent2))){
								overlap = true;
								break;
							}
						}
						if(!overlap){
							targets.add(agent1);
						}
					}
				}
			}
			if(!targets.isEmpty()){
				Agent target = targets.get(random.nextInt(targets.size()));
				AttackList.add(target);
				return TemplateWhisperFactory.attack(target);
			}

			if(!AttackList.isEmpty()){
				Agent target = AttackList.get(random.nextInt(AttackList.size()));
				return TemplateWhisperFactory.attack(target);
			}

			Agent target = AttackTargets.get(random.nextInt(AttackTargets.size()));
			AttackList.add(target);
			return TemplateWhisperFactory.attack(target);

		}
		return TemplateWhisperFactory.over();
	}

	@Override
	public Agent attack() {

		// まず占い師，霊媒師，狩人でCOに重複がない場合
		List<Agent> targets = new ArrayList<Agent>();
		for(Agent agent1:AttackTargets){
			if(coMap.containsKey(agent1)){
				if(coMap.get(agent1).equals(Role.BODYGUARD) ||
						coMap.get(agent1).equals(Role.SEER) ||
						coMap.get(agent1).equals(Role.MEDIUM)){
					boolean overlap = false;
					for(Agent agent2:AttackTargets){
						if(!agent1.equals(agent2) && coMap.get(agent1).equals(coMap.get(agent2))){
							overlap = true;
							break;
						}
					}
					if(!overlap){
						targets.add(agent1);
					}
				}
			}
		}
		if(!targets.isEmpty()){
			return targets.get(random.nextInt(targets.size()));
		}

		if(!AttackList.isEmpty()){
			return AttackList.get(random.nextInt(AttackList.size()));
		}

		return AttackTargets.get(random.nextInt(AttackTargets.size()));

	}

	@Override
	public Agent divine() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Agent guard() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private boolean isAlreadyWhisperOneBefore(){
		if(!gameInfo.getWhisperList().isEmpty()){
			if(gameInfo.getWhisperList().get(gameInfo.getWhisperList().size()-1).getAgent().equals(getMe())){
				return true;
			}
		}
		return false;
	}

}
