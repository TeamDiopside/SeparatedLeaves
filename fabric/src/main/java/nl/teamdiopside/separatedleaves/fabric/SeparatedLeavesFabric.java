package nl.teamdiopside.separatedleaves.fabric;

import net.fabricmc.api.ModInitializer;
import nl.teamdiopside.separatedleaves.SeparatedLeaves;

public class SeparatedLeavesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SeparatedLeaves.init();
    }
}
