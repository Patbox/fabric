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

package net.fabricmc.fabric.test.registry.sync;

import static net.minecraft.commands.Commands.literal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.network.chat.Component;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.registry.sync.validate.RegistryCustomContentState;

public class RegistryEntryWorldValidationTestInit implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(((dispatcher, buildContext, selection) -> {
			dispatcher.register(literal("override_registry_entries").executes(ctx -> {
				Path path = RegistryCustomContentState.getPath(ctx.getSource().getServer());

				if (!Files.isDirectory(path.getParent())) {
					try {
						Files.createDirectories(path.getParent());
					} catch (IOException e) {
						LOGGER.error("Failed to create folders!", e);
					}
				}

				InputStream stream = Objects.requireNonNull(RegistryEntryWorldValidationTestInit.class.getResourceAsStream("/world_validation/reference_registry_entries.dat"));

				try (OutputStream output = Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
					stream.transferTo(output);
					ctx.getSource().sendSystemMessage(Component.literal("Copied the reference registry entry file! Leave and entry this world again to test it!"));
				} catch (IOException e) {
					LOGGER.error("Failed to copy the reference registry entries file!", e);
					ctx.getSource().sendFailure(Component.literal("Failed to copy the reference registry entries file! See logs for more info"));
				}

				return 1;
			}));
		}));
	}
}
