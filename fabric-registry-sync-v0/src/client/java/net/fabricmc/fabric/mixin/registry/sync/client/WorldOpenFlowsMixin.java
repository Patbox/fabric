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

package net.fabricmc.fabric.mixin.registry.sync.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.storage.LevelStorageSource;

import net.fabricmc.fabric.impl.registry.sync.validate.RegistryCustomContentState;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
	@Unique
	private boolean missingRegistryEntries = false;

	@ModifyExpressionValue(method = "openWorldCheckWorldStemCompatibility", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/WorldData;worldGenSettingsLifecycle()Lcom/mojang/serialization/Lifecycle;"))
	private Lifecycle injectHereForCustomScreen(Lifecycle original, @Local(argsOnly = true) LevelStorageSource.LevelStorageAccess worldAccess, @Local(argsOnly = true) WorldStem worldStem) {
		RegistryCustomContentState state = RegistryCustomContentState.readFile(worldAccess);

		this.missingRegistryEntries = !state.validate(worldStem.registries().compositeAccess()).isEmpty();
		return this.missingRegistryEntries ? Lifecycle.deprecated(0) : original;
	}

	@ModifyArgs(method = "askForBackup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/BackupConfirmScreen;<init>(Ljava/lang/Runnable;Lnet/minecraft/client/gui/screens/BackupConfirmScreen$Listener;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Z)V"))
	private void correctWarningText(Args args) {
		if (this.missingRegistryEntries) {
			args.set(2, Component.translatable("fabric-registry-sync-v0.missing-entries.title"));
			args.set(3, Component.translatable("fabric-registry-sync-v0.missing-entries.description"));
		}
	}
}
