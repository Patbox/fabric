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

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.permission.PermissionNodeImpl;

/**
 * This class represents a permission, which consist of identifier as the key and a codec to dictate
 * its type. This class can be instantiated dynamically (just before permission request) or statically
 * on mod initialization. Newly created instances that use equal keys and the same codec instance
 * are considered to be equal.
 *
 * @param <T> type of the permission
 */
@ApiStatus.NonExtendable
public interface PermissionNode<T> {
	/**
	 * Creates a permission node of boolean type.
	 * Primarily used for simple permission checks (if owner can/can't do something).
	 *
	 * @param key a key identifying this permission
	 * @return permission node for boolean type
	 */
	static PermissionNode<Boolean> of(Identifier key) {
		Objects.requireNonNull(key, "key can't be null!");

		return new PermissionNodeImpl<>(key, Codec.BOOL);
	}

	/**
	 * Creates a permission node of boolean type.
	 * Primarily used for simple permission checks (if owner can/can't do something).
	 *
	 * @param namespace namespace of the key identifying this permission node
	 * @param path path of the key identifying this permission node
	 * @return permission node for boolean type
	 */
	static PermissionNode<Boolean> of(String namespace, String path) {
		Objects.requireNonNull(namespace, "namespace can't be null!");
		Objects.requireNonNull(path, "path can't be null!");

		return of(Identifier.fromNamespaceAndPath(namespace, path));
	}

	/**
	 * Creates a permission node of integer type.
	 * Primarily used for limiting permission checks (if player can do/have X of something).
	 *
	 * @param key a key identifying this permission
	 * @return permission node for integer type
	 */
	static PermissionNode<Integer> ofInteger(Identifier key) {
		Objects.requireNonNull(key, "key can't be null!");

		return new PermissionNodeImpl<>(key, Codec.INT);
	}

	/**
	 * Creates a permission node of integer type.
	 * Primarily used for limiting permission checks (if player can do/have X of something).
	 *
	 * @param namespace namespace of the key identifying this permission node
	 * @param path path of the key identifying this permission node
	 * @return permission node for integer type
	 */
	static PermissionNode<Integer> ofInteger(String namespace, String path) {
		Objects.requireNonNull(namespace, "namespace can't be null!");
		Objects.requireNonNull(path, "path can't be null!");

		return ofInteger(Identifier.fromNamespaceAndPath(namespace, path));
	}

	/**
	 * Creates a permission node of string type.
	 * Primarily used for information/logical checks.
	 *
	 * @param key a key identifying this permission
	 * @return permission node for string type
	 */
	static PermissionNode<String> ofString(Identifier key) {
		Objects.requireNonNull(key, "key can't be null!");

		return new PermissionNodeImpl<>(key, Codec.STRING);
	}

	/**
	 * Creates a permission node of string type.
	 * Primarily used for information/logical checks.
	 *
	 * @param namespace namespace of the key identifying this permission node
	 * @param path path of the key identifying this permission node
	 * @return permission node for string type
	 */
	static PermissionNode<String> ofString(String namespace, String path) {
		Objects.requireNonNull(namespace, "namespace can't be null!");
		Objects.requireNonNull(path, "path can't be null!");

		return ofString(Identifier.fromNamespaceAndPath(namespace, path));
	}

	/**
	 * Creates a permission node of custom, codec defined type.
	 *
	 * @param key a key identifying this permission
	 * @param codec a codec used to read the permission value
	 * @param <T> the type of permission
	 * @return permission node for codec-defined type
	 */
	static <T> PermissionNode<T> ofCustom(Identifier key, Codec<T> codec) {
		Objects.requireNonNull(key, "key can't be null!");

		return new PermissionNodeImpl<>(key, codec);
	}

	/**
	 * Creates a permission node of custom, codec defined type.
	 *
	 * @param namespace namespace of the key identifying this permission node
	 * @param path path of the key identifying this permission node	 * @param codec a codec used to read the permission value
	 * @param <T> the type of permission
	 * @return permission node for codec-defined type
	 */
	static <T> PermissionNode<T> ofCustom(String namespace, String path, Codec<T> codec) {
		Objects.requireNonNull(namespace, "namespace can't be null!");
		Objects.requireNonNull(path, "path can't be null!");

		return ofCustom(Identifier.fromNamespaceAndPath(namespace, path), codec);
	}

	/**
	 * Returns a key that represents this permission.
	 *
	 * @return key identifying this permission.
	 */
	Identifier key();

	/**
	 * Returns a codec, which defined type of this permission.
	 *
	 * @return codec representing the type of this permission.
	 */
	Codec<T> codec();
}
