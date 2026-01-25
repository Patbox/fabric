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

package net.fabricmc.fabric.api.networking.v1.context;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;

/**
 * This class allow to easily pass context between multiple packet listeners and packet serialization.
 * All connections get their own unique context object.
 */
@ApiStatus.NonExtendable
public interface PacketContext {
	/**
	 * Returns currently stored value.
	 *
	 * @param key unique key under which value is stored
	 * @return stored value or null if not set.
	 */
	@Nullable
	<T> T getValue(Key<T> key);

	/**
	 * Returns currently stored value.
	 * In case of it not being stored earlier, this method will throw.
	 *
	 * @param key unique key under which value is stored
	 * @return stored value
	 * @throws NullPointerException if not set
	 */
	default <T> T orElseThrow(Key<T> key) {
		return Objects.requireNonNull(getValue(key), () -> "Packet Context is missing the '" + key.key + "' value!");
	}

	/**
	 * Stores the value.
	 *
	 * @param key unique key under which value is stored
	 * @param value value to store
	 */
	<T> void setValue(Key<T> key, T value);

	/**
	 * Allows to update multiple values efficiently.
	 *
	 * @param updater consumer used to update stored context values
	 */
	void updateValues(Consumer<ContextUpdater> updater);

	/**
	 * Returns a connection that owns this packet context.
	 *
	 * @return the connection owning this context
	 */
	Connection getOwner();

	/**
	 * Returns currently set packet context.
	 *
	 * @return current context or null
	 */
	@Nullable
	static PacketContext get() {
		return PacketContextImpl.VALUE.get();
	}

	/**
	 * Returns currently set packet context.
	 * In case of context missing, this method will throw.
	 *
	 * @return current context or null
	 */
	static PacketContext orElseThrow() {
		return PacketContextImpl.VALUE.orElseThrow(() -> new RuntimeException("PacketContext is required, but it wasn't set up!"));
	}

	/**
	 * Runs specified runnable under a packet context.
	 *
	 * @param provider provider of the context
	 * @param runnable runnable to execute
	 */
	static void runWithContext(PacketContextProvider provider, Runnable runnable) {
		ScopedValue.where(PacketContextImpl.VALUE, provider.getPacketContext()).run(runnable);
	}

	/**
	 * Runs specified runnable under a packet context, returning a value.
	 *
	 * @param provider provider of the context
	 * @param supplier supplier to execute
	 * @return result of supplier
	 */
	static <T> T supplyWithContext(PacketContextProvider provider, Supplier<T> supplier) {
		return ScopedValue.where(PacketContextImpl.VALUE, provider.getPacketContext()).call(supplier::get);
	}

	/**
	 * Creates a new key to be used with the packet context.
	 *
	 * @param key identifier for this key
	 * @return a unique key
	 */
	static <T> Key<T> key(Identifier key) {
		return new Key<>(key);
	}

	@FunctionalInterface
	interface ContextUpdater {
		<T> void set(Key<T> key, T value);
	}

	final class Key<T> {
		private final Identifier key;

		private Key(Identifier key) {
			this.key = key;
		}

		@Override
		public String toString() {
			return "PacketContext.Key[" + this.key + "]";
		}
	}
}
