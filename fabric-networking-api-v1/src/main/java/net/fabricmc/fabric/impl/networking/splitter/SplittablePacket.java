package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import net.fabricmc.fabric.mixin.networking.accessor.EncoderHandlerAccessor;

import net.minecraft.network.handler.EncoderHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.state.NetworkState;

import java.util.function.Consumer;

public interface SplittablePacket {
	void fabric_split(int id, NetworkState<?> state, ChannelHandlerContext channelHandlerContext, EncoderHandler<?> encoder, Packet<?> packet, Consumer<Packet<?>> consumer) throws Exception;
}
