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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.permission.PermissionCheckCallbackImpl;

/**
 * The event used for getting the permission result for given context.
 * Implemented callbacks for this event should be thread safe, as permission methods can be called from another thread.
 * Additionally, the execution should be reasonably fast for non-player and online player cases.
 * Offline player checks are allowed to be slower and can happen asynchronously.
 *
 * <p>When implementing this callback, only {@link PermissionCheckCallback#onPermissionCheck} needs to be implemented,
 * but for better performance in case of support for async lookup, the {@link PermissionCheckCallback#onAsyncPermissionCheck}
 * method should also be implemented. In case it wasn't it will default to running {@link PermissionCheckCallback#onPermissionCheck}
 * on current thread.
 *
 * <p>To check for permissions, you should use dedicated methods from {@link PermissionContextOwner} interface
 * and it's implementations over invoking this event.
 */
public interface PermissionCheckCallback {
	/**
	 * Registers the permission callback.
	 *
	 * @param callback permission check callback to register
	 */
	static void register(PermissionCheckCallback callback) {
		register(Event.DEFAULT_PHASE, callback);
	}

	/**
	 * Registers the permission callback.
	 *
	 * @param phase ordering phase to place the callback
	 * @param callback permission check callback to register
	 */
	static void register(Identifier phase, PermissionCheckCallback callback) {
		Objects.requireNonNull(phase, "phase can't be null!");
		Objects.requireNonNull(callback, "callback can't be null!");

		PermissionCheckCallbackImpl.MAIN_EVENT.register(phase, callback::onPermissionCheck);
		PermissionCheckCallbackImpl.ASYNC_EVENT.register(phase, callback::onAsyncPermissionCheck);
	}

	/**
	 * Orders the phases in provided order.
	 *
	 * @param firstPhase the id of the phase that should happen first
	 * @param lastPhase the id of the phase that should happen last
	 */
	static void addPhaseOrdering(Identifier firstPhase, Identifier lastPhase) {
		Objects.requireNonNull(firstPhase, "firstPhase can't be null!");
		Objects.requireNonNull(lastPhase, "lastPhase can't be null!");

		PermissionCheckCallbackImpl.MAIN_EVENT.addPhaseOrdering(firstPhase, lastPhase);
		PermissionCheckCallbackImpl.ASYNC_EVENT.addPhaseOrdering(firstPhase, lastPhase);
	}

	/**
	 * Main check method, executes on current thread.
	 *
	 * @param context        context to check for
	 * @param permission     a permission node representing a permission
	 * @param <T>            type of permission
	 * @return value of type T if present, null to pass through.
	 */
	@ApiStatus.OverrideOnly
	@Nullable
	<T> T onPermissionCheck(PermissionContext context, PermissionNode<T> permission);

	/**
	 * Async permission check method.
	 *
	 * @param context        context to check for
	 * @param permission     a permission node representing a permission
	 * @param <T>            type of permission
	 * @return a completable future value of type T if present, null or null containing completable future to quickly pass through to next callback.
	 */
	@ApiStatus.OverrideOnly
	default <T> CompletableFuture<@Nullable T> onAsyncPermissionCheck(PermissionContext context, PermissionNode<T> permission) {
		return CompletableFuture.completedFuture(this.onPermissionCheck(context, permission));
	}
}
