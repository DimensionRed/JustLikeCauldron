package com.dimensionred;

import com.dimensionred.crucible.CrucibleBehavior;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CauldronFix implements ModInitializer {
	public static final String MOD_ID = "jscauldron";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {

		LOGGER.info("Initializing Just Like Cauldron");

		CFRegistry.setup();
		CrucibleBehavior.registerBehavior();
		CFEvents.registerEvents();
	}

}




