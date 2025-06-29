package ru.zznty.create_factory_abstractions.api.generic.stack;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.zznty.create_factory_abstractions.CreateFactoryAbstractions;
import ru.zznty.create_factory_abstractions.api.generic.GenericFilterProvider;
import ru.zznty.create_factory_abstractions.api.generic.key.GenericKey;
import ru.zznty.create_factory_abstractions.generic.key.item.ItemKey;

public record GenericStack(GenericKey key, int amount) {
    public static final GenericStack EMPTY = new GenericStack(GenericKey.EMPTY, 0);

    public boolean isEmpty() {
        return amount == 0 || key == GenericKey.EMPTY;
    }

    public GenericStack withAmount(int amount) {
        return new GenericStack(key, amount);
    }

    public boolean canStack(GenericStack ingredient) {
        return key.equals(ingredient.key);
    }

    public boolean canStack(GenericKey otherKey) {
        return key.equals(otherKey);
    }

    public static GenericStack wrap(ItemStack stack) {
        if (stack.getItem() == Items.AIR) return EMPTY;
        return new GenericStack(new ItemKey(stack.copyWithCount(1)), stack.getCount());
    }

    public static GenericStack of(FactoryPanelBehaviour behaviour) {
        if (!CreateFactoryAbstractions.EXTENSIBILITY_AVAILABLE)
            return GenericStack.wrap(behaviour.getFilter())
                    .withAmount(behaviour.count * (behaviour.upTo ? 1 : behaviour.getFilter().getMaxStackSize()));
        GenericFilterProvider filterProvider = (GenericFilterProvider) behaviour;
        return filterProvider.filter();
    }
}
