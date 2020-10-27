package minegame159.meteorclient;

import net.fabricmc.api.ClientModInitializer;

public class MeteorLoader implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Meteor.INSTANCE.init();
    }
}

