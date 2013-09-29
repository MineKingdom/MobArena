package com.garbagemule.MobArena.waves.ability.core;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.garbagemule.MobArena.framework.Arena;
import com.garbagemule.MobArena.waves.MABoss;
import com.garbagemule.MobArena.waves.ability.Ability;
import com.garbagemule.MobArena.waves.ability.AbilityInfo;

@AbilityInfo(
    name = "Clerk Benediction",
    aliases = {"clerkbuff"}
)
public class ClerkBenediction implements Ability {
	
    private final PotionEffect EFFECT = new PotionEffect(PotionEffectType.REGENERATION, 3 * 20, 10);
    
    @Override
    public void execute(Arena arena, MABoss boss) {
        for (Player p : arena.getPlayersInArena()) {
            p.addPotionEffect(EFFECT);
        }
    }
}
