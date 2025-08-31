package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;

import net.fabricmc.fabric.impl.networking.FabricPacketsImpl;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FabricSplitStartPacketPayload(int splitId) implements CustomPayload {
	public static final Id<FabricSplitStartPacketPayload> ID = new Id<>(Identifier.of(FabricPacketsImpl.MOD_ID, "split/start"));
	public static final PacketCodec<ByteBuf, FabricSplitStartPacketPayload> CODEC = PacketCodecs.VAR_INT.xmap(FabricSplitStartPacketPayload::new, FabricSplitStartPacketPayload::splitId);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
