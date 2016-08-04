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
		OnePersonCoFilter opcf = new OnePersonCoFilter(gif, coMap, gameSetting, gameInfo);
		DivineInquestResultFilter dirf = new DivineInquestResultFilter(opcf, talkList, coMap, gameSetting, gameInfo);
		SameRoleFilter srf = new SameRoleFilter(dirf, coMap, getMyRole(), gameSetting, gameInfo);

		estimated = srf.getMap();

	}

	@Override
	public String talk() {
		if(!co){
			if(estimated.size() >= 3){
				co = true;
				return TemplateTalkFactory.comingout(getMe(), Role.MEDIUM);
			}else{
				if(!isLoquacity(getMe())){
					//怪しい物に対して
					if(estimated.containsValue(FilterResult.INCREDIBLE)){
						List<Agent> agents = new ArrayList<Agent>();
						for(Agent agent:estimated.keySet()){
							if(estimated.get(agent).equals(FilterResult.INCREDIBLE) &&
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
				if(!estimated.isEmpty()){
					//ランダムで対象者を選ぶ
					List<String> willTalks = new ArrayList<String>();	//発言候補のリスト
					for(Agent target:estimated.keySet()){
						if(alives.contains(target) && !target.equals(getMe())){
							//対象者が生きている場合
							if(estimated.get(target).equals(FilterResult.INCREDIBLE)){
								//黒判定が出ていたらそれを公表
								String str = TemplateTalkFactory.estimate(target, Role.WEREWOLF);
								willTalks.add(str);
							}else{
								//白判定ならばその人の申告していたカミングアウトを信じる
								if(coMap.containsKey(target)){
									String str = TemplateTalkFactory.estimate(target, coMap.get(target));
									willTalks.add(str);
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
			if(alives.contains(agent)){
				resultAlive.put(agent, result.get(agent));
			}
		}
		for(Agent agent : estimated.keySet()){
			if(alives.contains(agent)){
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
