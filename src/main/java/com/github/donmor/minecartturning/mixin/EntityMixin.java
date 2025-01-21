package com.github.donmor.minecartturning.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import com.github.donmor.minecartturning.MinecartTurningMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Unique
	private float ridingEntityYawDelta;

	@Unique
	private float ridingEntityPitchDelta;

	@Shadow
	public abstract float getYRot();

	@Shadow
	public abstract void setYRot(float arg0);

	@Shadow
	public abstract float getXRot();

	@Shadow
	public abstract void setXRot(float arg0);

	@Shadow
	public abstract Entity getVehicle();

	@Inject(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;positionRider(Lnet/minecraft/world/entity/Entity;)V", shift = At.Shift.AFTER))
	private void modifyYawAndPitch(CallbackInfo ci) {
		if (!MinecartTurningMod.checkModEnabled())
			return;

		// intellij really doesn't like the "this" check because the nominally disparate class hierarchies at compile-time
		// also Entity#getVehicle is not null at this point in the control flow
		// noinspection ConstantValue
		if (this.getVehicle().getControllingPassenger() == (Object) this) {
			// if this is the controlling passenger, it's already setting the movements of the vehicle
			// for boats, pigs, etc.
			return;
		}

		if (this.getVehicle() instanceof LivingEntity livingVehicle) {
			// if just Entity#getYRot is used, nothing happens to the player camera when an animal turns at standstill
			// body instead of head to match 1.2 behaviour
			this.ridingEntityYawDelta = this.ridingEntityYawDelta + this.getVehicle().getVisualRotationYInDegrees() - livingVehicle.yBodyRotO;
		} else {
			// from here onwards is just taken from 1.2.5, with minor edits to use getters and such
			this.ridingEntityYawDelta = this.ridingEntityYawDelta + this.getVehicle().getYRot() - this.getVehicle().yRotO;
		}
		this.ridingEntityPitchDelta = this.ridingEntityPitchDelta + this.getVehicle().getXRot() - this.getVehicle().xRotO;

		while (this.ridingEntityYawDelta >= 180.0)
			this.ridingEntityYawDelta -= 360.0F;

		while (this.ridingEntityYawDelta < -180.0)
			this.ridingEntityYawDelta += 360.0F;

		while (this.ridingEntityPitchDelta >= 180.0)
			this.ridingEntityPitchDelta -= 360.0F;

		while (this.ridingEntityPitchDelta < -180.0)
			this.ridingEntityPitchDelta += 360.0F;

		var ridingEntityYawDeltaSmooth = this.ridingEntityYawDelta * 0.5F;
		var ridingEntityPitchDeltaSmooth = this.ridingEntityPitchDelta * 0.5F;

		var maxTurn = 10F;
		if (ridingEntityYawDeltaSmooth > maxTurn)
			ridingEntityYawDeltaSmooth = maxTurn;

		if (ridingEntityYawDeltaSmooth < -maxTurn)
			ridingEntityYawDeltaSmooth = -maxTurn;

		if (ridingEntityPitchDeltaSmooth > maxTurn)
			ridingEntityPitchDeltaSmooth = maxTurn;

		if (ridingEntityPitchDeltaSmooth < -maxTurn)
			ridingEntityPitchDeltaSmooth = -maxTurn;

		this.ridingEntityYawDelta -= ridingEntityYawDeltaSmooth;
		this.ridingEntityPitchDelta -= ridingEntityPitchDeltaSmooth;
		this.setYRot(this.getYRot() + ridingEntityYawDeltaSmooth);
		this.setXRot(this.getXRot() + ridingEntityPitchDeltaSmooth);
	}

	@Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("HEAD"))
	private void resetPitchAndDelta(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		this.ridingEntityPitchDelta = 0.0F;
		this.ridingEntityYawDelta = 0.0F;
	}
}