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

package net.fabricmc.fabric.api.recipe.v1.sync;

import java.util.Objects;

import net.minecraft.recipe.RecipeSerializer;

import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;

/**
 * Since Minecraft 1.21.2, vanilla no longer syncs all recipes to the client automatically,
 * opting into sending only required recipe book data.
 *
 * <p>This api allows to enable Fabric's recipe sync for select RecipeSerializers, which can be
 * then used on the client.
 * See {@link net.fabricmc.fabric.api.client.recipe.sync.SynchronizedClientRecipes}
 */
public final class RecipeSynchronization {
	private RecipeSynchronization() {
	}

	/**
	 * Enables synchronization for all recipes using provided RecipeSerializer.
	 *
	 * <p>This method should be only used to mark recipe serializers as synchronized only
	 * if they are provided by your own mod or are vanilla ones.
	 * Blindly adding unchecked recipe serializers might cause bugs and crashes.
	 *
	 * @param serializer recipe serializer used by synchronized recipes.
	 */
	public static void synchronizeRecipeSerializer(RecipeSerializer<?> serializer) {
		Objects.requireNonNull(serializer, "serializer can't be null!");
		Objects.requireNonNull(serializer.packetCodec(), "PacketCodec can't be null!");

		RecipeSyncImpl.addSynchronizedSerializer(serializer);
	}
}
