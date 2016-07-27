package com.naninuneda.chofu;


import org.aiwolf.client.base.player.AbstractRoleAssignPlayer;

import com.naninuneda.chofu.role.ChofuWerewolf;

public class ChofuPower extends AbstractRoleAssignPlayer{

	public ChofuPower(){

		//setSeerPlayer(new ChofuSeer(this));
		//setVillagerPlayer(new ChofuVillager(this));
		//setBodyguardPlayer(new ChofuBodyguard(this));
		setWerewolfPlayer(new ChofuWerewolf());
		//setPossessedPlayer(new ChofuPossessed(this));
	}


	@Override
	public String getName() {
		return "ChofuPower";
	}

}
