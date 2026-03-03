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

package net.fabricmc.fabric.test.permission;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.permission.v1.PermissionCheckCallback;
import net.fabricmc.fabric.api.permission.v1.PermissionCodecs;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.fabricmc.fabric.test.permission.example.PermissionMap;

public class PermissionTestMod implements ModInitializer, PermissionCheckCallback {
	private static final Identifier ON_STONE = Identifier.fromNamespaceAndPath("test", "on_stone");
	private static final Identifier IS_ENTITY = Identifier.fromNamespaceAndPath("test", "is_entity");
	private static final Identifier ABOVE_SEA = Identifier.fromNamespaceAndPath("test", "above_sea");
	private static final Identifier MAGIC = Identifier.fromNamespaceAndPath("test", "magic");

	private final PermissionMap globalPermissionMap = new PermissionMap();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(this::registerCommands);
		PermissionCheckCallback.register(this);

		try {
			this.runBasicTest();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private void runBasicTest() throws Throwable {
		int value = RandomSource.createThreadLocalInstance().nextInt();

		this.globalPermissionMap.set(MAGIC, value);

		PermissionContext context = PermissionContext.create(UUID.randomUUID(), PermissionContext.Type.OTHER, PermissionLevel.ADMINS);

		int valueMainCheck = context.checkPermission(MAGIC, PermissionCodecs.INT, value + 1);
		int valueAsyncCheck = context.checkPermissionAsync(MAGIC, PermissionCodecs.INT, value - 1).get(5, TimeUnit.SECONDS);

		if (valueMainCheck != value) {
			throw new IllegalStateException("Permission check failed! valueMainCheck != value");
		}

		if (valueMainCheck != valueAsyncCheck) {
			throw new IllegalStateException("Permission check failed! valueMainCheck != valueAsyncCheck");
		}
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
		dispatcher.register(literal("permissions")
				.then(
						literal("set").then(argument("permission", IdentifierArgument.id()).then(argument("value", NbtTagArgument.nbtTag()).executes(this::setPermissionValue)))
				)
				.then(
						literal("get").then(argument("permission", IdentifierArgument.id()).executes(this::getPermissionValue))
				)
				.then(
						literal("check_bool").then(argument("permission", IdentifierArgument.id()).executes(this::checkPermissionValue))
				)
				.then(
						literal("on_stone_command").requires(PermissionPredicates.require(ON_STONE, false)).executes(this::onStoneCommand)
				)
		);
	}

	private int setPermissionValue(CommandContext<CommandSourceStack> context) {
		Identifier id = IdentifierArgument.getId(context, "permission");
		Tag value = NbtTagArgument.getNbtTag(context, "value");

		if (context.getSource().getPlayer() instanceof ServerPlayer player) {
			context.getSource().getServer().getPlayerList().sendPlayerPermissionLevel(player);
		}

		this.globalPermissionMap.set(id, value);
		return 1;
	}

	private int getPermissionValue(CommandContext<CommandSourceStack> context) {
		Identifier id = IdentifierArgument.getId(context, "permission");
		Tag value = this.globalPermissionMap.getRaw(id);

		context.getSource().sendSystemMessage(value != null ? new TextComponentTagVisitor("", TextComponentTagVisitor.RichStyling.INSTANCE).visit(value) : Component.literal("<null>"));
		return 1;
	}

	private int checkPermissionValue(CommandContext<CommandSourceStack> context) {
		Identifier id = IdentifierArgument.getId(context, "permission");

		context.getSource().sendSystemMessage(Component.literal(context.getSource().checkPermission(id).getSerializedName()));
		return 1;
	}

	private int onStoneCommand(CommandContext<CommandSourceStack> context) {
		context.getSource().sendSystemMessage(Component.literal("You got the stone permission"));
		return 1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <T> T onPermissionCheck(PermissionContext context, Identifier permission, Codec<T> permissionType) {
		Level level = context.get(PermissionContext.LEVEL);
		BlockPos blockPos = context.get(PermissionContext.BLOCK_POSITION);
		Entity entity = context.get(PermissionContext.ENTITY);

		if (permissionType == PermissionCodecs.TRI_STATE) {
			if (permission.equals(ON_STONE) && level != null && blockPos != null) {
				return (T) TriState.from(level.getBlockState(blockPos.below()).is(Blocks.STONE));
			}

			if (permission.equals(IS_ENTITY)) {
				return (T) TriState.from(entity != null);
			}

			if (permission.equals(ABOVE_SEA) && blockPos != null && level != null) {
				return (T) TriState.from(level.getSeaLevel() < blockPos.getY());
			}
		}

		return this.globalPermissionMap.get(permission, permissionType);
	}
}
