package ru.zznty.create_factory_logistics.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRenderer;
import dev.engine_room.flywheel.lib.transform.Translate;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.zznty.create_factory_logistics.logistics.jar.JarItemRenderer;
import ru.zznty.create_factory_logistics.logistics.jar.JarPackageItem;

@Mixin(ChainConveyorRenderer.class)
public class ChainConveyorRendererMixin {
    @WrapOperation(
            method = "renderBox",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;renderInto(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
            )
    )
    private void renderFluid(SuperByteBuffer instance, PoseStack ms, VertexConsumer vertexConsumer, Operation<Void> original,
                             @Local(argsOnly = true) MultiBufferSource buffer,
                             @Local(argsOnly = true) ChainConveyorPackage box,
                             @Local(ordinal = 1) SuperByteBuffer boxBuffer,
                             @Local(ordinal = 1) int light) {
        if (boxBuffer == instance && box.item.getItem() instanceof JarPackageItem) {
            ms.pushPose();

            ms.mulPose(instance.getTransforms().last().pose());

            ms.translate(Translate.CENTER, Translate.CENTER, Translate.CENTER);

            JarItemRenderer.renderFluidContents(box.item, -1, ms, buffer, light);

            ms.popPose();

        }

        original.call(instance, ms, vertexConsumer);
    }
}
