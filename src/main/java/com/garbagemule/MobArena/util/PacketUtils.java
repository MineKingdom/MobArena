package com.garbagemule.MobArena.util;

import java.lang.reflect.Field;
import java.util.HashSet;

import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Packet;
import net.minecraft.server.v1_6_R2.Packet205ClientCommand;
import net.minecraft.server.v1_6_R2.Packet24MobSpawn;
import net.minecraft.server.v1_6_R2.Packet29DestroyEntity;
import net.minecraft.server.v1_6_R2.Packet40EntityMetadata;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.garbagemule.MobArena.MobArena;

public class PacketUtils {
  public static final int ENTITY_ID = 1234;
	
	private static HashSet<String> hasHealthBar = new HashSet<String>();
	
	private static void sendPacket(Player player, Packet packet){
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		
		entityPlayer.playerConnection.sendPacket(packet);
	}
	
	//Accessing packets
	private static Packet24MobSpawn getMobPacket(String text, Location loc){
		Packet24MobSpawn mobPacket = new Packet24MobSpawn();
		
		mobPacket.a = (int) ENTITY_ID; //Entity ID
		mobPacket.b = (byte) EntityType.ENDER_DRAGON.getTypeId(); //Mob type (ID: 64)
		mobPacket.c = (int) Math.floor(loc.getBlockX() * 32.0D); //X position
		mobPacket.d = (int) Math.floor((loc.getBlockY() - 256) * 32.0D); //Y position
		mobPacket.e = (int) Math.floor(loc.getBlockZ() * 32.0D); //Z position
		mobPacket.f = (byte) 0; //Pitch
		mobPacket.g = (byte) 0; //Head Pitch
		mobPacket.h = (byte) 0; //Yaw
		mobPacket.i = (short) 0; //X velocity
		mobPacket.j = (short) 0; //Y velocity
		mobPacket.k = (short) 0; //Z velocity
		
		DataWatcher watcher = getWatcher(text, 200);
		
		try{
			Field t = Packet24MobSpawn.class.getDeclaredField("t");
			
			t.setAccessible(true);
			t.set(mobPacket, watcher);
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return mobPacket;
	}
	
	private static Packet29DestroyEntity getDestroyEntityPacket(){
		Packet29DestroyEntity packet = new Packet29DestroyEntity();
		
		packet.a = new int[]{ENTITY_ID};
		
		return packet;
	}
	
	private static Packet40EntityMetadata getMetadataPacket(DataWatcher watcher){
		Packet40EntityMetadata metaPacket = new Packet40EntityMetadata();
		
		metaPacket.a = (int) ENTITY_ID;
		
		try{
			Field b = Packet40EntityMetadata.class.getDeclaredField("b");
			
			b.setAccessible(true);
			b.set(metaPacket, watcher.c());
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return metaPacket;
	}
		
	private static Packet205ClientCommand getRespawnPacket(){
		Packet205ClientCommand packet = new Packet205ClientCommand();
		
		packet.a = (int) 1;
		
		return packet;
	}
	
	private static DataWatcher getWatcher(String text, int health){
		DataWatcher watcher = new DataWatcher();
		
		watcher.a(0, (Byte) (byte) 0x20); //Flags, 0x20 = invisible
		watcher.a(6, (Float) (float) health);
		watcher.a(10, (String) text); //Entity name
		watcher.a(11, (Byte) (byte) 1); //Show name, 1 = show, 0 = don't show
		//watcher.a(16, (Integer) (int) health); //Wither health, 200 = full health
		
		return watcher;
	}
	
	//Other methods
	public static void displayTextBar(String text, final Player player){
		try {
			Packet24MobSpawn mobPacket = getMobPacket(text, player.getLocation());
			
			sendPacket(player, mobPacket);
			hasHealthBar.add(player.getName());
			
			new BukkitRunnable(){
				@Override
				public void run(){
					Packet29DestroyEntity destroyEntityPacket = getDestroyEntityPacket();
					
					sendPacket(player, destroyEntityPacket);
					hasHealthBar.remove(player.getName());
				}
			}.runTaskLater(MobArena.getInstance(), 120L);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void closeHealthBar(final Player player) {
		try {
			Packet29DestroyEntity destroyEntityPacket = getDestroyEntityPacket();
			
			sendPacket(player, destroyEntityPacket);
			hasHealthBar.remove(player.getName());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void updateHealthBar(final String text, final Player player, final double health) {
		try {
			if (hasHealthBar.contains(player.getName())) {
				DataWatcher watcher = getWatcher(text, (int) (health * 200));
				Packet40EntityMetadata metaPacket = getMetadataPacket(watcher);
				
				sendPacket(player, metaPacket);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void displayHealthBar(final String text, final Player player) {
		displayHealthBar(text, player, 1.0);
	}
	
	public static void displayHealthBar(final String text, final Player player, final double health) {
		try {
			Packet24MobSpawn mobPacket = getMobPacket(text, player.getLocation());
			
			sendPacket(player, mobPacket);
			hasHealthBar.add(player.getName());
			updateHealthBar(text, player, health);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void displayLoadingBar(final String text, final String completeText, final Player player, final int healthAdd, final long delay, final boolean loadUp){
		try {
			Packet24MobSpawn mobPacket = getMobPacket(text, player.getLocation());
			
			sendPacket(player, mobPacket);
			hasHealthBar.add(player.getName());
			
			new BukkitRunnable(){
				int health = (loadUp ? 0 : 200);
				
				@Override
				public void run(){
					if((loadUp ? health < 200 : health > 0)){
						DataWatcher watcher = getWatcher(text, health);
						Packet40EntityMetadata metaPacket = getMetadataPacket(watcher);
						
						sendPacket(player, metaPacket);
						
						if(loadUp){
							health += healthAdd;
						} else {
							health -= healthAdd;
						}
					} else {
						DataWatcher watcher = getWatcher(text, (loadUp ? 200 : 0));
						Packet40EntityMetadata metaPacket = getMetadataPacket(watcher);
						Packet29DestroyEntity destroyEntityPacket = getDestroyEntityPacket();
						
						sendPacket(player, metaPacket);					
						sendPacket(player, destroyEntityPacket);
						hasHealthBar.remove(player.getName());
						
						//Complete text
						Packet24MobSpawn mobPacket = getMobPacket(completeText, player.getLocation());
						
						sendPacket(player, mobPacket);
						hasHealthBar.add(player.getName());
						
						DataWatcher watcher2 = getWatcher(completeText, 200);
						Packet40EntityMetadata metaPacket2 = getMetadataPacket(watcher2);
						
						sendPacket(player, metaPacket2);
						
						new BukkitRunnable(){
							@Override
							public void run(){
								Packet29DestroyEntity destroyEntityPacket = getDestroyEntityPacket();
								
								sendPacket(player, destroyEntityPacket);
								hasHealthBar.remove(player.getName());
							}
						}.runTaskLater(MobArena.getInstance(), 40L);
						
						this.cancel();
					}
				}
			}.runTaskTimer(MobArena.getInstance(), delay, delay);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static void displayLoadingBar(final String text, final String completeText, final Player player, final int secondsDelay, final boolean loadUp){
		final int healthChangePerSecond = 200 / secondsDelay;
		
		displayLoadingBar(text, completeText, player, healthChangePerSecond, 20L, loadUp);
	}
}
