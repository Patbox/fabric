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

import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.impl.permission.PermissionCheckCallbackImpl;

/**
 * Utility interface allowing quick access for permission checking methods.
 * Implemented by default on {@link Entity}, {@link CommandSourceStack} and {@link PermissionContext}.
 * Other mods are allowed to implement this on their own classes as well.
 *
 * <p>See {@link PermissionContext} for creation and modification of permission contexts.
 *
 * <p>Example usage:
 * <pre>{@code
 * Identifier claimBypassPermission = Identifier.fromNamespaceAndPath("potatoclaims", "bypass_protection");
 * ServerPlayer player = ...;
 *
 * AttackEntityCallback.EVENT.register((playerEntity, _, _, entity, _) -> {
 *     if (ModChecks.isProtected(entity) && !player.checkPermission(claimBypassPermission, PermissionLevel.GAMEMASTERS)) {
 *         return InteractionResult.FAIL;
 *     }
 *     return InteractionResult.PASS;
 * });
 * }</pre>
 */
public interface PermissionContextOwner {
	/**
	 * Provides the permission context.
	 * In case of entities, this context will be dynamic.
	 *
	 * @return PermissionContext attached to this object
	 */
	default PermissionContext getPermissionContext() {
		throw new IllegalStateException("Implemented via Mixin");
	}

	/**
	 * Simple permission check. Should be used to check if something is allowed.
	 *
	 * @param permission a permission identifier to check against
	 * @return TriState returning value of the permission (DEFAULT if not changed)
	 */
	default TriState checkPermission(Identifier permission) {
		return checkPermission(permission, PermissionCodecs.TRI_STATE, TriState.DEFAULT);
	}

	/**
	 * Simple permission check. Should be used to check if something is allowed.
	 * Will default to {@param defaultValue} if permission value not is not provided.
	 *
	 * @param permission a permission identifier to check against
	 * @param defaultValue fallback value
	 * @return a boolean representing state of the permission, returns defaultValue if not modified by other mods
	 */
	default boolean checkPermission(Identifier permission, boolean defaultValue) {
		return checkPermission(permission, PermissionCodecs.TRI_STATE, TriState.DEFAULT).toBoolean(defaultValue);
	}

	/**
	 * Simple permission check. Should be used to check if something is allowed.
	 * Will check for vanilla permission level, if permission value not is not provided.
	 *
	 * @param permission a permission identifier to check against
	 * @param defaultPermissionLevel a fallback permission level to check against
	 * @return a boolean representing state of the permission
	 */
	default boolean checkPermission(Identifier permission, PermissionLevel defaultPermissionLevel) {
		PermissionLevel permissionLevel = this.getPermissionContext().permissionLevel();
		return checkPermission(permission, PermissionCodecs.TRI_STATE, TriState.DEFAULT).toBoolean(permissionLevel.isEqualOrHigherThan(defaultPermissionLevel));
	}

	/**
	 * A dynamic, typed permission check. Should be used to check for more complex permission values,
	 * like allowed amount and alike.
	 *
	 * @param permission a permission identifier to check against
	 * @param type codec representing the type of the permission
	 * @param <T> type of the permission
	 * @return value of the permission or null if not provided
	 */
	@Nullable
	default <T> T checkPermission(Identifier permission, Codec<T> type) {
		return checkPermission(permission, type, null);
	}

	/**
	 * A dynamic, typed permission check. Should be used to check for more complex permission values,
	 * like allowed amount and alike.
	 *
	 * @param permission a permission identifier to check against
	 * @param type codec representing the type of the permission
	 * @param defaultValue fallback value, if not provided
	 * @param <T> type of the permission
	 * @return  value of the permission or {@param defaultValue} if not provided
	 */
	default <T> T checkPermission(Identifier permission, Codec<T> type, T defaultValue) {
		T value = PermissionCheckCallbackImpl.MAIN_EVENT.invoker().onPermissionCheck(this.getPermissionContext(), permission, type);

		return value != null ? value : defaultValue;
	}

	/**
	 * Asynchronous simple permission check. Should be used to check if something is allowed.
	 *
	 * @param permission a permission identifier to check against
	 * @return TriState returning value of the permission (DEFAULT if not changed)
	 */
	default CompletableFuture<TriState> checkPermissionAsync(Identifier permission) {
		return checkPermissionAsync(permission, PermissionCodecs.TRI_STATE, TriState.DEFAULT);
	}

	/**
	 * Asynchronous simple permission check. Should be used to check if something is allowed.
	 * Will default to {@param defaultValue} if permission value not is not provided.
	 *
	 * @param permission a permission identifier to check against
	 * @param defaultValue fallback value
	 * @return a boolean representing state of the permission, returns defaultValue if not modified by other mods
	 */
	default CompletableFuture<Boolean> checkPermissionAsync(Identifier permission, boolean defaultValue) {
		return this.checkPermissionAsync(permission).thenApply(x -> x.toBoolean(defaultValue));
	}

	/**
	 * Asynchronous simple permission check. Should be used to check if something is allowed.
	 * Will check for vanilla permission level, if permission value not is not provided.
	 *
	 * @param permission a permission identifier to check against
	 * @param defaultPermissionLevel a fallback permission level to check against
	 * @return a boolean representing state of the permission
	 */
	default CompletableFuture<Boolean> checkPermissionAsync(Identifier permission, PermissionLevel defaultPermissionLevel) {
		boolean permissionLevelValue = this.getPermissionContext().permissionLevel().isEqualOrHigherThan(defaultPermissionLevel);
		return this.checkPermissionAsync(permission).thenApply(x -> x.toBoolean(permissionLevelValue));
	}

	/**
	 * Asynchronous, dynamic and typed permission check. Should be used to check for more complex permission values,
	 * like allowed amount and alike.
	 *
	 * @param permission a permission identifier to check against
	 * @param type codec representing the type of the permission
	 * @param <T> type of the permission
	 * @return value of the permission or null if not provided
	 */
	default <T> CompletableFuture<@Nullable T> checkPermissionAsync(Identifier permission, Codec<T> type) {
		return checkPermissionAsync(permission, type, null);
	}

	/**
	 * Asynchronous, dynamic and typed permission check. Should be used to check for more complex permission values,
	 * like allowed amount and alike.
	 *
	 * @param permission a permission identifier to check against
	 * @param type codec representing the type of the permission
	 * @param defaultValue fallback value, if not provided
	 * @param <T> type of the permission
	 * @return value of the permission or {@param defaultValue} if not provided
	 */
	default <T> CompletableFuture<T> checkPermissionAsync(Identifier permission, Codec<T> type, T defaultValue) {
		CompletableFuture<T> value = PermissionCheckCallbackImpl.ASYNC_EVENT.invoker().onAsyncPermissionCheck(this.getPermissionContext(), permission, type);

		return value != null ? value.thenApply(val -> val != null ? val : defaultValue) : CompletableFuture.completedFuture(defaultValue);
	}
}
