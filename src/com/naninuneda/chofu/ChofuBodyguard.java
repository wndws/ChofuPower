package com.naninuneda.chofu;

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

import com.naninuneda.chofu.role.ChofuBaseRole;

public class ChofuBodyguard extends ChofuBaseRole {

	Map<Agent,Species> result,seer;
	List<String> publicResult;	//発表した結果
	Agent guarded;
	int lastPlayerNumber;
	List<Agent> trustSeers;

	public ChofuBodyguard(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<String>();
		trustSeers = new ArrayList<Agent>();
		result = new HashMap<Agent,Species>();
		seer = new HashMap<Agent,Species>();
	}

	@Override
	public void dayStart() {
		super.dayStart();
		//自分の護衛したエージェントが生きているかつ人数の減少が1であるとき，その護衛したエージェントは人間
		if(this.getDay() >= 2){
			if(alives.contains(guarded)){
				if(lastPlayerNumber - alives.size() == 1){
					result.put(guarded, Species.HUMAN);
				}
			}
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

		//白判定している占い師の結果は無条件で信用する
		List<Agent> seers = new ArrayList<Agent>();
		for(Agent agent:coMap.keySet()){
			if(coMap.get(agent).equals(Role.SEER)){
				if(result.containsKey(agent)){
					if(result.get(agent).equals(Species.HUMAN)){
						seers.add(agent);
					}
				}
			}
		}
		if(!seers.isEmpty()){
			for(Talk talk:talkList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.DIVINED) && trustSeers.contains(talk.getAgent())){
					result.put(utterance.getTarget(), utterance.getResult());
				}
			}
		}

		//占い師COしている人間で証拠不十分で一応占い師にしておく人
		for(Agent agent:coMap.keySet()){
			if(coMap.get(agent).equals(Role.SEER)){
				if(!result.containsKey(agent)){
					seers.add(agent);
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
		//とりあえず信用している占い師が出した判断．
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
			if(result.size() >= 2){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.BODYGUARD);
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
								String str = TemplateTalkFactory.estimate(target, Role.WEREWOLF);
								willTalks.add(str);
							}else{

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

		lastPlayerNumber = alives.size();

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

		if(!agents.isEmpty()){
			return agents.get(random.nextInt(agents.size()));
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
				if(result.get(agent).equals(Species.HUMAN)){
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

 		//白判定の人を抜き出す
 		if(!result.isEmpty()){
			for(Agent agent:result.keySet()){
				if(result.get(agent).equals(Species.HUMAN)){
					agents.add(agent);
				}
			}
		}
 		if(!agents.isEmpty()){
 			guarded = agents.get(random.nextInt(agents.size()));
 			return guarded;
 		}

 		//白だと信頼する占い師が言う人で霊媒師の人
 		if(!seer.isEmpty()){
			for(Agent agent:seer.keySet()){
				if(seer.get(agent).equals(Species.HUMAN)){
					if(coMap.containsKey(agent)){
						if(coMap.get(agent).equals(Role.MEDIUM)){
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

 		//自称占い師，霊媒師の人
 		if(coMap.containsValue(Role.SEER) || coMap.containsValue(Role.MEDIUM)){
 			for(Agent agent:coMap.keySet()){
 				if(coMap.get(agent).equals(Role.SEER) || coMap.get(agent).equals(Role.MEDIUM)){
 					agents.add(agent);
 				}
 			}
 		}
 		if(!agents.isEmpty()){
 			guarded = agents.get(random.nextInt(agents.size()));
 			return guarded;
 		}

 		//白だと信頼する占い師が言う人
 		if(!seer.isEmpty()){
			for(Agent agent:seer.keySet()){
				if(seer.get(agent).equals(Species.HUMAN)){
					agents.add(agent);
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
