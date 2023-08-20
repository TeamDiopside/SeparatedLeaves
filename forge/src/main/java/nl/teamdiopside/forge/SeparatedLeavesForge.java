package nl.teamdiopside.forge;

import dev.architectury.platform.forge.EventBuses;
import nl.teamdiopside.SeparatedLeaves;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SeparatedLeaves.MOD_ID)
public class SeparatedLeavesForge {
    public SeparatedLeavesForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(SeparatedLeaves.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SeparatedLeaves.init();
    }
}
