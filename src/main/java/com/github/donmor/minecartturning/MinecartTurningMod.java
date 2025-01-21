package com.github.donmor.minecartturning;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(MinecartTurningMod.MOD_ID)
public final class MinecartTurningMod {
	public static final String MOD_ID = "minecart_turning";
	private static boolean modEnabled = true;

	public static boolean checkModEnabled() {
		return modEnabled;
	}

	public MinecartTurningMod() {
	}

	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
	public static class ModEventBusClientEvents {
		public static final Lazy<KeyMapping> KM_TOGGLE_LAZY = Lazy.of(() -> new KeyMapping("minecart-turning.toggle", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "minecart-turning.category"));

		@SubscribeEvent
		public static void registerBindings(final RegisterKeyMappingsEvent event) {
			event.register(KM_TOGGLE_LAZY.get());
		}

	}
	@Mod.EventBusSubscriber(modid = MOD_ID, bus = Bus.FORGE, value = Dist.CLIENT)
	public static class ModEventBusClientEvents2 {
		@SubscribeEvent
		public static void onPlayerTick(final PlayerTickEvent event) {
			if (event.side == LogicalSide.CLIENT) {
				while (ModEventBusClientEvents.KM_TOGGLE_LAZY.get().consumeClick()) {
					modEnabled = !modEnabled;
					event.player.displayClientMessage(Component.translatable(modEnabled?"minecart-turning.activate":"minecart-turning.deactivate"), true);
				}
			}
		}
	}
}
