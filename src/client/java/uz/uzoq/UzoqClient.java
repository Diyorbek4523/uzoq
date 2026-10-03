package uz.uzoq;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

/**
 * R - uzoqroqdan urish (5.5 blok).
 * B - yerda tez yurish, N - tezlikni almashtirish.
 *
 * Server o'yinchidan 6 blokdan (3 + 3) uzoqdagi zarbalarni qabul qilmaydi,
 * shuning uchun urish masofasi biroz kamroq — 5.5 blok — olingan.
 */
public class UzoqClient implements ClientModInitializer {
	/** O'yindagi standart urish masofasi (blok). */
	private static final double BASE_REACH = 3.0;
	private static final double LONG_REACH = 5.5;

	/** O'yindagi standart yurish tezligi. */
	private static final double BASE_SPEED = 0.10000000149011612;
	private static final double[] SPEEDS = {1.5, 2.0, 3.0};

	private static KeyMapping reachKey;
	private static KeyMapping speedToggleKey;
	private static KeyMapping speedLevelKey;

	private static boolean reachEnabled = false;
	private static boolean speedEnabled = false;
	private static int speedIndex = 1;

	@Override
	public void onInitializeClient() {
		reachKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.uzoq.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_R, "key.categories.uzoq"));
		speedToggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.uzoq.speed", InputConstants.Type.KEYSYM, InputConstants.KEY_B, "key.categories.uzoq"));
		speedLevelKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.uzoq.speedlevel", InputConstants.Type.KEYSYM, InputConstants.KEY_N, "key.categories.uzoq"));

		ClientTickEvents.END_CLIENT_TICK.register(UzoqClient::onTick);
	}

	private static void onTick(Minecraft client) {
		while (reachKey.consumeClick()) {
			reachEnabled = !reachEnabled;
			message(client, reachEnabled ? "§aUzoqdan urish YONIQ §7(" + LONG_REACH + " blok)" : "§cUzoqdan urish O'CHIQ");
		}

		while (speedToggleKey.consumeClick()) {
			speedEnabled = !speedEnabled;
			message(client, speedEnabled ? "§aTez yurish YONIQ §7(" + SPEEDS[speedIndex] + "x)" : "§cTez yurish O'CHIQ");
		}

		while (speedLevelKey.consumeClick()) {
			speedIndex = (speedIndex + 1) % SPEEDS.length;
			message(client, "§eYurish tezligi: " + SPEEDS[speedIndex] + "x");
		}

		LocalPlayer player = client.player;

		if (player == null) {
			return;
		}

		// Server o'lim yoki dunyo almashganda qiymatlarni qaytarishi mumkin, shuning uchun har tikda tekshiramiz.
		setBase(player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), reachEnabled ? LONG_REACH : BASE_REACH);
		setBase(player.getAttribute(Attributes.MOVEMENT_SPEED), speedEnabled ? BASE_SPEED * SPEEDS[speedIndex] : BASE_SPEED);
	}

	private static void setBase(AttributeInstance attribute, double value) {
		if (attribute != null && attribute.getBaseValue() != value) {
			attribute.setBaseValue(value);
		}
	}

	private static void message(Minecraft client, String text) {
		if (client.player != null) {
			client.player.displayClientMessage(Component.literal(text), true);
		}
	}
}
