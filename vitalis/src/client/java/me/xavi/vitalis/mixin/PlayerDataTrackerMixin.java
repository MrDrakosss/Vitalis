package me.xavi.vitalis.mixin;

// ... imports ...
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerDataTrackerMixin {

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onInitDataTracker(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        DataTracker tracker = player.getDataTracker();
        // Here is your call to startTracking!
        tracker.startTracking(SurgeryData.ON_TABLE, false);
        tracker.startTracking(SurgeryData.TABLE_POS, BlockPos.ORIGIN);
        tracker.startTracking(SurgeryData.INJURIES, new NbtCompound());
    }
}