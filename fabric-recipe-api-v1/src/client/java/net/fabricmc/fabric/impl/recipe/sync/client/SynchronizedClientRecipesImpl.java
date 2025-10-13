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

package net.fabricmc.fabric.impl.recipe.sync.client;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.client.recipe.v1.sync.SynchronizedClientRecipes;

public record SynchronizedClientRecipesImpl(PreparedRecipes recipes) implements SynchronizedClientRecipes {
	public static final SynchronizedClientRecipesImpl EMPTY = new SynchronizedClientRecipesImpl(PreparedRecipes.EMPTY);

	public static SynchronizedClientRecipesImpl of(Iterable<RecipeEntry<?>> recipes) {
		return new SynchronizedClientRecipesImpl(PreparedRecipes.of(recipes));
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
		return this.recipes.find(type, input, world);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
		return this.recipes.getAll(type);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(RecipeType<T> type, I input, World world) {
		return this.recipes.find(type, input, world).findFirst();
	}

	@Override
	public @Nullable RecipeEntry<?> get(RegistryKey<Recipe<?>> key) {
		return this.recipes.get(key);
	}
}
