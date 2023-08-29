package nl.teamdiopside.separatedleaves;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.architectury.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Reload {

    public static void reload(ResourceManager resourceManager) {
        apply(getJsons(resourceManager));
    }

    public record LeavesRule(Set<Block> leaves, Set<Block> logs) {}

    public record JsonFile(ResourceLocation key, JsonElement json) {}

    public static final List<LeavesRule> LEAVES_RULES = new ArrayList<>();

    public static void apply(Map<ResourceLocation, JsonElement> jsons) {
        LEAVES_RULES.clear();
        List<LeavesRule> temp = new ArrayList<>();

        List<JsonFile> files = new ArrayList<>();
        jsons.forEach((key, json) -> files.add(new JsonFile(key, json)));
        files.sort(Comparator.comparing(jsonFile -> jsonFile.key().toString()));

        for (JsonFile file : files) {
            ResourceLocation key = file.key();
            JsonElement json = file.json();
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

    public static Map<ResourceLocation, JsonElement> getJsons(ResourceManager resourceManager) {
        String directory = "separated_leaves";
        Gson gson = new Gson();
        HashMap<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = directory.length() + 1;
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(directory, resourceLocation -> resourceLocation.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation resourceLocation2 = entry.getKey();
            String string = resourceLocation2.getPath();
            ResourceLocation resourceLocation22 = new ResourceLocation(resourceLocation2.getNamespace(), string.substring(i, string.length() - ".json".length()));
            try {
                BufferedReader reader = entry.getValue().openAsReader();
                try {
                    JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                    if (jsonElement != null) {
                        JsonElement jsonElement2 = map.put(resourceLocation22, jsonElement);
                        if (jsonElement2 == null) continue;
                        throw new IllegalStateException("Duplicate data file ignored with ID " + resourceLocation22);
                    }
                    SeparatedLeaves.LOGGER.error("Couldn't load data file {} from {} as it's null or empty", resourceLocation22, resourceLocation2);
                } finally {
                    ((Reader)reader).close();
                }
            } catch (JsonParseException | IOException | IllegalArgumentException exception) {
                SeparatedLeaves.LOGGER.error("Couldn't parse data file {} from {}", resourceLocation22, resourceLocation2, exception);
            }
        }
        return map;
    }
}
