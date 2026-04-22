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

package net.fabricmc.fabric.api.registry.fluid;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

/**
 * Interface for handling common entity fluid interactions.
 */
@ApiStatus.Experimental
public interface FluidBehaviour {
	/**
	 * Called when fluid pushing should be applied to an entity.
	 *
	 * @param fluid         a tag key representing the fluid type
	 * @param entity        entity that fluid interaction update is processed for
	 * @param interaction   entity's fluid interaction tracker, can be used to query values or apply fluid current
	 * @param canPushEntity controls whatever entity can be pushed
	 */
	default void handleFluidInteractionUpdate(TagKey<Fluid> fluid, Entity entity, EntityFluidInteraction interaction, boolean canPushEntity) {

	}

	/**
	 * Used to determine whatever player can swim in a fluid.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that fluid interaction update is processed for
	 */
	default boolean canSwimInFluid(TagKey<Fluid> fluid, Entity entity) {
		return false;
	}

	/**
	 * Used to apply fluid movement logic for the entity.
	 * For implementing this method, you should look into how vanilla handles,
	 * movement in fluids at {@link LivingEntity#travelInWater(Vec3, double, boolean, double)}
	 * and {@link LivingEntity#travelInLava(Vec3, double, boolean, double)}.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that is moving through a fluid
	 * @param input entity's movement input
	 * @param baseGravity entity's gravity
	 * @param isFalling whatever entity is currently falling or not
	 * @param oldY old y position value
	 */
	default void travelInFluid(TagKey<Fluid> fluid, LivingEntity entity, Vec3 input, double baseGravity, boolean isFalling, double oldY) {

	}

	/**
	 * Checks if player can controllably go down faster by sneaking while in fluid.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that is moving through a fluid
	 */
	default boolean canMoveDownInFluid(TagKey<Fluid> fluid, Entity entity) {
		return false;
	}

	/**
	 * Checks if entity should drown while submerged in fluid.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that is moving through a fluid
	 */
	default boolean canDrownInFluid(TagKey<Fluid> fluid, LivingEntity entity) {
		return false;
	}
}
