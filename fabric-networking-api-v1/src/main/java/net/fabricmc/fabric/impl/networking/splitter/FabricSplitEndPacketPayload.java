package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;

import net.fabricmc.fabric.impl.networking.FabricPacketsImpl;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FabricSplitEndPacketPayload(int splitId) implements FabricSplitPacketPayload {
	public static final Id<FabricSplitEndPacketPayload> ID = new Id<>(Identifier.of(FabricPacketsImpl.MOD_ID, "split/end"));
	public static final PacketCodec<ByteBuf, FabricSplitEndPacketPayload> CODEC = PacketCodecs.VAR_INT.xmap(FabricSplitEndPacketPayload::new, FabricSplitEndPacketPayload::splitId);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
