package com.comze_instancelabs.mgsnake;

import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {

	public IClassesConfig(Main m){
		super(m, true);
		this.getConfig().options().header("Used for saving classes. Default class:");
		
		// default (Speed)
    	this.getConfig().addDefault("config.kits.default.name", "default");
    	this.getConfig().addDefault("config.kits.default.items", "399*1");
    	this.getConfig().addDefault("config.kits.default.lore", "Speed");
    	this.getConfig().addDefault("config.kits.default.requires_money", false);
    	this.getConfig().addDefault("config.kits.default.requires_permission", false);
    	this.getConfig().addDefault("config.kits.default.money_amount", 100);
    	this.getConfig().addDefault("config.kits.default.permission_node", "minigames.kits.default");

    	this.getConfig().options().copyDefaults(true);
    	this.saveConfig();
	}
	
}
