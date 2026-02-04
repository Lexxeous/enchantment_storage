package com.testing.hellofabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloFabric implements ModInitializer {

    public static final String MOD_ID = "hellofabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric mod world! Success!");
    }
}
