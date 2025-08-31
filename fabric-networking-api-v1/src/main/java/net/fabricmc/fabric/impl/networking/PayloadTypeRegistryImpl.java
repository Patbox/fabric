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

package net.fabricmc.fabric.impl.networking;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketSplitter;

public class PayloadTypeRegistryImpl<B extends PacketByteBuf> implements PayloadTypeRegistry<B> {
	public static final PayloadTypeRegistryImpl<PacketByteBuf> CONFIGURATION_C2S = new PayloadTypeRegistryImpl<>("configuration_c2s", NetworkPhase.CONFIGURATION, NetworkSide.SERVERBOUND);
	public static final PayloadTypeRegistryImpl<PacketByteBuf> CONFIGURATION_S2C = new PayloadTypeRegistryImpl<>("configuration_s2c", NetworkPhase.CONFIGURATION, NetworkSide.CLIENTBOUND);
	public static final PayloadTypeRegistryImpl<RegistryByteBuf> PLAY_C2S = new PayloadTypeRegistryImpl<>("play_c2s", NetworkPhase.PLAY, NetworkSide.SERVERBOUND);
	public static final PayloadTypeRegistryImpl<RegistryByteBuf> PLAY_S2C = new PayloadTypeRegistryImpl<>("play_s2c", NetworkPhase.PLAY, NetworkSide.CLIENTBOUND);

	public static final Logger LOGGER = LoggerFactory.getLogger(FabricPacketsImpl.MOD_ID);

	private final Map<Identifier, CustomPayload.Type<B, ? extends CustomPayload>> packetTypes = new HashMap<>();
	private final Reference2IntMap<CustomPayload.Id<?>> packetSplitThreshold = new Reference2IntOpenHashMap<>();
	private final NetworkPhase state;
	private final NetworkSide side;
	private final String name;

	private PayloadTypeRegistryImpl(String name, NetworkPhase state, NetworkSide side) {
		this.state = state;
		this.side = side;
		this.name = name;
	}

	@Override
	public <T extends CustomPayload> CustomPayload.Type<? super B, T> register(CustomPayload.Id<T> id, PacketCodec<? super B, T> codec) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(codec, "codec");

		final CustomPayload.Type<B, T> payloadType = new CustomPayload.Type<>(id, codec.cast());

		if (packetTypes.containsKey(id.id())) {
			throw new IllegalArgumentException("Packet type " + id + " is already registered!");
		}

		packetTypes.put(id.id(), payloadType);
		return payloadType;
	}

	@Override
	public <T extends CustomPayload> CustomPayload.Type<? super B, T> registerSplittable(CustomPayload.Id<T> id, PacketCodec<? super B, T> codec) {
		return registerSplittable(id, codec, FabricPacketSplitter.SAFE_SPLIT_SIZE);
	}

	@Override
	public <T extends CustomPayload> CustomPayload.Type<? super B, T> registerSplittable(CustomPayload.Id<T> id, PacketCodec<? super B, T> codec, int splitThreshold) {
		if (this.side == NetworkSide.CLIENTBOUND) {
			packetSplitThreshold.put(id, splitThreshold);
		} else {
			LOGGER.warn("The " + this.name + " doesn't support packet splitting! Using registerSplittable will have no effect!");
		}

		return register(id, codec);
	}

	@Nullable
	public CustomPayload.Type<B, ? extends CustomPayload> get(Identifier id) {
		return packetTypes.get(id);
	}

	@Nullable
	public <T extends CustomPayload> CustomPayload.Type<B, T> get(CustomPayload.Id<T> id) {
		//noinspection unchecked
		return (CustomPayload.Type<B, T>) packetTypes.get(id.id());
	}

	public int getSplittingThreshold(CustomPayload.Id<?> id) {
		return this.packetSplitThreshold.getOrDefault(id, -1);
	}

	public NetworkPhase getPhase() {
		return state;
	}

	public NetworkSide getSide() {
		return side;
	}
}
