package nl.teamdiopside.separatedleaves;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Reload {

    public record LeavesRule(Set<Block> leaves, Set<Block> logs) {}
    public record LeavesJson(Optional<Set<String>> leaves, Optional<Set<String>> logs, Optional<Boolean> allBiomes, Optional<Set<String>> biomes) {
        public static final Codec<LeavesJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("leaves").forGetter(LeavesJson::leaves),
                Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("logs").forGetter(LeavesJson::logs),
                Codec.BOOL.optionalFieldOf("all").forGetter(LeavesJson::allBiomes),
                Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("biomes").forGetter(LeavesJson::biomes)
        ).apply(instance, LeavesJson::new));
    }
    public record JsonFile(ResourceLocation key, LeavesJson json) {}

    public static final List<LeavesRule> LEAVES_RULES = new CopyOnWriteArrayList<>();
    public static final Set<String> BIOMES = new CopyOnWriteArraySet<>();
    public static final Set<String> BIOME_NAMESPACES = new CopyOnWriteArraySet<>();

    public static void reload(ResourceManager resourceManager) {
        apply(getJsons(resourceManager));
    }

    public static void apply(Map<ResourceLocation, LeavesJson> jsons) {
        LEAVES_RULES.clear();
        BIOMES.clear();
        BIOME_NAMESPACES.clear();

        List<LeavesRule> rules = new ArrayList<>();
        Set<String> biomes = new HashSet<>();
        Set<String> biomeNamespaces = new HashSet<>();

        List<JsonFile> files = new ArrayList<>();
        jsons.forEach((key, json) -> files.add(new JsonFile(key, json)));
        files.sort(Comparator.comparing(jsonFile -> jsonFile.key().toString()));

        for (JsonFile file : files) {
            ResourceLocation key = file.key();
            LeavesJson json = file.json();

            // Skip unloaded mods
            if (!Platform.getModIds().contains(key.getNamespace())) {
                continue;
            }

            // biomes.json
            if (key.getPath().equals("biomes")) {
                try {
                    if (json.allBiomes().isPresent() && json.allBiomes().get()) {
                        biomeNamespaces.add(key.getNamespace());
                    } else {
                        json.biomes().ifPresentOrElse(biomes::addAll, () -> SeparatedLeaves.LOGGER.error("Failed to parse {}'s biomes.json for Separated Leaves, Error: {}", key.getNamespace(), "No biomes found!"));
                    }
                } catch (Exception e) {
                    SeparatedLeaves.LOGGER.error("Failed to parse {}'s biomes.json for Separated Leaves, Error: {}", key.getNamespace(), e);
                }
            } else {
                if (json.leaves().isPresent() && json.logs().isPresent()) {
                    try {
                        Set<Block> leaves = getBlocks(key, json.leaves().get());
                        Set<Block> logs = getBlocks(key, json.logs().get());

                        if (!leaves.isEmpty() && !logs.isEmpty()) {
                            rules.add(new LeavesRule(leaves, logs));
                            SeparatedLeaves.LOGGER.info("Loaded Separated Leaves file {}", key);
                        }
                    } catch (Exception e) {
                        SeparatedLeaves.LOGGER.error("Failed to parse JSON object for leaves rule {}.json, Error: {}", key, e);
                    }
                } else {
                    SeparatedLeaves.LOGGER.error("Failed to parse JSON object for leaves rule {}.json, Error: {}", key, "Invalid Format!");
                }
            }
        }

        LEAVES_RULES.addAll(rules);
        BIOMES.addAll(biomes);
        BIOME_NAMESPACES.addAll(biomeNamespaces);
    }

    public static Set<Block> getBlocks(ResourceLocation key, Set<String> stringSet) {
        Set<Block> blocks = new HashSet<>();
        for (String string : stringSet) {
            if (string.startsWith("#")) {
                TagKey<Block> blockTagKey = TagKey.create(Registries.BLOCK, ResourceLocation.parse(string.replace("#", "")));
                BuiltInRegistries.BLOCK.get(blockTagKey).ifPresent(named -> named.forEach(blockHolder -> blocks.add(blockHolder.value())));
            } else {
                Optional<Holder.Reference<Block>> block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(string));
                if (block.isEmpty()) {
                    SeparatedLeaves.LOGGER.error("Block \"{}\" from {} does not exist!", string, key);
                } else {
                    blocks.add(block.get().value());
                }
            }
        }
        return blocks;
    }

    public static Map<ResourceLocation, LeavesJson> getJsons(ResourceManager resourceManager) {
        FileToIdConverter directory = FileToIdConverter.json("separated_leaves");
        HashMap<ResourceLocation, LeavesJson> map = Maps.newHashMap();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, directory, JsonOps.INSTANCE, LeavesJson.CODEC, map);
        return map;
    }
}
