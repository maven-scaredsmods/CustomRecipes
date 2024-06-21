package io.github.scaredsmods.customrecipes.api.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomRecipeManager extends JsonDataLoader {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<CustomRecipeType<?>, Map<Identifier, CustomRecipeEntry<?>>> recipes = ImmutableMap.of();
    private Map<Identifier, CustomRecipeEntry<?>> recipesById = ImmutableMap.of();
    private boolean errored;

    public CustomRecipeManager() {
        super(GSON, "recipes");
    }

    protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler) {
        this.errored = false;
        Map<CustomRecipeType<?>, ImmutableMap.Builder<Identifier, CustomRecipeEntry<?>>> map2 = Maps.newHashMap();
        ImmutableMap.Builder<Identifier, CustomRecipeEntry<?>> builder = ImmutableMap.builder();
        Iterator var6 = map.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<Identifier, JsonElement> entry = (Map.Entry)var6.next();
            Identifier identifier = (Identifier)entry.getKey();

            try {
                CustomRecipeEntry<?> recipeEntry = deserialize(identifier, JsonHelper.asObject((JsonElement)entry.getValue(), "top element"));
                ((ImmutableMap.Builder)map2.computeIfAbsent(recipeEntry.value().getType(), (recipeType) -> {
                    return ImmutableMap.builder();
                })).put(identifier, recipeEntry);
                builder.put(identifier, recipeEntry);
            } catch (IllegalArgumentException | JsonParseException var10) {
                RuntimeException runtimeException = var10;
                LOGGER.error("Parsing error loading recipe {}", identifier, runtimeException);
            }
        }

        this.recipes = (Map)map2.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entryx) -> {
            return ((ImmutableMap.Builder)entryx.getValue()).build();
        }));
        this.recipesById = builder.build();
        LOGGER.info("Loaded {} recipes", map2.size());
    }

    public boolean isErrored() {
        return this.errored;
    }

    public <C extends Inventory, T extends CustomRecipe<C>> Optional<CustomRecipeEntry<T>> getFirstMatch(CustomRecipeType<T> type, C inventory, World world) {
        return this.getAllOfType(type).values().stream().filter((recipe) -> {
            return recipe.value().matches(inventory, world);
        }).findFirst();
    }

    public <C extends Inventory, T extends CustomRecipe<C>> Optional<Pair<Identifier, CustomRecipeEntry<T>>> getFirstMatch(CustomRecipeType<T> type, C inventory, World world, @Nullable Identifier id) {
        Map<Identifier, CustomRecipeEntry<T>> map = this.getAllOfType(type);
        if (id != null) {
            CustomRecipeEntry<T> recipeEntry = (CustomRecipeEntry)map.get(id);
            if (recipeEntry != null && recipeEntry.value().matches(inventory, world)) {
                return Optional.of(Pair.of(id, recipeEntry));
            }
        }

        return map.entrySet().stream().filter((entry) -> {
            return ((CustomRecipeEntry)entry.getValue()).value().matches(inventory, world);
        }).findFirst().map((entry) -> {
            return Pair.of((Identifier)entry.getKey(), (CustomRecipeEntry)entry.getValue());
        });
    }

    public <C extends Inventory, T extends CustomRecipe<C>> List<CustomRecipeEntry<T>> listAllOfTypeCustom(CustomRecipeType<T> type) {
        return List.copyOf(this.getAllOfType(type).values());
    }

    public <C extends Inventory, T extends CustomRecipe<C>> List<CustomRecipeEntry<T>> getAllMatches(CustomRecipeType<T> type, C inventory, World world) {
        return ((List)this.getAllOfType(type).values().stream().filter((recipe) -> {
            return recipe.value().matches(inventory, world);
        }).sorted(Comparator.comparing((recipeEntry) -> {
            return recipeEntry.value().getFirstOutput(world.getRegistryManager()).getTranslationKey() + recipeEntry.value().getSecondOutput(world.getRegistryManager()).getTranslationKey();
        })).collect(Collectors.toList()));


    }


    private <C extends Inventory, T extends CustomRecipe<C>> Map<Identifier, CustomRecipeEntry<T>> getAllOfType(CustomRecipeType<T> type) {
        return (Map)this.recipes.getOrDefault(type, Collections.emptyMap());
    }

    public <C extends Inventory, T extends CustomRecipe<C>> DefaultedList<ItemStack> getRemainingStacks(CustomRecipeType<T> type, C inventory, World world) {
        Optional<CustomRecipeEntry<T>> optional = this.getFirstMatch(type, inventory, world);
        if (optional.isPresent()) {
            return ((CustomRecipeEntry)optional.get()).value().getRemainder(inventory);
        } else {
            DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

            for(int i = 0; i < defaultedList.size(); ++i) {
                defaultedList.set(i, inventory.getStack(i));
            }

            return defaultedList;
        }
    }

    public Optional<CustomRecipeEntry<?>> get(Identifier id) {
        return Optional.ofNullable((CustomRecipeEntry)this.recipesById.get(id));
    }

    public Collection<CustomRecipeEntry<?>> values() {
        return (Collection)this.recipes.values().stream().flatMap((map) -> {
            return map.values().stream();
        }).collect(Collectors.toSet());
    }

    public Stream<Identifier> keys() {
        return this.recipes.values().stream().flatMap((map) -> {
            return map.keySet().stream();
        });
    }

    protected static CustomRecipeEntry<?> deserialize(Identifier id, JsonObject json) {
        CustomRecipe<?> recipe = (CustomRecipe) Util.getResult(CustomRecipe.CODEC.parse(JsonOps.INSTANCE, json), JsonParseException::new);
        return new  CustomRecipeEntry(id, recipe);
    }

    public void setRecipes(Iterable< CustomRecipeEntry<?>> recipes) {
        this.errored = false;
        Map<CustomRecipeType<?>, Map<Identifier,  CustomRecipeEntry<?>>> map = Maps.newHashMap();
        ImmutableMap.Builder<Identifier,  CustomRecipeEntry<?>> builder = ImmutableMap.builder();
        recipes.forEach((recipe) -> {
            Map<Identifier,  CustomRecipeEntry<?>> map2 = (Map)map.computeIfAbsent(recipe.value().getType(), (t) -> {
                return Maps.newHashMap();
            });
            Identifier identifier = recipe.id();
            CustomRecipeEntry<?> recipeEntry = ( CustomRecipeEntry)map2.put(identifier, recipe);
            builder.put(identifier, recipe);
            if (recipeEntry != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + identifier);
            }
        });
        this.recipes = ImmutableMap.copyOf(map);
        this.recipesById = builder.build();
    }

    public static <C extends Inventory, T extends  CustomRecipe<C>>  CustomRecipeManager.MatchGetter<C, T> createCachedMatchGetter(final  CustomRecipeType<T> type) {
        return new  CustomRecipeManager.MatchGetter<C, T>() {
            @Nullable
            private Identifier id;

            public Optional< CustomRecipeEntry<T>> getFirstMatch(C inventory, World world) {
                CustomRecipeManager recipeManager = new CustomRecipeManager();
                Optional<Pair<Identifier,  CustomRecipeEntry<T>>> optional = recipeManager.getFirstMatch(type, inventory, world, this.id);
                if (optional.isPresent()) {
                    Pair<Identifier,  CustomRecipeEntry<T>> pair = (Pair)optional.get();
                    this.id = (Identifier)pair.getFirst();
                    return Optional.of(( CustomRecipeEntry)pair.getSecond());
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    public interface MatchGetter<C extends Inventory, T extends  CustomRecipe<C>> {
        Optional< CustomRecipeEntry<T>> getFirstMatch(C inventory, World world);
    }
}
