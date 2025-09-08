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

			if (this.packetMerger.add(channelHandlerContext, payload, list)) {
				this.packetMerger = null;
			}
		} else if (packet instanceof GenericPayloadAccessor accessor && accessor.fabric_payload() instanceof FabricSplitStartPacketPayload payload) {
			ensureNotTransitioning(packet);
			int maxSize = payloadTypeRegistry.getMaxPacketSize(payload.packetId());

			if (maxSize == -1) {
				throw new DecoderException("Received '" + payload.packetId() + "' packet doesn't support splitting, but received split start!");
			} else if (maxSize < payload.size()) {
				throw new DecoderException("Received '" + payload.packetId() + "' packet is larger than max allowed size! Got " + payload.size() + " bytes, expected " + maxSize + " bytes!");
			}

			this.packetMerger = new Merger(this.decoderHandler, payload.packetId(), payload.size());
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
		private int part = 0;

		private final int expectedSize;

		private final ByteBuf byteBuf;

		Merger(DecoderHandler<?> decoderHandler, Identifier identifier, int expectedSize) {
			this.decoderHandler = (DecoderHandlerAccessor) decoderHandler;
			this.packetId = identifier;
			this.byteBuf = Unpooled.buffer(expectedSize);
			this.expectedSize = expectedSize;
		}

		boolean add(ChannelHandlerContext channelHandlerContext, CustomPayload payload, List<Object> objects) throws Exception {
			if (payload instanceof FabricSplitDataPacketPayload dataPacketPayload) {
				if (this.part != dataPacketPayload.part()) {
					throw new DecoderException("Received wrong part of '" + FabricSplitDataPacketPayload.ID.id() + "'! Expected " + this.part + " received " + dataPacketPayload.part() + "!");
				}

				int newSize = this.byteBuf.readableBytes() + dataPacketPayload.byteBuf().readableBytes();

				if (this.expectedSize < newSize) {
					throw new DecoderException("Received too much data for packet '" + this.packetId + "'! Expected " + this.expectedSize + " bytes received " + newSize + " bytes!");
				}

				this.part++;

				this.byteBuf.writeBytes(dataPacketPayload.byteBuf());
				return false;
			} else if (payload instanceof FabricSplitEndPacketPayload) {
				this.decoderHandler.fabric_decode(channelHandlerContext, byteBuf, objects);
				return true;
			} else {
				throw new DecoderException("Expected '" + FabricSplitDataPacketPayload.ID.id() + "' or '" + FabricSplitEndPacketPayload.ID.id() +"' payload packets, but received '" + payload.getId().id() + "'!");
			}
		}
	}
}
