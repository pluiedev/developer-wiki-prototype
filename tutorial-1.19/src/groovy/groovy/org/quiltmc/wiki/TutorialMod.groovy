package org.quiltmc.wiki

import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.item.Item
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings

// @start Declaration
final Item EXAMPLE_ITEM = new Item(new QuiltItemSettings())
// @end Declaration

// @start Registration
// arg0 is the ModContainer
Registries.ITEM[new Identifier(arg0.metadata().id(), 'example_item')] = EXAMPLE_ITEM
// @end Registration
