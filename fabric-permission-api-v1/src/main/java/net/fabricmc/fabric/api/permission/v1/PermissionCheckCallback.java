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

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * The event used for getting the permission result for given context.
 *
 * <p>To check permissions, you should use dedicated methods from {@link PermissionContextOwner} interface
 * and it's implementations over invoking this event.
 */
public interface PermissionCheckCallback {
	Event<PermissionCheckCallback> EVENT = EventFactory.createArrayBacked(PermissionCheckCallback.class, callbacks -> new PermissionCheckCallback() {
		@Override
		public @Nullable <T> T onPermissionCheck(PermissionContext context, Identifier permission, Codec<T> permissionType) {
			for (PermissionCheckCallback callback : callbacks) {
				T value = callback.onPermissionCheck(context, permission, permissionType);

				if (value != null) {
					return value;
				}
			}

			return null;
		}
	});

	@Nullable
	<T> T onPermissionCheck(PermissionContext context, Identifier permission, Codec<T> permissionType);
}
