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

package net.fabricmc.fabric.impl.permission;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public class EntityPermissionContext implements PermissionContext {
	private final Entity entity;
	private final Type type;

	public EntityPermissionContext(Entity entity) {
		this.entity = entity;
		this.type = entity instanceof Player ? Type.PLAYER : Type.ENTITY;
	}

	@Override
	public UUID uuid() {
		return this.entity.getUUID();
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <T> T get(Key<T> key) {
		if (key == PermissionContext.POSITION) {
			return (T) this.entity.position();
		} else if (key == PermissionContext.BLOCK_POSITION) {
			return (T) this.entity.blockPosition();
		} else if (key == PermissionContext.LEVEL) {
			return (T) this.entity.level();
		} else if (key == PermissionContext.ENTITY) {
			return (T) this.entity;
		} else if (key == PermissionContext.COMMAND_SOURCE_STACK) {
			return (T) this.entity instanceof ServerPlayer player ? (T) player.commandSource() : null;
		}

		return null;
	}

	@Override
	public PermissionLevel permissionLevel() {
		return entity instanceof Player player && player.permissions() instanceof LevelBasedPermissionSet levelBasedPermissionSet
				? levelBasedPermissionSet.level()
				: PermissionLevel.ALL;
	}

	@Override
	public Type type() {
		return type;
	}
}
