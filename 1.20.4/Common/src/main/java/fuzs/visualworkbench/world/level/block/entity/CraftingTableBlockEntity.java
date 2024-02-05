package fuzs.visualworkbench.world.level.block.entity;

import fuzs.puzzleslib.api.block.v1.entity.TickingBlockEntity;
import fuzs.puzzleslib.api.container.v1.ContainerSerializationHelper;
import fuzs.visualworkbench.VisualWorkbench;
import fuzs.visualworkbench.init.ModRegistry;
import fuzs.visualworkbench.world.inventory.VisualCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CraftingTableBlockEntity extends RandomizableContainerBlockEntity implements TickingBlockEntity, WorkbenchVisualsProvider {
    public static final MutableComponent COMPONENT_CRAFTING = Component.translatable("container.crafting");
    public static final String TAG_RESULT = VisualWorkbench.id("result").toString();

    private final CraftingTableAnimationController animationController;
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final NonNullList<ItemStack> resultItems = NonNullList.withSize(1, ItemStack.EMPTY);

    public CraftingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistry.CRAFTING_TABLE_BLOCK_ENTITY.value(), pos, blockState);
        this.animationController = new CraftingTableAnimationController(pos);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items.clear();
        this.resultItems.clear();
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
            ContainerSerializationHelper.loadAllItems(TAG_RESULT, tag, this.resultItems);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
            ContainerSerializationHelper.saveAllItems(TAG_RESULT, tag, this.resultItems);
        }
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemStacks) {
        // why does this event exist...
        throw new UnsupportedOperationException();
    }

    @Override
    protected Component getDefaultName() {
        return COMPONENT_CRAFTING;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new VisualCraftingMenu(containerId, inventory, this, ContainerLevelAccess.create(this.getLevel(), this.getBlockPos()));
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clientTick() {
        this.animationController.tick(this.getLevel());
    }

    public NonNullList<ItemStack> getResultItems() {
        return this.resultItems;
    }

    @Override
    public ItemStack getCraftingResult() {
        return this.resultItems.get(0);
    }

    @Override
    public CraftingTableAnimationController getAnimationController() {
        return this.animationController;
    }
}
