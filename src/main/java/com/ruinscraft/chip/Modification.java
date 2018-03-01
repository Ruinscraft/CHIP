package com.ruinscraft.chip;

public enum Modification {
	
	ENTITY_INVULNERABLE("invulnerable"),
	ENTITY_GLOWING("glowing"),
	ENTITY_CUSTOM_NAME_TOO_LONG("name too long"),
	ENTITY_CUSTOM_NAME_VISIBLE("name visible"),
	ENTITY_ARMOR_STAND_SMALL("armor stand small"),
	ENTITY_ARMOR_STAND_VISIBLE("armor stand visible"),
	ENTITY_ARMOR_STAND_BASE_PLATE("armor stand base plate"),
	
	ITEMSTACK_ENCHANTMENT_TOO_HIGH("enchant level too high"),
	ITEMSTACK_ENCHANTMENT_TOO_LOW("enchant level too low"),
	ITEMSTACK_ENCHANTMENT_NOT_COMPATIBLE("incompatible enchants"),
	ITEMSTACK_FIREWORK_NOT_CRAFTABLE("firework not craftable"),
	ITEMSTACK_META_CUSTOM_LORE("custom lore"),
	ITEMSTACK_META_UNBREAKABLE("unbreakable"),
	ITEMSTACK_META_COLORED_NAME("colored name"),
	ITEMSTACK_META_COLORED_LORE("colored lore"),
	ITEMSTACK_META_NAME_TOO_LONG("name too long"),
	ITEMSTACK_META_LORE_TOO_LONG("lore too long"),
	ITEMSTACK_POTION_CUSTOM("custom potion");
	
	private String pretty;
	
	private Modification(String pretty) {
		this.pretty = pretty;
	}
	
	public String getPretty() {
		return pretty;
	}
	
}
