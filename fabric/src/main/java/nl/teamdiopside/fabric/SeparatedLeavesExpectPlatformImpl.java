package nl.teamdiopside.fabric;

import nl.teamdiopside.SeparatedLeavesExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class SeparatedLeavesExpectPlatformImpl {
    /**
     * This is our actual method to {@link SeparatedLeavesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
