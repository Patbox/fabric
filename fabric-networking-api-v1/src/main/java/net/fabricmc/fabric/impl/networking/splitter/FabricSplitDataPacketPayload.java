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

package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.networking.FabricPacketsImpl;

public record FabricSplitDataPacketPayload(int splitId, int part, ByteBuf byteBuf) implements FabricSplitPacketPayload {
	public static final Id<FabricSplitDataPacketPayload> ID = new Id<>(Identifier.of(FabricPacketsImpl.MOD_ID, "split/data"));
	public static final PacketCodec<ByteBuf, FabricSplitDataPacketPayload> CODEC = PacketCodec.ofStatic(FabricSplitDataPacketPayload::write, FabricSplitDataPacketPayload::read);

	private static FabricSplitDataPacketPayload read(ByteBuf buf) {
		int id = VarInts.read(buf);
		int part = VarInts.read(buf);
		int size = VarInts.read(buf);
		return new FabricSplitDataPacketPayload(id, part, buf.readBytes(size));
	}

	private static void write(ByteBuf buf, FabricSplitDataPacketPayload payload) {
		VarInts.write(buf, payload.splitId());
		VarInts.write(buf, payload.part());
		VarInts.write(buf, payload.byteBuf().readableBytes());
		buf.writeBytes(payload.byteBuf());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
