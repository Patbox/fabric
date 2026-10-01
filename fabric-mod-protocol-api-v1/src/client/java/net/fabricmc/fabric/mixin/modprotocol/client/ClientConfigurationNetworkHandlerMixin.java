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

package net.fabricmc.fabric.mixin.modprotocol.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationNetworkHandlerMixin extends ClientCommonPacketListenerImpl {
	protected ClientConfigurationNetworkHandlerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
		super(minecraft, connection, cookie);
	}

	@Inject(method = "handleSelectKnownPacks", at = @At("HEAD"), cancellable = true)
	private void preventJoiningIncompatibleServers(ClientboundSelectKnownPacks packet, CallbackInfo ci) {
		if (this.getPacketContext().get(ModProtocolManager.REMOTE_MOD_VERSIONS_KEY) == null && !ModProtocolManager.REQUIRED_ON_SERVER.isEmpty()) {
			this.minecraft.execute(() -> this.connection.disconnect(ModProtocolManager.constructMessage(ModProtocolManager.REQUIRED_ON_SERVER, ModProtocolManager.LOCAL_MOD_PROTOCOLS_BY_ID)));
			ci.cancel();
		}
	}
}
