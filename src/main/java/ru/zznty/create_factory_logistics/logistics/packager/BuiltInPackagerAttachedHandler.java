package ru.zznty.create_factory_logistics.logistics.packager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryTrackerBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import ru.zznty.create_factory_logistics.logistics.ingredient.BoardIngredient;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackageBuilder;
import ru.zznty.create_factory_logistics.logistics.ingredient.capability.PackagerAttachedHandler;
import ru.zznty.create_factory_logistics.logistics.stock.IngredientInventorySummary;

import java.util.List;

@ApiStatus.Internal
public record BuiltInPackagerAttachedHandler(PackagerBlockEntity packagerBE) implements PackagerAttachedHandler {
    @Override
    public int slotCount() {
        return packagerBE.targetInventory.hasInventory() ? packagerBE.targetInventory.getInventory().getSlots() : 0;
    }

    @Override
    public BoardIngredient extract(int slot, int amount, boolean simulate) {
        if (!packagerBE.targetInventory.hasInventory()) return BoardIngredient.of();

        ItemStack extracted = packagerBE.targetInventory.getInventory().extractItem(slot, amount, simulate);

        return extracted.isEmpty() ? BoardIngredient.of() : new BoardIngredient(IngredientKey.of(extracted), extracted.getCount());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean unwrap(Level level, BlockPos pos, BlockState state, Direction side, @Nullable PackageOrderWithCrafts orderContext, ItemStack box, boolean simulate) {
        if (!PackageItem.isPackage(box))
            return false;

        if (!box.has(AllDataComponents.PACKAGE_CONTENTS)) return false;

        ItemStackHandler contents = PackageItem.getContents(box);
        List<ItemStack> items = ItemHelper.getNonEmptyStacks(contents);
        if (items.isEmpty())
            return true;

        UnpackingHandler handler = UnpackingHandler.REGISTRY.get(state);
        UnpackingHandler toUse = handler != null ? handler : UnpackingHandler.DEFAULT;
        // note: handler may modify the passed items
        return toUse.unpack(level, pos, state, side, items, orderContext, simulate);
    }

    @Override
    public PackageBuilder newPackage() {
        return new BuiltInPackageBuilder();
    }

    @Override
    public boolean hasChanges() {
        return !packagerBE.getBehaviour(VersionedInventoryTrackerBehaviour.TYPE).stillWaiting(packagerBE.targetInventory);
    }

    @Override
    public void collectAvailable(boolean scanInputSlots, IngredientInventorySummary summary) {
        if (!packagerBE.targetInventory.hasInventory() || packagerBE.targetInventory.getInventory() instanceof PackagerItemHandler)
            return;

        IItemHandler targetInv = packagerBE.targetInventory.getInventory();

        if (targetInv instanceof BottomlessItemHandler bih) {
            summary.add(new BoardIngredient(IngredientKey.of(bih.getStackInSlot(0)), BigItemStack.INF));
            return;
        }

        for (int slot = 0; slot < targetInv.getSlots(); slot++) {
            int slotLimit = targetInv.getSlotLimit(slot);
            ItemStack stack = scanInputSlots ? targetInv.getStackInSlot(slot) : targetInv.extractItem(slot, slotLimit, true);
            summary.add(new BoardIngredient(IngredientKey.of(stack), stack.getCount()));
        }

        packagerBE.getBehaviour(VersionedInventoryTrackerBehaviour.TYPE).awaitNewVersion(packagerBE.targetInventory);
    }

    @Override
    public Block supportedGauge() {
        return AllBlocks.FACTORY_GAUGE.get();
    }

    @Override
    public IdentifiedInventory identifiedInventory() {
        if (!packagerBE.targetInventory.hasInventory())
            return null;
        return new IdentifiedInventory(InventoryIdentifier.get(packagerBE.targetInventory.getWorld(), packagerBE.targetInventory.getTarget().getOpposite()), packagerBE.targetInventory.getInventory());
    }
}
