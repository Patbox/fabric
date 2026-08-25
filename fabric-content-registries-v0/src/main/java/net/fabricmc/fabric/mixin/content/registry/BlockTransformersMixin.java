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

package net.fabricmc.fabric.mixin.content.registry;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.core.component.BlockTransformer;
import net.minecraft.world.item.component.BlockTransformers;

import net.fabricmc.fabric.impl.content.registry.BlockTransformerRegistryImpl;

@Mixin(BlockTransformers.class)
abstract class BlockTransformersMixin {
	@ModifyArg(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/BlockTransformer;<init>(Ljava/util/List;)V", ordinal = 0))
	private static List<BlockTransformer.BlockTransformData> addShovelTransformations(List<BlockTransformer.BlockTransformData> transformations) {
		return BlockTransformerRegistryImpl.addShovelTransformations(transformations);
	}

	@ModifyArg(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/BlockTransformer;<init>(Ljava/util/List;)V", ordinal = 1))
	private static List<BlockTransformer.BlockTransformData> addAxeTransformations(List<BlockTransformer.BlockTransformData> transformations) {
		return BlockTransformerRegistryImpl.addAxeTransformations(transformations);
	}

	@ModifyArg(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/BlockTransformer;<init>(Ljava/util/List;)V", ordinal = 2))
	private static List<BlockTransformer.BlockTransformData> addHoeTransformations(List<BlockTransformer.BlockTransformData> transformations) {
		return BlockTransformerRegistryImpl.addHoeTransformations(transformations);
	}
}
