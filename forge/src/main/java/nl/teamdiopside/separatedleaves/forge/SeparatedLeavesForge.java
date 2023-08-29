package nl.teamdiopside.separatedleaves.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nl.teamdiopside.separatedleaves.Reload;
import nl.teamdiopside.separatedleaves.SeparatedLeaves;

import java.util.function.Consumer;

@Mod(SeparatedLeaves.MOD_ID)
public class SeparatedLeavesForge {
    public SeparatedLeavesForge() {
        EventBuses.registerModEventBus(SeparatedLeaves.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        SeparatedLeaves.init();

        Consumer<TagsUpdatedEvent> tags = tagsUpdatedEvent -> {
            if (tagsUpdatedEvent.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
                reload();
            }
        };

        Consumer<ServerStartingEvent> serverStarting = serverStartingEvent -> {
            SeparatedLeaves.minecraftServer = serverStartingEvent.getServer();
            reload();
        };

        Consumer<ServerStoppingEvent> serverStopping = serverStoppingEvent -> SeparatedLeaves.minecraftServer = serverStoppingEvent.getServer();

        MinecraftForge.EVENT_BUS.addListener(tags);
        MinecraftForge.EVENT_BUS.addListener(serverStarting);
        MinecraftForge.EVENT_BUS.addListener(serverStopping);
    }

    public static void reload() {
        if (SeparatedLeaves.minecraftServer != null) {
            Reload.reload(SeparatedLeaves.minecraftServer.getResourceManager());
        }
    }
}
