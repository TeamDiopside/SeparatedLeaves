package nl.teamdiopside.separatedleaves.fabric;

import net.fabricmc.loader.api.FabricLoader;
import nl.teamdiopside.separatedleaves.SeparatedLeavesExpectPlatform;

import java.nio.file.Path;

public class SeparatedLeavesExpectPlatformImpl {
    /**
     * This is our actual method to {@link SeparatedLeavesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
