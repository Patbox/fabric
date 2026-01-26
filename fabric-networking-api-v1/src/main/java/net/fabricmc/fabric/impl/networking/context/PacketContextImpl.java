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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.Connection;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public final class PacketContextImpl implements PacketContext {
	public static final ScopedValue<PacketContext> VALUE = ScopedValue.newInstance();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Connection connection;
	private final Map<Key<?>, Object> contextMap = new IdentityHashMap<>();

	public PacketContextImpl(Connection connection) {
		this.connection = connection;
	}

	@Override
	public @Nullable <T> T get(Key<T> key) {
		this.lock.readLock().lock();

		try {
			//noinspection unchecked
			return (T) this.contextMap.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public <T> void set(Key<T> key, T value) {
		this.lock.writeLock().lock();

		if (value == null) {
			this.contextMap.remove(key);
		} else {
			this.contextMap.put(key, value);
		}

		this.lock.writeLock().unlock();
	}

	@Override
	public Connection getOwner() {
		return this.connection;
	}
}
