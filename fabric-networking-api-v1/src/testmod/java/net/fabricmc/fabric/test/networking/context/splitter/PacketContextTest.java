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

package net.fabricmc.fabric.test.networking.context.splitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class PacketContextTest implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(PacketContextTest.class);

	public static final PacketContext.Key<String> STRING_KEY = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "string_key"));
	public static final String STRING_VALUE = "Hello World!";

	@Override
	public void onInitialize() {
		// Store some example value when logging in.
		ServerLoginConnectionEvents.INIT.register((listener, server) -> {
			listener.getPacketContext().setValue(STRING_KEY, STRING_VALUE);
		});
		// Write it in play.
		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
			String stringValue = listener.getPacketContext().orElseThrow(STRING_KEY);

			listener.player.sendSystemMessage(Component.literal("PacketContext: " + stringValue));
		});
	}
}
