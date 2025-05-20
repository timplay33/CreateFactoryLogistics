package ru.zznty.create_factory_logistics.logistics.ingredient.impl;

import org.jetbrains.annotations.ApiStatus;
import ru.zznty.create_factory_logistics.logistics.ingredient.CapabilityFactory;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKey;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeyProvider;
import ru.zznty.create_factory_logistics.logistics.ingredient.IngredientKeySerializer;

@ApiStatus.Internal
public class EmptyIngredientProvider implements IngredientKeyProvider {
    private final EmptyKeySerializer serializer = new EmptyKeySerializer();

    @Override
    public <K extends IngredientKey> K defaultKey() {
        //noinspection unchecked
        return (K) IngredientKey.of();
    }

    @Override
    public <T, K extends IngredientKey<T>> K wrap(T value) {
        //noinspection unchecked
        return (K) IngredientKey.EMPTY;
    }

    @Override
    public <K extends IngredientKey> IngredientKeySerializer<K> serializer() {
        //noinspection unchecked
        return (IngredientKeySerializer<K>) serializer;
    }

    @Override
    public <K extends IngredientKey> int compare(K a, K b) {
        return 0;
    }

    @Override
    public <T> CapabilityFactory<T> capabilityFactory() {
        return null;
    }

    @Override
    public String ingredientTypeUid() {
        return "empty";
    }
}
