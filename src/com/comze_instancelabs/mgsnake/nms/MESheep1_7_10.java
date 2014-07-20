package com.comze_instancelabs.mgsnake.nms;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.server.v1_7_R4.DamageSource;
import net.minecraft.server.v1_7_R4.EntityComplexPart;
import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.EntityMonster;
import net.minecraft.server.v1_7_R4.EntitySheep;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.PathfinderGoal;
import net.minecraft.server.v1_7_R4.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_7_R4.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R4.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_7_R4.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_7_R4.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.comze_instancelabs.mgsnake.Main;

public class MESheep1_7_10 extends EntitySheep {

	private boolean onGround = false;
	private Main m;
	private String arena;
	
	public MESheep1_7_10(Main m, String arena, Location loc, World world) {
		super(world);
		this.m = m;
		this.arena = arena;
		setPosition(loc.getX(), loc.getY(), loc.getZ());
	}

	int X;
	int Y;
	int Z;
	
	public void setYaw(Location target){
		double disX = (this.locX - target.getX());
		double disY = (this.locY - target.getY());
		double disZ = (this.locZ - target.getZ());

		this.X = (int) (Math.abs(disX));
		this.Y = (int) (Math.abs(disY));
		this.Z = (int) (Math.abs(disZ));
		
		if (this.locX <= target.getX()) {
			if (this.locZ >= target.getZ()) {
				this.yaw = getLookAtYaw(new Vector(this.X, this.Y, this.Z)) + 180F;
			} else {
				this.yaw = getLookAtYaw(new Vector(this.X, this.Y, this.Z)) - 90F;
			}
		} else { // (this.locX > target.getX())
			if (this.locZ >= target.getZ()) {
				this.yaw = getLookAtYaw(new Vector(this.X, this.Y, this.Z)) + 90F;
			} else {
				this.yaw = getLookAtYaw(new Vector(this.X, this.Y, this.Z));
			}
		}
	}
	
	public static float getLookAtYaw(Vector motion) {
        double dx = motion.getX();
        double dz = motion.getZ();
        double yaw = 0;

        if (dx != 0) {
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            } else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx);
        } else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI - 90);
    }
	
	/*@Override
	public void e() {
		return;
	}*/

	public boolean damageEntity(DamageSource damagesource, int i) {
		return false;
	}

	@Override
	public int getExpReward() {
		return 0;
	}

	public boolean a(EntityComplexPart entitycomplexpart, DamageSource damagesource, int i) {
		return false;
	}

	
}