package com.ruinscraft.chip;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.ChatColor;

public class LoreUtil {

	// https://www.spigotmc.org/threads/how-to-hide-item-lore-how-to-bind-data-to-itemstack.196008/
	public static String encodeStringForLore(String msg) {
		StringBuilder output = new StringBuilder();

		byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
		String hex = Hex.encodeHexString(bytes);

		for (char c : hex.toCharArray()) {
			output.append(ChatColor.COLOR_CHAR).append(c);
		}

		return output.toString();
	}

	// https://www.spigotmc.org/threads/how-to-hide-item-lore-how-to-bind-data-to-itemstack.196008/
	public static String decodeStringForLore(String msg) {
		if (msg.isEmpty()) {
			return msg;
		}

		char[] chars = msg.toCharArray();

		char[] hexChars = new char[chars.length / 2];

		IntStream.range(0, chars.length)
		.filter(value -> value % 2 != 0)
		.forEach(value -> hexChars[value / 2] = chars[value]);

		try {
			return new String(Hex.decodeHex(hexChars), StandardCharsets.UTF_8);
		} catch (DecoderException e) {}
		
		return null;
	}
	
}
