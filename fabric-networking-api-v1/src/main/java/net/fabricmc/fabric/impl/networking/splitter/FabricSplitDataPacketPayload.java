package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;

import net.fabricmc.fabric.impl.networking.FabricPacketsImpl;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FabricSplitDataPacketPayload(int splitId, int part, ByteBuf byteBuf) implements FabricSplitPacketPayload {
	public static final Id<FabricSplitDataPacketPayload> ID = new Id<>(Identifier.of(FabricPacketsImpl.MOD_ID, "split/data"));
	public static final PacketCodec<ByteBuf, FabricSplitDataPacketPayload> CODEC = PacketCodec.ofStatic(FabricSplitDataPacketPayload::write, FabricSplitDataPacketPayload::read);

	private static FabricSplitDataPacketPayload read(ByteBuf buf) {
		var id = VarInts.read(buf);
		var part = VarInts.read(buf);
		var size = VarInts.read(buf);
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
