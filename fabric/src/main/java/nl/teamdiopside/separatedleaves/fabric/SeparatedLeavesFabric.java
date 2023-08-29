package nl.teamdiopside.separatedleaves.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import nl.teamdiopside.separatedleaves.Reload;
import nl.teamdiopside.separatedleaves.SeparatedLeaves;

public class SeparatedLeavesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SeparatedLeaves.init();

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) {
                reload();
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SeparatedLeaves.minecraftServer = server;
            reload();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> SeparatedLeaves.minecraftServer = null);
    }

    public static void reload() {
        if (SeparatedLeaves.minecraftServer != null) {
            Reload.reload(SeparatedLeaves.minecraftServer.getResourceManager());
        } else {
            SeparatedLeaves.LOGGER.error("No server instance");
        }
    }
}
