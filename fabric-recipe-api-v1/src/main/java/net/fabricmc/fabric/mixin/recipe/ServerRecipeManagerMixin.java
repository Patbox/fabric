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

package net.fabricmc.fabric.mixin.recipe;

import java.util.Collection;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.recipe.v1.FabricServerRecipeManager;

@Mixin(ServerRecipeManager.class)
public abstract class ServerRecipeManagerMixin implements FabricServerRecipeManager {
	@Shadow
	private PreparedRecipes preparedRecipes;

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
		return this.preparedRecipes.getAll(type);
	}

	@Override
	public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
		return this.preparedRecipes.find(type, input, world);
	}
}
