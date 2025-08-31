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
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import net.minecraft.network.handler.EncoderHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.state.NetworkState;

import net.fabricmc.fabric.mixin.networking.accessor.EncoderHandlerAccessor;

public class FabricPacketSplitter extends MessageToMessageEncoder<Packet<?>> {
	public static final int SAFE_SPLIT_SIZE = 1048576 - 2000;
	private final EncoderHandler<?> encoder;
	private final NetworkState<?> state;

	private int splitId = 0;

	public FabricPacketSplitter(NetworkState<?> state, EncoderHandler<?> encoderHandler) {
		this.state = state;
		this.encoder = encoderHandler;
	}

	protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
		if (packet instanceof SplittablePacket splittablePacket) {
			splittablePacket.fabric_split(this.splitId++, this.state, channelHandlerContext, this.encoder, packet, list::add);

			if (this.splitId > 1024) {
				this.splitId = 0;
			}
		} else {
			list.add(packet);
		}

		if (packet.transitionsNetworkState()) {
			channelHandlerContext.pipeline().remove(channelHandlerContext.name());
		}
	}

	public static void customFabricSplit(int id, ChannelHandlerContext channelHandlerContext, EncoderHandler<?> encoder, Packet<?> packet, Consumer<Packet<?>> consumer, int chunkSize) throws Exception {
		ByteBuf buf = Unpooled.buffer();
		((EncoderHandlerAccessor) encoder).fabric_encode(channelHandlerContext, packet, buf);

		consumer.accept(new CustomPayloadS2CPacket(new FabricSplitStartPacketPayload(id)));
		int part = 0;

		while (buf.readableBytes() > chunkSize) {
			consumer.accept(new CustomPayloadS2CPacket(new FabricSplitDataPacketPayload(id, part++, buf.readSlice(chunkSize))));
		}

		consumer.accept(new CustomPayloadS2CPacket(new FabricSplitDataPacketPayload(id, part, buf.readSlice(buf.readableBytes()))));
		consumer.accept(new CustomPayloadS2CPacket(new FabricSplitEndPacketPayload(id)));
	}
}
