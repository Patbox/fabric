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

package net.fabricmc.fabric.mixin.recipe.sync;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerRecipeBook;sendInitRecipesPacket(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void sendRecipesOnJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
		RecipeSyncImpl.sendRecipes(server, player);
	}

	@Inject(method = "onDataPacksReloaded", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerRecipeBook;sendInitRecipesPacket(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void sendRecipesOnReload(CallbackInfo ci, @Local ServerPlayerEntity player) {
		RecipeSyncImpl.sendRecipes(server, player);
	}
}
