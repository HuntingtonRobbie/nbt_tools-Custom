package net.typho.nbt_tools.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

//? if >=1.21.11 {
import net.minecraft.client.input.KeyEvent;
//? }

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(
            method = "keyPress",
            at = @At("HEAD")
    )
    private void keyPress(
            long window,
            //? if <1.21.11 {
            /*int key,
            int scanCode,
            int action,
            int modifiers,
            *///? } else {
            int action,
            KeyEvent event,
            //? }
            CallbackInfo ci
    ) {
        //? if <1.21.11 {
        /*if (key == GLFW_KEY_C && (modifiers & GLFW_MOD_CONTROL) != 0 && action == GLFW_PRESS) {
        *///? } else {
        if (event.key() == GLFW_KEY_C && event.hasControlDown() && action == GLFW_PRESS) {
        //? }
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreenAccessor accessor) {
                ItemStack copyStack;

                if (!accessor.nbt_tools$getDraggingItem().isEmpty()) {
                    copyStack = accessor.nbt_tools$getDraggingItem();
                } else if (accessor.nbt_tools$getHoveredSlot() != null) {
                    //noinspection DataFlowIssue
                    copyStack = accessor.nbt_tools$getHoveredSlot().getItem();
                } else {
                    copyStack = null;
                }

                if (copyStack != null && !copyStack.isEmpty()) {
                    try {
                        Identifier id = BuiltInRegistries.ITEM.getKey(copyStack.getItem());
                        StringBuilder command = new StringBuilder("/give @s ");

                        command.append(id);

                        List<TypedDataComponent<?>> components = new LinkedList<>();

                        DataComponentMap comps = copyStack.getComponents();
                        DataComponentMap def = copyStack.getPrototype();

                        for (TypedDataComponent<?> next : comps) {
                            if (def.has(next.type()) && def.get(next.type()) == next.value()) {
                                continue;
                            }

                            components.add(next);
                        }

                        if (!components.isEmpty()) {
                            DynamicOps<JsonElement> ops;

                            if (Minecraft.getInstance().player == null) {
                                ops = JsonOps.INSTANCE;
                            } else {
                                ops = RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().player.registryAccess());
                            }

                            command.append('[');
                            command.append(String.join(
                                    ", ",
                                    components.stream()
                                            .map(comp -> BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(comp.type()) + "=" + comp.encodeValue(ops).getOrThrow().toString())
                                            .toArray(String[]::new)
                            ));

                            command.append(']');
                        }

                        //? if <1.21.11 {
                        /*GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().getWindow(), command);
                         *///? } else {
                        GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().handle(), command);
                        //? }

                        Minecraft.getInstance().getChatListener().handleSystemMessage(
                                Component.translatable(
                                        "nbt_tools.copied",
                                        id.toString()
                                ),
                                false
                        );
                    } catch (RuntimeException e) {
                        e.printStackTrace(System.err);

                        Minecraft.getInstance().getChatListener().handleSystemMessage(
                                Component.translatable(
                                        "nbt_tools.failed",
                                        e.getMessage()
                                ).withStyle(ChatFormatting.RED),
                                false
                        );
                    }
                }
            }
        }
    }
}
