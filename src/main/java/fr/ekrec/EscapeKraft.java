package fr.ekrec;

import fr.ekrec.commands.EKCommands;
import fr.ekrec.creativetabs.EKCreativeTabs;
import fr.ekrec.items.EKItems;
import fr.ekrec.sounds.EKSounds;
import fr.ekrec.teams.EKTeamManager;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EscapeKraft implements ModInitializer {

	public static final String MOD_ID = "escapekraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		EKSounds.initialize();
		EKItems.initialize();
		EKCreativeTabs.initialize();
		EKCommands.initialize();
		EKTeamManager.initialize();

		LOGGER.info("EscapeKraft is on point !");
	}

}