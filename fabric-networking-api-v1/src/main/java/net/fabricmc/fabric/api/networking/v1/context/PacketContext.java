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
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;

import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;

/**
 * This class allow to easily pass context between multiple packet listeners and packet serialization.
 * All connections get their own unique context object.
 */
@ApiStatus.NonExtendable
public interface PacketContext {
	/**
	 * The server instance that handles this connection. Only present on clientbound connections.
	 * This value is set once the {@link ServerHandshakePacketListenerImpl} is constructed.
	 */
	ReadKey<MinecraftServer> SERVER_INSTANCE = PacketContextImpl.SERVER_INSTANCE;
	/**
	 * The Game Profile attached to this connection.
	 * This value is set on both server and client, once the login process succeeds.
	 */
	ReadKey<GameProfile> GAME_PROFILE = PacketContextImpl.GAME_PROFILE;
	/**
	 * The connection that owns this packet context.
	 * This value is always present.
	 */
	ReadKey<@NonNull Connection> CONNECTION = PacketContextImpl.CONNECTION;

	/**
	 * Returns currently stored value.
	 *
	 * @param key unique key under which value is stored
	 * @return stored value or null if not set.
	 */
	@Nullable
	<T> T get(ReadKey<T> key);

	/**
	 * Returns currently stored value.
	 * In case of it not being stored earlier, this method will throw.
	 *
	 * @param key unique key under which value is stored
	 * @return stored value
	 * @throws NullPointerException if not set
	 */
	default <T> T orElseThrow(ReadKey<T> key) {
		return Objects.requireNonNull(get(key), () -> "Packet Context is missing the '" + ((PacketContextImpl.KeyImpl<T>) key).key() + "' value!");
	}

	/**
	 * Returns currently stored value.
	 * In case of it not being stored earlier, this method will return provided default value.
	 *
	 * @param key unique key under which value is stored
	 * @param defaultValue value to return if no value is set
	 * @return stored value if present, defaultValue otherwise
	 */
	default <T> T orElse(ReadKey<T> key, T defaultValue) {
		return Objects.requireNonNullElse(get(key), defaultValue);
	}

	/**
	 * Stores the value.
	 *
	 * @param key unique key under which value is stored
	 * @param value value to store
	 */
	<T> void set(Key<T> key, @Nullable T value);

	/**
	 * Returns currently set packet context.
	 *
	 * @return current context or null
	 */
	@Nullable
	static PacketContext get() {
		if (PacketContextImpl.VALUE.isBound()) {
			return PacketContextImpl.VALUE.get();
		}

		return null;
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
		return new PacketContextImpl.KeyImpl<>(key);
	}

	@ApiStatus.NonExtendable
	interface ReadKey<T> { }

	@ApiStatus.NonExtendable
	interface Key<T> extends ReadKey<T> { }
}
