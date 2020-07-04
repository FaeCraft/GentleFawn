package com.sakira.gentlefawn.init;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sakira.gentlefawn.entity.EntityDeer;
import com.sakira.gentlefawn.gentlefawn;
import com.sakira.gentlefawn.utils.BiomeDictionaryHelper;
import com.sakira.gentlefawn.utils.ConfigurationHandler;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


@EventBusSubscriber(modid = gentlefawn.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DeerRegistry {
    private static List<EntityType> entities = Lists.newArrayList();
    private static List<Item> spawnEggs = Lists.newArrayList();

    public static final EntityType<EntityDeer> DEER = createEntity(EntityDeer.class, EntityDeer::new, 0.9F, 1.9F, 0xB49D8A, 0xF6F3EF);

    private static <T extends AnimalEntity> EntityType<T> createEntity(Class<T> entityClass, EntityType.IFactory<T> factory, float width, float height, int eggPrimary, int eggSecondary) {
        ResourceLocation location = new ResourceLocation(gentlefawn.MOD_ID, classToString(entityClass));
        EntityType<T> entity = EntityType.Builder.create(factory, EntityClassification.CREATURE).size(width, height).setTrackingRange(64).setUpdateInterval(1).build(location.toString());
        entity.setRegistryName(location);
        entities.add(entity);
        Item spawnEgg = new SpawnEggItem(entity, eggPrimary, eggSecondary, (new Item.Properties()).group(ItemGroup.MISC));
        spawnEgg.setRegistryName(new ResourceLocation(gentlefawn.MOD_ID, classToString(entityClass) + "_spawn_egg"));
        spawnEggs.add(spawnEgg);

        return entity;
    }
    private static String classToString(Class<? extends AnimalEntity> entityClass) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entityClass.getSimpleName()).replace("entity_", "");
    }

    @SubscribeEvent
    public static void registerDeers(RegistryEvent.Register<EntityType<?>> event) {
        for (EntityType entity : entities) {
            Preconditions.checkNotNull(entity.getRegistryName(), "registryName");
            event.getRegistry().register(entity);
            EntitySpawnPlacementRegistry.register(entity, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityDeer::canAnimalSpawn);
        }
    }

    @SubscribeEvent
    public static void registerSpawnEggs(RegistryEvent.Register<Item> event) {
        for (Item spawnEgg : spawnEggs) {
            Preconditions.checkNotNull(spawnEgg.getRegistryName(), "registryName");
            event.getRegistry().register(spawnEgg);
        }
    }

    public static void addSpawn() {
        List<Biome> spawnableBiomes = Lists.newArrayList();

        List<BiomeDictionary.Type> includeList = Arrays.asList(BiomeDictionaryHelper.toBiomeTypeArray(ConfigurationHandler.SPAWN.include.get()));
        List<BiomeDictionary.Type> excludeList = Arrays.asList(BiomeDictionaryHelper.toBiomeTypeArray(ConfigurationHandler.SPAWN.exclude.get()));
        if (!includeList.isEmpty()) {
            for (BiomeDictionary.Type type : includeList) {
                for (Biome biome : BiomeDictionary.getBiomes(type)) {
                    if (!biome.getSpawns(EntityClassification.CREATURE).isEmpty()) {
                        spawnableBiomes.add(biome);
                    }
                }
            }
            if (!excludeList.isEmpty()) {
                for (BiomeDictionary.Type type : excludeList) {
                    Set<Biome> excludeBiomes = BiomeDictionary.getBiomes(type);
                    for (Biome biome : excludeBiomes) {
                        spawnableBiomes.remove(biome);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Do not leave the BiomeDictionary type inclusion list empty. If you wish to disable spawning of an entity, set the weight to 0 instead.");
        }
        for (Biome biome : spawnableBiomes) {
            biome.getSpawns(EntityClassification.CREATURE).add(new Biome.SpawnListEntry(DEER, ConfigurationHandler.SPAWN.weight.get(), ConfigurationHandler.SPAWN.min.get(), ConfigurationHandler.SPAWN.max.get()));
        }
    }
}
