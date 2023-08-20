package nl.teamdiopside.forge;

import nl.teamdiopside.SeparatedLeavesExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class SeparatedLeavesExpectPlatformImpl {
    /**
     * This is our actual method to {@link SeparatedLeavesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
