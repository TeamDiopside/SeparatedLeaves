package nl.teamdiopside.separatedleaves.forge;

import net.minecraftforge.fml.loading.FMLPaths;
import nl.teamdiopside.separatedleaves.SeparatedLeavesExpectPlatform;

import java.nio.file.Path;

public class SeparatedLeavesExpectPlatformImpl {
    /**
     * This is our actual method to {@link SeparatedLeavesExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
