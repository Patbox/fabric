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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.handler.DecoderHandler;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.networking.GenericPayloadAccessor;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.mixin.networking.accessor.DecoderHandlerAccessor;

public class FabricPacketMerger extends MessageToMessageDecoder<Packet<?>> {
	private final DecoderHandler<?> decoderHandler;
	private final PayloadTypeRegistryImpl<?> payloadTypeRegistry;
	@Nullable
	private Merger packetMerger;

	public FabricPacketMerger(DecoderHandler<?> decoderHandler, PayloadTypeRegistryImpl<?> payloadTypeRegistry) {
		this.decoderHandler = decoderHandler;
		this.payloadTypeRegistry = payloadTypeRegistry;
	}

	protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		if (this.packetMerger != null) {
			ensureNotTransitioning(packet);

			CustomPayload payload = packet instanceof GenericPayloadAccessor accessor ? accessor.fabric_payload() : null;

			if (payload == null) {
				throw new DecoderException("Received '" + packet.getPacketType().id() + "' packet, while expecting 'minecraft:custom_payload'!");
			}

			if (!(payload instanceof FabricSplitPacketPayload splitPacketPayload)) {
				throw new DecoderException("Expected '" + FabricSplitPacketPayload.ID.id() +"' payload packet, but received '" + payload.getId().id() + "'!");
			}

			if (this.packetMerger.add(channelHandlerContext, splitPacketPayload, list)) {
				this.packetMerger = null;
			}
		} else if (packet instanceof GenericPayloadAccessor accessor && accessor.fabric_payload() instanceof FabricSplitPacketPayload payload) {
			ensureNotTransitioning(packet);
			ByteBuf buf = payload.byteBuf();
			int readerIndex = buf.readerIndex();

			// Skips packet id, as it's not needed
			VarInts.read(buf);
			Identifier payloadId = Identifier.PACKET_CODEC.decode(payload.byteBuf());

			buf.readerIndex(readerIndex);
			int maxSize = payloadTypeRegistry.getMaxPacketSize(payloadId);

			if (maxSize == -1) {
				throw new DecoderException("Received '" + payloadId + "' packet doesn't support splitting, but received split data!");
			}

			this.packetMerger = new Merger(this.decoderHandler, payloadId, maxSize);

			if (this.packetMerger.add(channelHandlerContext, payload, list)) {
				throw new DecoderException("Received '" + payloadId + "' as a split packet, but it wasn't actually split!");
			}
		} else {
			list.add(packet);

			if (packet.transitionsNetworkState()) {
				channelHandlerContext.pipeline().remove(channelHandlerContext.name());
			}
		}
	}

	private static void ensureNotTransitioning(Packet<?> packet) {
		if (packet.transitionsNetworkState()) {
			throw new DecoderException("Terminal message received in bundle");
		}
	}

	private static class Merger {
		private final DecoderHandlerAccessor decoderHandler;
		private final Identifier packetId;
		private final int maxSize;

		private final ByteBuf byteBuf;

		Merger(DecoderHandler<?> decoderHandler, Identifier identifier, int maxSize) {
			this.decoderHandler = (DecoderHandlerAccessor) decoderHandler;
			this.packetId = identifier;
			this.byteBuf = Unpooled.buffer();
			this.maxSize = maxSize;
		}

		boolean add(ChannelHandlerContext channelHandlerContext, FabricSplitPacketPayload payload, List<Object> objects) throws Exception {
			int newSize = this.byteBuf.readableBytes() + payload.byteBuf().readableBytes();

			if (this.maxSize < newSize) {
				throw new DecoderException("Received too much data for packet '" + this.packetId + "'! Expected up to " + this.maxSize + " bytes, received " + newSize + " bytes!");
			}

			this.byteBuf.writeBytes(payload.byteBuf());

			if (payload.finished()) {
				this.decoderHandler.fabric_decode(channelHandlerContext, byteBuf, objects);
				return true;
			}

			return false;
		}
	}
}
