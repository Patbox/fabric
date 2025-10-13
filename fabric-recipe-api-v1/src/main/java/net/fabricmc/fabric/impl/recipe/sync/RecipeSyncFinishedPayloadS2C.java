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

package net.fabricmc.fabric.impl.recipe.sync;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RecipeSyncFinishedPayloadS2C() implements CustomPayload {
	public static final PacketCodec<RegistryByteBuf, RecipeSyncFinishedPayloadS2C> CODEC = PacketCodec.unit(new RecipeSyncFinishedPayloadS2C());

	public static final Id<RecipeSyncFinishedPayloadS2C> ID = new Id<>(Identifier.of("fabric", "recipe_sync_finished"));

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
