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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public class CommandPermissionContext implements PermissionContext {
	private final CommandSourceStack source;
	private final Type type;
	private final UUID uuid;

	public CommandPermissionContext(CommandSourceStack source) {
		this.source = source;
		this.type = switch (source.getEntity()) {
		case Player player -> Type.PLAYER;
		case Entity entity -> Type.ENTITY;
		case null -> Type.SYSTEM;
		};
		this.uuid = switch (source.getEntity()) {
		case Entity entity -> entity.getUUID();
		case null -> Util.NIL_UUID;
		};
	}

	@Override
	public UUID uuid() {
		return this.uuid;
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <T> T get(Key<T> key) {
		if (key == PermissionContext.POSITION) {
			return (T) this.source.getPosition();
		} else if (key == PermissionContext.BLOCK_POSITION) {
			return (T) BlockPos.containing(this.source.getPosition());
		} else if (key == PermissionContext.LEVEL) {
			return (T) this.source.getLevel();
		} else if (key == PermissionContext.ENTITY) {
			return (T) this.source.getEntity();
		} else if (key == PermissionContext.COMMAND_SOURCE_STACK) {
			return (T) this;
		}

		return null;
	}

	@Override
	public PermissionLevel permissionLevel() {
		return this.source.permissions() instanceof LevelBasedPermissionSet levelBasedPermissionSet ? levelBasedPermissionSet.level() : PermissionLevel.ALL;
	}

	@Override
	public Type type() {
		return type;
	}
}
