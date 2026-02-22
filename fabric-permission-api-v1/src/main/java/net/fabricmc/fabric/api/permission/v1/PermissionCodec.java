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

import net.minecraft.util.TriState;

/**
 * Set of <i>suggested</i> codecs used for permission checks.
 * While custom codecs are also allowed, using one below (or other minimal codecs) is encouraged.
 */
public interface PermissionCodec {
	/**
	 * TriState codec, can be used for simple boolean / ability checks.
	 */
	Codec<TriState> TRI_STATE = TriState.CODEC;

	/**
	 * Integer codec, can be used for limit checks.
	 */
	Codec<Integer> INT = Codec.INT;

	/**
	 * String codec, can be used for display.
	 */
	Codec<String> STRING = Codec.STRING;
}
