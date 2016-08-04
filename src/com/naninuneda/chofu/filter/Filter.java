package com.naninuneda.chofu.filter;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

public interface Filter {

	public FilterResult getResult(Agent agent);

}
