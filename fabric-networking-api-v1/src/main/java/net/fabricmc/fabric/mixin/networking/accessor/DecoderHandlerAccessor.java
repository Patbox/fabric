package net.fabricmc.fabric.mixin.networking.accessor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import net.minecraft.network.handler.DecoderHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(DecoderHandler.class)
public interface DecoderHandlerAccessor {
	@Invoker("decode")
	void fabric_decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws Exception;
}
