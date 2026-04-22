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

package net.fabricmc.fabric.test.content.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.registry.fluid.FluidBehaviour;
import net.fabricmc.fabric.mixin.content.registry.fluid.LivingEntityAccessor;

public record TestFluidBehaviour() implements FluidBehaviour {
	@Override
	public void handleFluidInteractionUpdate(TagKey<Fluid> fluid, Entity entity, EntityFluidInteraction interaction, boolean canPushEntity) {
		if (canPushEntity) {
			interaction.applyCurrentTo(fluid, entity, 0.05);
		}

		entity.fallDistance *= 0.5f;
	}

	@Override
	public boolean canSwimInFluid(TagKey<Fluid> fluid, Entity entity) {
		return true;
	}

	@Override
	public boolean canMoveDownInFluid(TagKey<Fluid> tagKey, Entity entity) {
		return true;
	}

	@Override
	public boolean canDrownInFluid(TagKey<Fluid> fluid, LivingEntity entity) {
		return true;
	}

	@Override
	public void travelInFluid(TagKey<Fluid> fluid, LivingEntity entity, Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		entity.moveRelative(entity.getSpeed() * 0.5f, input);
		entity.move(MoverType.SELF, entity.getDeltaMovement());

		if (entity.getFluidHeight(fluid) <= entity.getFluidJumpThreshold()) {
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5f, 0.8, 0.5f));
			Vec3 movement = entity.getFluidFallingAdjustedMovement(baseGravity, isFalling, entity.getDeltaMovement());
			entity.setDeltaMovement(movement);
		} else {
			entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5f));
		}

		if (baseGravity != (double) 0.0F) {
			entity.setDeltaMovement(entity.getDeltaMovement().add(0.0F, -baseGravity / 6, 0.0F));
		}

		((LivingEntityAccessor) entity).callJumpOutOfFluid(oldY);
	}
}
