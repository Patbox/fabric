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

package net.fabricmc.fabric.mixin.content.registry.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.impl.content.registry.fluid.EntityFluidInteractionRegistryImpl;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@ModifyExpressionValue(method = "shouldTravelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z"))
	private boolean isInCustomFluid(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			var inFluid = ((EntityAccessor) this).getFluidInteraction().isInFluid(tagKey);

			if (inFluid) {
				return true;
			}
		}

		return false;
	}

	@WrapWithCondition(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travelInLava(Lnet/minecraft/world/phys/Vec3;DZD)V"))
	private boolean travenInCustomFluid(LivingEntity instance, Vec3 vec3, double input, boolean baseGravity, double isFalling) {
		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			var inFluid = ((EntityAccessor) this).getFluidInteraction().isInFluid(tagKey);

			if (inFluid) {
				EntityFluidInteractionRegistryImpl.getFluidBehaviour(tagKey).travelInFluid(tagKey, (LivingEntity) (Object) this, vec3, input, baseGravity, isFalling);
				return false;
			}
		}

		return true;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D", ordinal = 1))
	private double tryOtherFluids(double original, @Share("fluid") LocalRef<TagKey<Fluid>> fluid) {
		if (original != 0) {
			return original;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			var inFluid = ((EntityAccessor) this).getFluidInteraction().isInFluid(tagKey);

			if (inFluid) {
				fluid.set(tagKey);
				return ((EntityAccessor) this).getFluidInteraction().getFluidHeight(tagKey);
			}
		}

		return 0;
	}

	@ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean customFluidDrowning(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			var inFluid = ((EntityAccessor) this).getFluidInteraction().isEyeInFluid(tagKey);

			if (inFluid && EntityFluidInteractionRegistryImpl.getFluidBehaviour(tagKey).canDrownInFluid(tagKey, (LivingEntity) (Object) this)) {
				return true;
			}
		}

		return false;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z", ordinal = 1))
	private boolean jumpInCustomFluid(boolean original, @Share("fluid") LocalRef<TagKey<Fluid>> fluid) {
		return original || fluid.get() != null;
	}

	@ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpInLiquid(Lnet/minecraft/tags/TagKey;)V", ordinal = 0))
	private TagKey<Fluid> swapFluidTag(TagKey<Fluid> fluidTagKey, @Share("fluid") LocalRef<TagKey<Fluid>> fluid) {
		var custom = fluid.get();
		return custom != null ? custom : fluidTagKey;
	}
}
