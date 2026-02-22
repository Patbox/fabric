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

package net.fabricmc.fabric.api.permission.v1;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;

/**
 * Utility interface allowing quick access for permission checking methods.
 * Implemented by default on {@link Entity}, {@link CommandSourceStack} and {@link PermissionContext}
 *
 * <p>See {@link PermissionContext}
 */
public interface PermissionContextOwner {
	default PermissionContext getPermissionContext() {
		throw new IllegalStateException("Implemented via Mixin");
	}

	default TriState checkPermission(Identifier permission) {
		return checkPermission(permission, PermissionCodec.TRI_STATE, TriState.DEFAULT);
	}

	default boolean checkPermission(Identifier permission, boolean defaultValue) {
		return checkPermission(permission, PermissionCodec.TRI_STATE, TriState.DEFAULT).toBoolean(defaultValue);
	}

	default boolean checkPermission(Identifier permission, PermissionLevel defaultPermissionLevel) {
		PermissionLevel permissionLevel = this.getPermissionContext().permissionLevel();
		return checkPermission(permission, PermissionCodec.TRI_STATE, TriState.DEFAULT).toBoolean(permissionLevel.isEqualOrHigherThan(defaultPermissionLevel));
	}

	@Nullable
	default <T> T checkPermission(Identifier permission, Codec<T> type) {
		return checkPermission(permission, type, null);
	}

	default <T> T checkPermission(Identifier permission, Codec<T> type, T defaultValue) {
		PermissionContext ctx = this.getPermissionContext();
		T value = PermissionCheckCallback.EVENT.invoker().onPermissionCheck(ctx, permission, type);
		return value != null ? value : defaultValue;
	}
}
