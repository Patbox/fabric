package net.fabricmc.fabric.impl.networking.splitter;

import net.minecraft.network.packet.CustomPayload;

public interface FabricSplitPacketPayload extends CustomPayload {
	int splitId();
}
