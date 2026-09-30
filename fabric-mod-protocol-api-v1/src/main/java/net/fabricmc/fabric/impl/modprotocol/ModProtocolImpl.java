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

package net.fabricmc.fabric.impl.modprotocol;

import java.util.List;
import java.util.stream.IntStream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;

import net.fabricmc.fabric.api.modprotocol.v1.ModProtocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record ModProtocolImpl(Identifier id, String name, String version, IntList protocol, boolean requireClient, boolean requireServer) implements ModProtocol {
	public static final int UNSUPPORTED = -1;
	public static final Codec<ModProtocolImpl> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(ModProtocolImpl::id),
			Codec.STRING.fieldOf("name").forGetter(ModProtocolImpl::name),
			Codec.STRING.fieldOf("version").forGetter(ModProtocolImpl::version),
			Codec.INT_STREAM.xmap(x -> IntList.of(x.toArray()), x -> IntStream.of(x.toIntArray())).fieldOf("protocol").forGetter(ModProtocolImpl::protocol),
			Codec.BOOL.fieldOf("require_client").forGetter(ModProtocolImpl::requireClient),
			Codec.BOOL.fieldOf("require_server").forGetter(ModProtocolImpl::requireServer)
	).apply(instance, ModProtocolImpl::new));
	public static final Codec<List<ModProtocolImpl>> LIST_CODEC = CODEC.listOf();
	public static final StreamCodec<FriendlyByteBuf, ModProtocolImpl> PACKET_CODEC = StreamCodec.of(ModProtocolImpl::encode, ModProtocolImpl::decode);

	private static ModProtocolImpl decode(FriendlyByteBuf buf) {
		Identifier id = buf.readIdentifier();
		String name = buf.readUtf();
		String version = buf.readUtf();
		IntList protocols = IntList.of(buf.readVarIntArray());
		byte b = buf.readByte();
		boolean requireClient = (b & 0b10) != 0;
		boolean requireServer = (b & 0b01) != 0;
		return new ModProtocolImpl(id, name, version, protocols, requireClient, requireServer);
	}

	private static void encode(FriendlyByteBuf buf, ModProtocolImpl protocol) {
		buf.writeIdentifier(protocol.id);
		buf.writeUtf(protocol.name);
		buf.writeUtf(protocol.version);
		buf.writeVarIntArray(protocol.protocol.toIntArray());
		buf.writeByte((protocol.requireClient ? 0b10 : 0) | (protocol.requireServer ? 0b01 : 0));
	}

	public int getHighestVersion(IntList list) {
		int value = UNSUPPORTED;
		int size = list.size();

		for (int i = 0; i < size; i++) {
			int proto = list.getInt(i);

			if (this.protocol.contains(proto) && value < proto) {
				value = proto;
			}
		}

		return value;
	}

	public boolean syncWithServerMetadata() {
		return this.requireClient() || this.requireServer();
	}
}
