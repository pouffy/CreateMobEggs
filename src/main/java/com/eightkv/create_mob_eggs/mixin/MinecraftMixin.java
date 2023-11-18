package com.eightkv.create_mob_eggs.mixin;

import com.eightkv.create_mob_eggs.ExampleMod;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;

import net.minecraft.world.effect.MobEffects;

import net.minecraft.world.entity.MobType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@Mixin(CrushingWheelControllerBlockEntity.class)
public class MinecraftMixin {

	@Shadow(remap = false)
	public Entity processingEntity;
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPos(DDD)V", shift = At.Shift.AFTER))
	private void injected(CallbackInfo ci) {

		if(!processingEntity.isAlive() && processingEntity instanceof LivingEntity){
			boolean hasRegeneration = ((LivingEntity) processingEntity).hasEffect(MobEffects.REGENERATION);
			boolean hasWeakness = ((LivingEntity) processingEntity).hasEffect(MobEffects.WEAKNESS);
			boolean hasEffect = ((LivingEntity) processingEntity).getMobType() == MobType.UNDEAD ? hasWeakness : hasRegeneration;

			if(hasEffect){
				var self = (CrushingWheelControllerBlockEntity)(Object)this;
				Vec3 centerPos = VecHelper.getCenterOf(self.getBlockPos());
				Direction facing = self.getBlockState().getValue(CrushingWheelControllerBlock.FACING);
				int offset = facing.getAxisDirection().getStep();
				String entityNamespace = EntityType.getKey(processingEntity.getType()).getNamespace();
				String entityId = EntityType.getKey(processingEntity.getType()).toString().replace(entityNamespace + ":", "");
				ResourceLocation eggLocation = new ResourceLocation(entityNamespace, entityId + "_spawn_egg");
				Vec3 outSpeed = new Vec3((facing.getAxis() == Direction.Axis.X ? 0.25D : 0.0D) * offset, offset == 1 ? (facing.getAxis() == Direction.Axis.Y ? 0.5D : 0.0D) : 0.0D, (facing.getAxis() == Direction.Axis.Z ? 0.25D : 0.0D) * offset);
				Vec3 outPos = centerPos.add((facing.getAxis() == Direction.Axis.X ? .55f * offset : 0f), (facing.getAxis() == Direction.Axis.Y ? .55f * offset : 0f), (facing.getAxis() == Direction.Axis.Z ? .55f * offset : 0f));
				ItemStack eggStack = Registry.ITEM.get(eggLocation).getDefaultInstance();
				ItemEntity entitySpawnEgg = new ItemEntity(processingEntity.level, outPos.x, outPos.y, outPos.z, eggStack);
				entitySpawnEgg.setDeltaMovement(outSpeed);
				entitySpawnEgg.getExtraCustomData().put("BypassCrushingWheel", NbtUtils.writeBlockPos(self.getBlockPos()));
				processingEntity.level.addFreshEntity(entitySpawnEgg);
			}

		}
	}
}
