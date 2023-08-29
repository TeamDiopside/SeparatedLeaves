package nl.teamdiopside.separatedleaves;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.architectury.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class SLReloadListener extends SimpleJsonResourceReloadListener {

    public SLReloadListener() {
        super(new Gson(), "separated_leaves");
    }

    public record LeavesRule(Set<Block> leaves, Set<Block> logs) {}

    public record JsonFile(ResourceLocation key, JsonElement json) {}

    public static final List<LeavesRule> LEAVES_RULES = new ArrayList<>();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        LEAVES_RULES.clear();
        List<LeavesRule> temp = new ArrayList<>();

        List<JsonFile> files = new ArrayList<>();
        jsons.forEach((key, json) -> files.add(new JsonFile(key, json)));
        files.sort(Comparator.comparing(jsonFile -> jsonFile.key.toString()));

        for (JsonFile file : files) {
            ResourceLocation key = file.key;
            JsonElement json = file.json;
            if (!Platform.getModIds().contains(key.getNamespace())) {
                continue;
            }
            try {
                Set<Block> leaves = getBlocks(key, json, "leaves");
                Set<Block> logs = getBlocks(key, json, "logs");

                if (!leaves.isEmpty() && !logs.isEmpty()) {
                    temp.add(new LeavesRule(leaves, logs));
                    SeparatedLeaves.LOGGER.info("Loaded Separated Leaves file " + key);
                }
            } catch (Exception e) {
                SeparatedLeaves.LOGGER.error("Failed to parse JSON object for leaves rule " + key + ".json, Error: " + e);
            }
        }

        LEAVES_RULES.addAll(temp);
    }

    public static Set<Block> getBlocks(ResourceLocation key, JsonElement json, String string) {
        Set<Block> blocks = new HashSet<>();
        for (JsonElement jsonElement : json.getAsJsonObject().get(string).getAsJsonArray()) {
            if (jsonElement.getAsString().startsWith("#")) {
                TagKey<Block> blockTagKey = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(jsonElement.getAsString().replace("#", "")));
                HolderSet.Named<Block> tag = Registry.BLOCK.getOrCreateTag(blockTagKey);
                for (Holder<Block> blockHolder : tag) {
                    blocks.add(blockHolder.value());
                }
            } else {
                Block block = Registry.BLOCK.get(new ResourceLocation(jsonElement.getAsString()));
                if (block == Blocks.AIR && !jsonElement.getAsString().replace("minecraft:", "").equals("air")) {
                    SeparatedLeaves.LOGGER.error("Block \"" + jsonElement.getAsString() + "\" from " + key + " does not exist!");
                } else {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }
}