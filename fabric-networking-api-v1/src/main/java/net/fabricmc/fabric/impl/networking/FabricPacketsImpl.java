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

package net.fabricmc.fabric.impl.networking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.splitter.FabricSplitDataPacketPayload;
import net.fabricmc.fabric.impl.networking.splitter.FabricSplitEndPacketPayload;
import net.fabricmc.fabric.impl.networking.splitter.FabricSplitStartPacketPayload;

public final class FabricPacketsImpl {
	public static final String MOD_ID = "fabric-networking-api-v1";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static void init() {
		PayloadTypeRegistry.configurationS2C().register(FabricSplitStartPacketPayload.ID, FabricSplitStartPacketPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(FabricSplitStartPacketPayload.ID, FabricSplitStartPacketPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(FabricSplitEndPacketPayload.ID, FabricSplitEndPacketPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(FabricSplitEndPacketPayload.ID, FabricSplitEndPacketPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(FabricSplitDataPacketPayload.ID, FabricSplitDataPacketPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(FabricSplitDataPacketPayload.ID, FabricSplitDataPacketPayload.CODEC);
	}
}
