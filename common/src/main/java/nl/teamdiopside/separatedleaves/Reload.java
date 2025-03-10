package nl.teamdiopside.separatedleaves;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.architectury.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Reload {

    public record LeavesRule(Set<Block> leaves, Set<Block> logs) {}
    public record JsonFile(ResourceLocation key, JsonElement json) {}

    public static final List<LeavesRule> LEAVES_RULES = new CopyOnWriteArrayList<>();
    public static final Set<String> BIOME_NAMESPACES = new CopyOnWriteArraySet<>();

    public static void reload(ResourceManager resourceManager) {
        apply(getJsons(resourceManager));
    }

    public static void apply(Map<ResourceLocation, JsonElement> jsons) {
        LEAVES_RULES.clear();
        List<LeavesRule> rules = new ArrayList<>();
        Set<String> biomeNamespaces = new HashSet<>();

        List<JsonFile> files = new ArrayList<>();
        jsons.forEach((key, json) -> files.add(new JsonFile(key, json)));
        files.sort(Comparator.comparing(jsonFile -> jsonFile.key().toString()));

        for (JsonFile file : files) {
            ResourceLocation key = file.key();
            JsonElement json = file.json();

            // Skip unloaded mods
            if (!Platform.getModIds().contains(key.getNamespace())) {
                continue;
            }

            biomeNamespaces.add(key.getNamespace());
            // Leaves rule
            try {
                Set<Block> leaves = getBlocks(key, json, "leaves");
                Set<Block> logs = getBlocks(key, json, "logs");

                if (!leaves.isEmpty() && !logs.isEmpty()) {
                    rules.add(new LeavesRule(leaves, logs));
                    SeparatedLeaves.LOGGER.info("Loaded Separated Leaves file {}", key);
                }
            } catch (Exception e) {
                SeparatedLeaves.LOGGER.error("Failed to parse JSON object for leaves rule {}.json, Error: {}", key, e);
            }
        }

        LEAVES_RULES.addAll(rules);
        BIOME_NAMESPACES.addAll(biomeNamespaces);
    }

    public static Set<Block> getBlocks(ResourceLocation key, JsonElement json, String string) {
        Set<Block> blocks = new HashSet<>();
        for (JsonElement jsonElement : json.getAsJsonObject().get(string).getAsJsonArray()) {
            if (jsonElement.getAsString().startsWith("#")) {
                TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, new ResourceLocation(jsonElement.getAsString().replace("#", "")));
                for (Holder<Block> blockHolder : BuiltInRegistries.BLOCK.getOrCreateTag(blockTagKey)) {
                    blocks.add(blockHolder.value());
                }
            } else {
                Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(jsonElement.getAsString()));
                if (block == Blocks.AIR && !jsonElement.getAsString().replace("minecraft:", "").equals("air")) {
                    SeparatedLeaves.LOGGER.error("Block \"{}\" from {} does not exist!", jsonElement.getAsString(), key);
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
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, directory, gson, map);
        return map;
    }
}
