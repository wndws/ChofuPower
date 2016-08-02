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

	Map<Agent,Species> result;	//0が人間1が人狼
	ArrayList<Agent> publicResult;	//発表した結果
	int wolf;	//生きている人狼の数
	double possessed;	//0が人狼1が狂人
	Agent declareVote;
	boolean inquested = false;

	public ChofuMedium(ChofuPower chofuPower) {
		super();
		publicResult = new ArrayList<Agent>();
		result = new HashMap<Agent,Species>();
	}

	@Override
	public void initialize(GameInfo gameInfo,GameSetting gameSetting){
		super.initialize(gameInfo,gameSetting);

		//初期状態では人狼,狂人がMAXの人数がいる．
		wolf = gameSetting.getRoleNum(Role.WEREWOLF);
		possessed = gameSetting.getRoleNum(Role.POSSESSED);
	}

	@Override
	public void dayStart() {
		super.dayStart();
		declareVote = getMe();
		inquested = false;
	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);

		if(!inquested){
			inquestedUpdate();
		}


		//矛盾する占い，霊媒結果を出していた人間はブラックとする
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
		if(gameSetting.getRoleNum(Role.MEDIUM) == 1){
			for(Talk talk:talkList){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.COMINGOUT)){
					Agent agent = talk.getAgent();
					if(result.containsKey(agent) && !talk.getAgent().equals(getMe())){
						if(utterance.getRole().equals(Role.MEDIUM)){
							result.put(talk.getAgent(), Species.WEREWOLF);
						}
					}
				}
			}
		}
	}

	@Override
	public String talk() {

		if(!co){

			if(result.size() >= 1){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.MEDIUM);
			}else{
				//if(!isLoquacity(getMe())){
					//怪しい物に対して
					if(result.containsValue(Species.WEREWOLF)){
						List<Agent> agents = new ArrayList<Agent>();
						for(Agent agent:result.keySet()){
							if(result.get(agent).equals(Species.WEREWOLF)){
								agents.add(agent);
							}
						}
						if(!agents.isEmpty()){
							return TemplateTalkFactory.vote(agents.get(random.nextInt(agents.size())));
						}
					}
					Agent agent = getRandomVoteTarget();
					declareVote = agent;
					return TemplateTalkFactory.vote(agent);
				//}
			}
		}else{
			//if(!isLoquacity(getMe())){
				//発表していない結果があった場合はそれを発表
				if(!result.isEmpty()){
					List<Agent> notPublic = new ArrayList<Agent>();
					for(Agent agent:result.keySet()){
						if(!publicResult.contains(agent) &&
								(result.get(agent).equals(Species.HUMAN) || result.get(agent).equals(Species.WEREWOLF))){
							notPublic.add(agent);
						}
					}
					if(!notPublic.isEmpty()){
						//ランダムで対象者を選ぶ
						Agent target = notPublic.get(random.nextInt(notPublic.size()));
						if(alives.contains(target)){
							//対象者が生きている場合
							if(result.get(target).equals(Species.WEREWOLF)){
								//黒判定が出ていたらそれを公表
								publicResult.add(target);
								return TemplateTalkFactory.estimate(target, Role.WEREWOLF);
							}else{
								//その人の申告していたカミングアウトを信じる
								for(Talk talk:talkList){
									Utterance utterance = new Utterance(talk.getContent());
									if(utterance.getTopic().equals(Topic.COMINGOUT) && talk.getAgent().equals(target)){
										publicResult.add(target);
										return TemplateTalkFactory.estimate(target, utterance.getRole());
									}
								}
							}
						}else{
							//死んでいる場合は霊媒結果として発表
							publicResult.add(target);
							return TemplateTalkFactory.inquested(target, result.get(target));
						}
					}
				}
				Agent agent = getRandomVoteTarget();
				declareVote = agent;
				return TemplateTalkFactory.vote(agent);
			//}
		}

		//return TemplateTalkFactory.over();
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
		if(resultAlive.containsValue(1.0)){
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

		//talkで宣言していた物
		if(!declareVote.equals(getMe())){
			return declareVote;
		}

		//自分に投票すると言っている奴を吊る
		List<Agent> enemys = new ArrayList<Agent>();
		for(Talk talk:todayTalkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.VOTE) || utterance.getTopic().equals(Topic.ESTIMATE)){
				if(utterance.getTarget().equals(getMe())){
					enemys.add(talk.getAgent());
				}
			}
		}
		if(!enemys.isEmpty()){
			return enemys.get(random.nextInt(enemys.size()));
		}

		//あとはランダムで
		return getRandomVoteTarget();
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

	private void inquestedUpdate(){

		//霊媒結果の反映．
		Judge judge = gameInfo.getMediumResult();
		if(judge != null){

			//霊媒結果人狼だった場合
			if(judge.getResult().equals(Species.WEREWOLF)){
				result.put(judge.getTarget(), Species.WEREWOLF);
				wolf--;
			}else{
				//もし怪しまれていた対象だった場合，狂人である可能性が高い．狂人の数を減らす
				if(result.containsKey(judge.getTarget())){
					if(result.get(judge.getTarget()).equals(Species.WEREWOLF)){
						if(possessed > 0){
							possessed--;
						}
					}
				}
				result.put(judge.getTarget(), Species.HUMAN);
			}

			inquested = true;
		}else{
			for(int i = 0;i < 100;i++){
				System.out.println("チェックポイント！nullです！！！！！\n"+judge);
			}
		}

		//襲撃結果の反映 襲撃されたのは人間である．
		Agent attaked = gameInfo.getAttackedAgent();
		if(attaked != null){
			result.put(attaked, Species.HUMAN);
		}

	}

}
