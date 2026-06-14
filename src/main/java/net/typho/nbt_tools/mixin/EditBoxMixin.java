package net.typho.nbt_tools.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.EditBox;
import net.typho.nbt_tools.EditBoxExtension;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBox.class)
public class EditBoxMixin implements EditBoxExtension {
    @Shadow
    private String value;
    @Unique
    private boolean nbt_tools$isChat = false;

    @Override
    public boolean getNbt_tools$isChat() {
        return nbt_tools$isChat;
    }

    @Override
    public void setNbt_tools$isChat(boolean b) {
        nbt_tools$isChat = b;
    }

    @ModifyExpressionValue(
            method = {
                    "insertText",
                    "setValue"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/components/EditBox;maxLength:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int insertText(int maxLength, @Local(argsOnly = true) String text) {
        return adjustMaxLength(maxLength, text);
    }

    @WrapOperation(
            //? if <26.1 {
            /*method = "renderWidget",
            *///? } else {
            method = "extractWidgetRenderState",
            //? }
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/EditBox;getMaxLength()I"
            )
    )
    private int renderWidget(EditBox instance, Operation<Integer> original) {
        return adjustMaxLength(original.call(instance), value);
    }
}
