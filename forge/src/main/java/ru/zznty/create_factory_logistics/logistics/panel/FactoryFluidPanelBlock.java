package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockItem;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import ru.zznty.create_factory_logistics.FactoryBlockEntities;
import ru.zznty.create_factory_logistics.FactoryBlocks;

public class FactoryFluidPanelBlock extends FactoryPanelBlock {
    public FactoryFluidPanelBlock(Properties p_53182_) {
        super(p_53182_);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getBlockEntityClass() {
        return FactoryFluidPanelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FactoryPanelBlockEntity> getBlockEntityType() {
        return FactoryBlockEntities.FACTORY_FLUID_PANEL.get();
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;
        if (!FactoryBlocks.FACTORY_FLUID_GAUGE.isIn(stack))
            return ItemInteractionResult.SUCCESS;
        Vec3 location = hitResult.getLocation();
        if (location == null)
            return ItemInteractionResult.SUCCESS;

        if (!FactoryPanelBlockItem.isTuned(stack)) {
            AllSoundEvents.DENY.playOnServer(level, pos);
            player.displayClientMessage(CreateLang.translate("factory_panel.tune_before_placing")
                    .component(), true);
            return ItemInteractionResult.FAIL;
        }

        PanelSlot newSlot = getTargetedSlot(pos, state, location);
        withBlockEntityDo(level, pos, fpbe -> {
            if (!fpbe.addPanel(newSlot, LogisticallyLinkedBlockItem.networkFromStack(FactoryPanelBlockItem.fixCtrlCopiedStack(stack))))
                return;
            player.displayClientMessage(CreateLang.translateDirect("logistically_linked.connected"), true);
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS);
            if (player.isCreative())
                return;
            stack.shrink(1);
            if (stack.isEmpty())
                player.setItemInHand(hand, ItemStack.EMPTY);
        });
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        if (pUseContext.isSecondaryUseActive())
            return false;
        if (!FactoryBlocks.FACTORY_FLUID_GAUGE.isIn(pUseContext.getItemInHand()))
            return false;
        Vec3 location = pUseContext.getClickLocation();
        if (location == null)
            return false;

        BlockPos pos = pUseContext.getClickedPos();
        PanelSlot slot = getTargetedSlot(pos, pState, location);
        FactoryPanelBlockEntity blockEntity = getBlockEntity(pUseContext.getLevel(), pos);

        if (blockEntity == null)
            return false;
        if (blockEntity.panels.get(slot)
                .isActive())
            return false;
        return true;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        PanelSlot slot = getTargetedSlot(pos, state, context.getClickLocation());

        if (!(world instanceof ServerLevel))
            return InteractionResult.SUCCESS;

        return onBlockEntityUse(world, pos, be -> {
            FactoryPanelBehaviour behaviour = be.panels.get(slot);
            if (behaviour == null || !behaviour.isActive())
                return InteractionResult.SUCCESS;

            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), player);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled())
                return InteractionResult.SUCCESS;

            if (!be.removePanel(slot))
                return InteractionResult.SUCCESS;

            if (!player.isCreative())
                player.getInventory()
                        .placeItemBackInInventory(FactoryBlocks.FACTORY_FLUID_GAUGE.asStack());

            IWrenchable.playRemoveSound(world, pos);
            if (be.activePanels() == 0)
                world.destroyBlock(pos, false);

            return InteractionResult.SUCCESS;
        });
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
                                       FluidState fluid) {
        if (tryDestroySubPanelFirst(state, level, pos, player))
            return false;

        playerWillDestroy(level, pos, state, player);
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }

    private boolean tryDestroySubPanelFirst(BlockState state, Level level, BlockPos pos, Player player) {
        double range = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)
                .getValue() + 1;
        HitResult hitResult = player.pick(range, 1, false);
        Vec3 location = hitResult.getLocation();
        PanelSlot destroyedSlot = getTargetedSlot(pos, state, location);
        return InteractionResult.SUCCESS == onBlockEntityUse(level, pos, fpbe -> {
            if (fpbe.activePanels() < 2)
                return InteractionResult.FAIL;
            if (!fpbe.removePanel(destroyedSlot))
                return InteractionResult.FAIL;
            if (!player.isCreative())
                popResource(level, pos, FactoryBlocks.FACTORY_FLUID_GAUGE.asStack());
            return InteractionResult.SUCCESS;
        });
    }
}
