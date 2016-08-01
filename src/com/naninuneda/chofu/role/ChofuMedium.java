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

	Map<Agent,Double> result;	//0が人間1が人狼
	int wolf;	//生きている人狼の数
	double possessed;	//0が人狼1が狂人
	Agent declareVote;

	public ChofuMedium(ChofuPower chofuPower) {
		super();
		result = new HashMap<Agent,Double>();
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

		//霊媒結果の反映．
		Judge judge = gameInfo.getMediumResult();
		if(judge != null){
			//霊媒結果人狼だった場合
			if(judge.getResult().equals(Species.WEREWOLF)){
				result.put(judge.getTarget(), 1.0);
				wolf--;
			}else{
				//もし怪しまれていた対象だった場合，狂人である可能性が高い
				if(result.containsKey(judge.getTarget())){
					if(result.get(judge.getTarget()) > 0.0){
						if(possessed > 0){
							possessed = possessed - result.get(judge.getTarget());
						}
					}
				}
				result.put(judge.getTarget(), 0.0);
			}
		}

		//襲撃結果の反映 襲撃されたのは人間である．
		Agent attaked = gameInfo.getAttackedAgent();
		if(attaked != null){
			result.put(attaked, 0.0);
		}

		//襲撃から守られた人は人間である(と考えてよいとする)
		Agent guarded = gameInfo.getGuardedAgent();
		if(guarded != null){
			result.put(guarded, 0.0);
		}



	}

	@Override
	public void update(GameInfo gameInfo){

		super.update(gameInfo);
		//矛盾する占い結果を出していた人間は人狼か狂人
		for(Talk talk:talkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.DIVINED)){
				Agent target = utterance.getTarget();
				if(result.containsKey(target)){
					if(utterance.getResult().equals(Species.HUMAN) && result.get(target) == 1.0){
						//確定でなければ上書きしない
						if(result.get(target) != 0.0 && result.get(target) != 1.0){
							result.put(talk.getAgent(), 1 - possessed/gameSetting.getPlayerNum());
						}
					}else if(utterance.getResult().equals(Species.WEREWOLF) && result.get(target) == 0.0){
						//確定でなければ上書きしない
						if(result.get(target) != 0.0 && result.get(target) != 1.0){
							result.put(talk.getAgent(), 1 - possessed/gameSetting.getPlayerNum());
						}
					}
				}
			}
		}
		//自分以外の霊媒師COは人狼か狂人
		for(Talk talk:talkList){
			Utterance utterance = new Utterance(talk.getContent());
			if(utterance.getTopic().equals(Topic.COMINGOUT)){
				Agent agent = talk.getAgent();
				if(result.containsKey(agent)){
					//確定でないならば上書き
					if(result.get(agent) != 0.0 && result.get(agent) != 1.0){
						result.put(agent, 1 - (double)gameSetting.getRoleNum(Role.POSSESSED)/gameSetting.getPlayerNum());
					}
				}else{
					result.put(agent, 1 - (double)gameSetting.getRoleNum(Role.POSSESSED)/gameSetting.getPlayerNum());
				}
			}
		}
	}

	@Override
	public String talk() {

		if(!co){
			if(result.size() > 3){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), getMyRole());
			}
		}

		return TemplateTalkFactory.over();
	}

	@Override
	public Agent vote() {

		Map<Agent,Double> resultAlive = new HashMap<Agent,Double>();

		for(Agent agent : result.keySet()){
			if(alives.contains(agent)){
				resultAlive.put(agent, result.get(agent));
			}
		}

		//1.0(=人狼確定の物について投票する．
		if(resultAlive.containsValue(1.0)){
			List<Agent> agents = new ArrayList<Agent>();
			for(Agent agent:resultAlive.keySet()){
				if(resultAlive.get(agent) == 1.0){
					agents.add(agent);
				}
			}
			if(!agents.isEmpty()){
				return agents.get(random.nextInt(agents.size()));
			}
		}

		//次に白でないものに関して
		if(!resultAlive.isEmpty()){
			List<Agent> agents = new ArrayList<Agent>();
			for(Agent agent:resultAlive.keySet()){
				if(resultAlive.get(agent) > 0.0){
					agents.add(agent);
				}
			}
			if(!agents.isEmpty()){
				//怪しさが最大の物を選ぶ
				Agent max = agents.get(random.nextInt(agents.size()));
				for(Agent agent : agents){
					if(resultAlive.get(agent) > resultAlive.get(max)){
						max = agent;
					}
				}
				return max;
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

	@Override
	public Agent divine() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
