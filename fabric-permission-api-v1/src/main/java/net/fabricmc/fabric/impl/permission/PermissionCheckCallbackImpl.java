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

import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.permission.v1.PermissionCheckCallback;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;

/**
 * This class holds the permission checks split into 2 events.
 *
 * <p>The main event happens on current thread/synchroniously and should be used for non-players
 * and online players, as it should be relatively fast.
 *
 * <p>The async event can happen on any thread, being completed on same as one that executed it
 * or any other a mod needs to. API-side {@link PermissionCheckCallback} will have defaulted
 * implementation of the async one running on current thread.
 */
public class PermissionCheckCallbackImpl {
	public static final Event<Callback> MAIN_EVENT = EventFactory.createArrayBacked(Callback.class, callbacks -> new Callback() {
		@Override
		public <T> T onPermissionCheck(PermissionContext context, Identifier permission, Codec<T> permissionType) {
			for (Callback callback : callbacks) {
				T value = callback.onPermissionCheck(context, permission, permissionType);

				if (value != null) {
					return value;
				}
			}

			return null;
		}
	});

	public static final Event<AsyncCallback> ASYNC_EVENT = EventFactory.createArrayBacked(AsyncCallback.class, callbacks -> new AsyncCallback() {
		@Override
		public <T> CompletableFuture<T> onAsyncPermissionCheck(PermissionContext context, Identifier permission, Codec<T> permissionType) {
			CompletableFuture<T> res = CompletableFuture.completedFuture(null);

			for (AsyncCallback callback : callbacks) {
				CompletableFuture<T> future = callback.onAsyncPermissionCheck(context, permission, permissionType);

				if (future != null) {
					res = res.thenCompose(value -> {
						if (value != null) {
							return CompletableFuture.completedFuture(value);
						}

						return future;
					});
				}
			}

			return res;
		}
	});

	public interface Callback {
		<T> T onPermissionCheck(PermissionContext context, Identifier permission, Codec<T> permissionType);
	}

	public interface AsyncCallback {
		<T> @Nullable CompletableFuture<@Nullable T> onAsyncPermissionCheck(PermissionContext context, Identifier permission, Codec<T> permissionType);
	}
}
