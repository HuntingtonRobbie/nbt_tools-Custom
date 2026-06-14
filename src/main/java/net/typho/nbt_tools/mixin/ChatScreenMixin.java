package net.typho.nbt_tools.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.typho.nbt_tools.EditBoxExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow
    protected EditBox input;

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void init(CallbackInfo ci) {
        if (input instanceof EditBoxExtension ext) {
            ext.setNbt_tools$isChat(true);
        }
    }
}
