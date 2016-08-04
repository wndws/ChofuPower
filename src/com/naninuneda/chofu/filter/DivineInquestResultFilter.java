package com.naninuneda.chofu.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

public class DivineInquestResultFilter extends EstimateFilter {

	public DivineInquestResultFilter(EstimateFilter upperFilter,List<Talk> talkList,Map<Agent,Role> coMap){

		super(upperFilter);

		//信頼する占い師，霊媒師の抜き出し
		List<Agent> trusts = new ArrayList<Agent>();
		for(Agent agent:coMap.keySet()){
			if(map.containsKey(agent) && (coMap.get(agent).equals(Role.SEER) || coMap.get(agent).equals(Role.MEDIUM))){
				if(map.get(agent).equals(FilterResult.CREDIBLE)){
					trusts.add(agent);
				}
			}
		}

		//そのエージェントの霊媒，占い結果より結果に反映する
		for(Talk talk:talkList){
			if(trusts.contains(talk.getAgent())){
				Utterance utterance = new Utterance(talk.getContent());
				if(utterance.getTopic().equals(Topic.DIVINED) || utterance.getTopic().equals(Topic.INQUESTED)){
					//上書きはしない
					if(!map.containsKey(utterance.getTarget())){
						if(utterance.getResult().equals(Species.HUMAN)){
							map.put(utterance.getTarget(), FilterResult.CREDIBLE);
						}else{
							map.put(utterance.getTarget(), FilterResult.INCREDIBLE);
						}
					}
				}
			}
		}
	}

}
