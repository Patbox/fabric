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

package net.fabricmc.fabric.impl.networking.context;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.Connection;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public final class PacketContextImpl implements PacketContext {
	public static final ScopedValue<PacketContext> VALUE = ScopedValue.newInstance();
	private final Connection connection;
	private volatile Map<Key<?>, Object> contextMap = Map.of();

	public PacketContextImpl(Connection connection) {
		this.connection = connection;
	}

	@Override
	public @Nullable <T> T getValue(Key<T> key) {
		//noinspection unchecked
		return (T) this.contextMap.get(key);
	}

	@Override
	public <T> void setValue(Key<T> key, T value) {
		// Values can be set/read from multiple threads, so making it safe is kinda needed.
		// Also setting values should be way less common than reading them, so keeping that fast,
		// While putting synchronization + new map for when new values are set.
		// When more values at the same time, the updateValues method should be used.
		synchronized (this) {
			var map = new IdentityHashMap<>(contextMap);
			map.put(key, value);
			this.contextMap = map;
		}
	}

	@Override
	public void updateValues(Consumer<ContextUpdater> updater) {
		// Same as with setValue
		synchronized (this) {
			var map = new IdentityHashMap<>(contextMap);
			updater.accept(map::put);
			this.contextMap = map;
		}
	}

	@Override
	public Connection getOwner() {
		return this.connection;
	}
}
