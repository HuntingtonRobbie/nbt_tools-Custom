package net.typho.nbt_tools.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract DataComponentMap getComponents();

    @Shadow
    public abstract Item getItem();

    @ModifyArg(
            //? if <1.21.11 {
            /*method = "getTooltipLines",
            *///? } else {
            method = "addDetailsToTooltip",
            //? }
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private ChatFormatting getTooltipLines(ChatFormatting format) {
        return format == ChatFormatting.DARK_GRAY ? ChatFormatting.GRAY : format;
    }

    @SuppressWarnings("LocalMayBeArgsOnly")
    @Inject(
            //? if <1.21.11 {
            /*method = "getTooltipLines",
            *///? } else {
            method = "addDetailsToTooltip",
            //? }
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;enabledFeatures()Lnet/minecraft/world/flag/FeatureFlagSet;"
            )
    )
    private void getTooltipLines(
            CallbackInfo ci,
            @Local(argsOnly = true) Item.TooltipContext context,
            @Local(argsOnly = true) TooltipFlag flag,
            @Local Consumer<Component> out
    ) {
        if (flag.isAdvanced()) {
            DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
            HolderLookup.Provider registries = context.registries();

            if (registries != null) {
                ops = RegistryOps.create(JsonOps.INSTANCE, registries);
            }

            for (TypedDataComponent<?> component : getComponents()) {
                if (component.type() == DataComponents.CONTAINER && getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock) {
                    continue;
                }

                component.encodeValue(ops).ifSuccess(json -> {
                    String text = " " + BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type()) + " - " + json.toString();
                    List<String> lines = new ArrayList<>();

                    while (text.length() > 50) {
                        int index = lines.isEmpty() ? 50 : 40;

                        while (index > 0 && !Character.isWhitespace(text.charAt(index))) {
                            index--;
                        }

                        if (index == 0) {
                            index = lines.isEmpty() ? 50 : 40;
                        }

                        if (lines.isEmpty()) {
                            lines.add(text.substring(0, index));
                        } else {
                            lines.add(" ".repeat(10) + text.substring(0, index).trim());
                        }

                        text = text.substring(index).trim();
                    }

                    lines.add(lines.isEmpty() ? text : (" ".repeat(10) + text));

                    for (String line : lines) {
                        out.accept(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY));
                    }
                });
            }
        }
    }
}
