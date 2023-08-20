package nl.teamdiopside.separatedleaves.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nl.teamdiopside.separatedleaves.SeparatedLeaves;

@Mod(SeparatedLeaves.MOD_ID)
public class SeparatedLeavesForge {
    public SeparatedLeavesForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(SeparatedLeaves.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SeparatedLeaves.init();
    }
}
