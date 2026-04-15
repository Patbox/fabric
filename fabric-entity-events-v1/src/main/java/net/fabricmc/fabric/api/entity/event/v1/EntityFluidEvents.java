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

package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events related to fluid interactions with entities.
 */
public final class EntityFluidEvents {
	/**
	 * An event to handle the fluid interaction update for all entities.
	 * Listeners should return true, if entity is interacting with mods custom fluid, false otherwise.
	 */
	public static final Event<FluidInteractionUpdate> ON_FLUID_INTERACTION_UPDATE = EventFactory.createArrayBacked(FluidInteractionUpdate.class, listeners -> (entity, interaction, shouldBePushed) -> {
		boolean interacted = false;

		for (FluidInteractionUpdate listener : listeners) {
			interacted |= listener.onFluidInteractionUpdate(entity, interaction, shouldBePushed);
		}

		return interacted;
	});

	@FunctionalInterface
	public interface FluidInteractionUpdate {
		/**
		 * Called when entity processes the fluid interaction updates.
		 *
		 * @param entity entity that fluid interaction update is processed for
		 * @param interaction entity's fluid interaction tracker, can be used to query values or apply fluid current
		 * @return true if entity interacted with a custom fluid, false otherwise
		 */
		boolean onFluidInteractionUpdate(Entity entity, EntityFluidInteraction interaction, boolean shouldBePushed);
	}

	private EntityFluidEvents() {
	}
}
