package com.naninuneda.chofu.role;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.aiwolf.client.lib.TemplateTalkFactory;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import com.naninuneda.chofu.ChofuPower;

public class ChofuMedium extends ChofuBaseRole  {

	Map<Agent,Species> result;
	PriorityQueue<Talk> talkQueue;

	public ChofuMedium(ChofuPower chofuPower) {
		super();
		result = new HashMap<Agent,Species>();
		talkQueue = new PriorityQueue<Talk>();
	}

	@Override
	public void dayStart() {
		super.dayStart();

		//霊媒結果の反映．
		Judge judge = gameInfo.getMediumResult();
		if(judge != null){
			result.put(judge.getTarget(), judge.getResult());
		}
		//襲撃結果の反映 襲撃されたのは人間である．
		Agent attaked = gameInfo.getAttackedAgent();
		result.put(attaked, Species.HUMAN);

	}

	@Override
	public String talk() {
		if(!talkQueue.isEmpty()){
			talkQueue.
		}
		return TemplateTalkFactory.over();
	}

	@Override
	public Agent vote() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private int canCo(){
		//COした方がよいかした方がよいとしたらその優先度．
		return 0;
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
