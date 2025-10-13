/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.client.recipe.v1.sync;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.recipebook.ClientRecipeManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;

/**
 * This class provides access to synchronized recipes on the client.
 *
 * <p>You can access SynchronizedClientRecipes by calling getSynchronizedRecipes
 * method on {@link ClientRecipeManager}, {@link ClientPlayNetworkHandler} or {@link ClientWorld}
 *
 * <p>See {@link RecipeSynchronization}.
 */
@ApiStatus.NonExtendable
public interface SynchronizedClientRecipes {
	/**
	 * Creates a stream of all recipe entries of the given {@code type} that match the
	 * given {@code input} and {@code world}.
	 *
	 * <p>If {@code input.isEmpty()} returns true, the returned stream will be always empty.
	 *
	 * @return the stream of matching recipes
	 */
	<I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world);

	/**
	 * @return the collection of recipe entries of given type
	 */
	<I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type);

	default <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable RegistryKey<Recipe<?>> recipe) {
		RecipeEntry<T> recipeEntry = recipe != null ? this.get(type, recipe) : null;
		return this.getFirstMatch(type, input, world, recipeEntry);
	}

	default <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world, @Nullable RecipeEntry<T> recipe) {
		return recipe != null && recipe.value().matches(input, world) ? Optional.of(recipe) : this.getFirstMatch(type, input, world);
	}

	<I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world);

	@Nullable
	RecipeEntry<?> get(RegistryKey<Recipe<?>> key);

	@Nullable
	default <T extends Recipe<?>> RecipeEntry<T> get(RecipeType<T> type, RegistryKey<Recipe<?>> key) {
		RecipeEntry<?> recipeEntry = this.get(key);
		//noinspection unchecked
		return recipeEntry != null && recipeEntry.value().getType().equals(type) ? (RecipeEntry<T>) recipeEntry : null;
	}
}
