package nl.teamdiopside.fabric;

import nl.teamdiopside.SeparatedLeaves;
import net.fabricmc.api.ModInitializer;

public class SeparatedLeavesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SeparatedLeaves.init();
    }
}
