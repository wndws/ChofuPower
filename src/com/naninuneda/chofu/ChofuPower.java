package com.naninuneda.chofu;


import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;

import com.naninuneda.chofu.role.ChofuMedium;
import com.naninuneda.chofu.role.ChofuWerewolf;

public class ChofuPower extends AbstractRoleAssignPlayer{

	public ChofuPower(){

		setSeerPlayer(new ChofuSeer(this));
		setVillagerPlayer(new ChofuVillager(this));
		setBodyguardPlayer(new ChofuBodyguard(this));
		setWerewolfPlayer(new ChofuWerewolf(this));
		setPossessedPlayer(new ChofuPossessed(this));
		setMediumPlayer(new ChofuMedium(this));
	}


	@Override
	public String getName() {
		return "ChofuPower";
	}

}
