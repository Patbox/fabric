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
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

import net.fabricmc.fabric.mixin.networking.accessor.DecoderHandlerAccessor;

public class FabricPacketMerger extends MessageToMessageDecoder<Packet<?>> {
	private final DecoderHandler<?> decoderHandler;
	@Nullable
	private Merger packetMerger;

	public FabricPacketMerger(DecoderHandler<?> decoderHandler) {
		this.decoderHandler = decoderHandler;
	}

	protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		if (this.packetMerger != null) {
			ensureNotTransitioning(packet);

			if (this.packetMerger.add(channelHandlerContext, packet, list)) {
				this.packetMerger = null;
			}
		} else if (packet instanceof CustomPayloadS2CPacket payloadPacket && payloadPacket.payload() instanceof FabricSplitStartPacketPayload payload) {
			ensureNotTransitioning(packet);

			this.packetMerger = new Merger(this.decoderHandler, payload.splitId());
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
		private final int id;
		private final DecoderHandlerAccessor decoderHandler;
		private int part = 0;

		private final ByteBuf byteBuf;

		Merger(DecoderHandler<?> decoderHandler, int id) {
			this.decoderHandler = (DecoderHandlerAccessor) decoderHandler;
			this.id = id;
			this.byteBuf = Unpooled.buffer();
		}

		boolean add(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> objects) throws Exception {
			if (!(packet instanceof CustomPayloadS2CPacket customPayloadS2CPacket) || !(customPayloadS2CPacket.payload() instanceof FabricSplitPacketPayload payload)) {
				throw new DecoderException("Expected CustomPayloadS2CPacket of type FabricSplitPacketPayload, but received something else!");
			}

			if (payload.splitId() != this.id) {
				throw new DecoderException("Received wrong id of FabricSplitPacketPayload! Expected " + this.id + " received " + payload.splitId() + "!");
			}

			if (payload instanceof FabricSplitDataPacketPayload dataPacketPayload) {
				if (this.part != dataPacketPayload.part()) {
					throw new DecoderException("Received wrong part of FabricSplitDataPacketPayload! Expected " + this.part + " received " + dataPacketPayload.part() + "!");
				}

				this.part++;
				this.byteBuf.writeBytes(dataPacketPayload.byteBuf());
				return false;
			}

			if (payload instanceof FabricSplitEndPacketPayload) {
				this.decoderHandler.fabric_decode(channelHandlerContext, byteBuf, objects);
				return true;
			}

			return false;
		}
	}
}
