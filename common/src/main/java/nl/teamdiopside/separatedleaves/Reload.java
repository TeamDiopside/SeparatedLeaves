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
    public record LeavesJson(Set<String> leaves, Set<String> logs) {
        public static final Codec<LeavesJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("leaves").forGetter(LeavesJson::leaves),
                Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("logs").forGetter(LeavesJson::logs)
        ).apply(instance, LeavesJson::new));
    }
    public record JsonFile(ResourceLocation key, LeavesJson json) {}

    public static final List<LeavesRule> LEAVES_RULES = new CopyOnWriteArrayList<>();
    public static final Set<String> BIOME_NAMESPACES = new CopyOnWriteArraySet<>();

    public static void reload(ResourceManager resourceManager) {
        apply(getJsons(resourceManager));
    }

    public static void apply(Map<ResourceLocation, LeavesJson> jsons) {
        LEAVES_RULES.clear();
        BIOME_NAMESPACES.clear();

        List<LeavesRule> rules = new ArrayList<>();
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

            biomeNamespaces.add(key.getNamespace());
            try {
                Set<Block> leaves = getBlocks(key, json.leaves());
                Set<Block> logs = getBlocks(key, json.logs());

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
