package com.comze_instancelabs.mgsnake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.comze_instancelabs.mgsnake.nms.MEFallingBlock1_7_10;
import com.comze_instancelabs.mgsnake.nms.MEFallingBlock1_7_2;
import com.comze_instancelabs.mgsnake.nms.MEFallingBlock1_7_5;
import com.comze_instancelabs.mgsnake.nms.MEFallingBlock1_7_9;
import com.comze_instancelabs.mgsnake.nms.register1_7_10;
import com.comze_instancelabs.mgsnake.nms.register1_7_2;
import com.comze_instancelabs.mgsnake.nms.register1_7_5;
import com.comze_instancelabs.mgsnake.nms.register1_7_9;
import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.util.Util;

public class IArena extends Arena {

	Main m = null;
	BukkitTask task = null;
	HashMap<String, Integer> pteam = new HashMap<String, Integer>();
	HashMap<String, Integer> pslimes = new HashMap<String, Integer>();
	BukkitTask powerup_task;

	public IArena(Main m, String arena) {
		super(m, arena);
		this.m = m;
	}

	int failcount = 0;

	@Override
	public void started() {
		initPlayerMovements(this.getName());
		final IArena a = this;
		powerup_task = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				if (Math.random() * 100 <= m.getConfig().getInt("config.powerup_spawn_percentage")) {
					try {
						Player p = Bukkit.getPlayer(a.getAllPlayers().get((int) Math.random() * (a.getAllPlayers().size() - 1)));
						if (p != null) {
							boolean spawn = true;
							if (MinigamesAPI.getAPI().pinstances.get(m).global_lost.containsKey(p.getName())) {
								// player is a spectator, retry
								p = Bukkit.getPlayer(a.getAllPlayers().get((int) Math.random() * (a.getAllPlayers().size() - 1)));
								if (p != null) {
									if (MinigamesAPI.getAPI().pinstances.get(m).global_lost.containsKey(p.getName())) {
										spawn = false;
									}
								} else {
									spawn = false;
								}
							}
							if (spawn) {
								Util.spawnPowerup(m, a, p.getLocation().clone().add(0D, 5D, 0D), getItemStack());
							}
						}
					} catch (Exception e) {
						if (a != null) {
							if (a.getArenaState() != ArenaState.INGAME) {
								if (powerup_task != null) {
									System.out.println("Cancelled powerup task.");
									powerup_task.cancel();
								}
							}
						}
						Bukkit.getLogger().info("Use the latest MinigamesLib version to get powerups.");
						failcount++;
						if (failcount > 2) {
							if (powerup_task != null) {
								System.out.println("Cancelled powerup task.");
								powerup_task.cancel();
							}
						}
					}
				}
			}
		}, 60, 60);
	}

	@Override
	public void leavePlayer(final String playername, final boolean fullLeave) {
		Player p = Bukkit.getPlayer(playername);
		for (Entity t : p.getNearbyEntities(70, 70, 70)) {
			if (t.getType() == EntityType.SHEEP) {
				Sheep sheep = (Sheep) t;
				DyeColor color = ((Colorable) sheep).getColor();
				if (pteam.containsKey(p.getName())) {
					if (pteam.get(p.getName()) == (int) color.getData()) {
						t.remove();
					}
				}

			}
		}
		p.removePotionEffect(PotionEffectType.JUMP);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		super.leavePlayer(playername, fullLeave);
	}

	@Override
	public void joinPlayerLobby(String playername) {
		if (pteam.containsKey(playername)) {
			pteam.remove(playername);
		}
		for (int i = 0; i < 16; i++) {
			if (!pteam.values().contains(i)) {
				pteam.put(playername, i);
				break;
			}
		}
		if (pteam.containsKey(playername)) {
			super.joinPlayerLobby(playername);
		}
	}

	@Override
	public void spectate(String playername) {
		Player p = Bukkit.getPlayer(playername);
		if (p != null) {
			p.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
		super.spectate(playername);
	}

	@Override
	public void stop() {
		if (task != null) {
			task.cancel();
		}
		super.stop();
		pvecs.clear();
		this.psheep1_7_10.clear();
		this.psheep1_7_9.clear();
		this.psheep1_7_5.clear();
		this.psheep1_7_2.clear();
	}

	public ItemStack getItemStack() {
		double i = Math.random() * 100;
		ItemStack ret = new ItemStack(Material.IRON_BOOTS);
		if (i <= 60) {
			// get a speed boost
			ret = new ItemStack(Material.IRON_BOOTS);
		} else {
			// get a jump boost
			ret = new ItemStack(Material.GOLD_BOOTS);
		}
		return ret;
	}

	Random r = new Random();

	private void initPlayerMovements(final String arena) {
		boolean invisible = m.getConfig().getBoolean("config.players_invisible");
		for (String p_ : this.getAllPlayers()) {
			final Player p = Bukkit.getPlayer(p_);

			p.setWalkSpeed(0.0F);
			p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 9999999, -5));
			if (invisible) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 2));
			}
			Vector v = p.getLocation().getDirection().normalize();
			Location l = p.getLocation().subtract((new Vector(v.getX(), 0.0001D, v.getZ())));
			Location l_ = p.getLocation().subtract((new Vector(v.getX(), 0.0001D, v.getZ()).multiply(2D)));
			if (m.v1_7_2) {
				ArrayList<MEFallingBlock1_7_2> temp = new ArrayList<MEFallingBlock1_7_2>(Arrays.asList(register1_7_2.spawnSheep(m, arena, l.add(0D, 1D, 0D), pteam.get(p.getName())), register1_7_2.spawnSheep(m, arena, l_.add(0D, 1D, 0D), pteam.get(p.getName()))));
				psheep1_7_2.put(p, temp);
			} else if (m.v1_7_5) {
				ArrayList<MEFallingBlock1_7_5> temp = new ArrayList<MEFallingBlock1_7_5>(Arrays.asList(register1_7_5.spawnSheep(m, arena, l.add(0D, 1D, 0D), pteam.get(p.getName())), register1_7_5.spawnSheep(m, arena, l_.add(0D, 1D, 0D), pteam.get(p.getName()))));
				psheep1_7_5.put(p, temp);
			} else if (m.v1_7_9) {
				ArrayList<MEFallingBlock1_7_9> temp = new ArrayList<MEFallingBlock1_7_9>(Arrays.asList(register1_7_9.spawnSheep(m, arena, l.add(0D, 1D, 0D), pteam.get(p.getName())), register1_7_9.spawnSheep(m, arena, l_.add(0D, 1D, 0D), pteam.get(p.getName()))));
				psheep1_7_9.put(p, temp);
			} else if (m.v1_7_10) {
				ArrayList<MEFallingBlock1_7_10> temp = new ArrayList<MEFallingBlock1_7_10>(Arrays.asList(register1_7_10.spawnSheep(m, arena, l.add(0D, 1D, 0D), pteam.get(p.getName())), register1_7_10.spawnSheep(m, arena, l_.add(0D, 1D, 0D), pteam.get(p.getName()))));
				psheep1_7_10.put(p, temp);
			}

		}

		BukkitTask t = null;

		arenasize.put(arena, 3);

		final IArena a = this;

		t = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
			public void run() {
				ArrayList<String> temparr = new ArrayList<String>(a.getAllPlayers());
				for (String p_ : temparr) {
					if (!MinigamesAPI.getAPI().pinstances.get(m).global_lost.containsKey(p_)) {
						final Player p = Bukkit.getPlayer(p_);
						Location l_ = p.getLocation();
						l_.setPitch(0F);
						double multiplier = 0.4D;
						double jump_multiplier = 0.0001D;
						if (m.pspeed.contains(p.getName())) {
							multiplier = 1D;
						}
						if (m.pjump.contains(p.getName())) {
							jump_multiplier = 1.4D;
							m.pjump.remove(p.getName());
						}
						Vector dir = l_.getDirection().normalize().multiply(multiplier);
						Vector dir_ = new Vector(dir.getX(), jump_multiplier, dir.getZ());
						p.setVelocity(dir_);

						Vector v = p.getLocation().getDirection().normalize();
						Location l = p.getLocation().subtract((new Vector(v.getX(), 0.0001D, v.getZ()).multiply(-1D)));

						if (l.getBlock().getType() != Material.AIR) {
							a.spectate(p.getName());
						}

						int chance = r.nextInt(100);
						if (chance < 3) {
							int temp = r.nextInt(10) - 5;
							if (temp < 0) {
								temp -= 3;
							} else {
								temp += 3;
							}
							Slime s = (Slime) p.getWorld().spawnEntity(p.getLocation().add(temp, 0, temp), EntityType.SLIME);
							s.setSize(1);
						}

						for (Entity ent : p.getNearbyEntities(1, 1, 1)) {
							if (ent.getType() == EntityType.SHEEP) {
								Sheep s = (Sheep) ent;
								DyeColor color = ((Colorable) s).getColor();
								if (color.getData() != (byte) pteam.get(p.getName()).byteValue()) {
									a.spectate(p.getName());
								}
							} else if (ent.getType() == EntityType.SLIME) {
								arenasize.put(arena, arenasize.get(arena) + 1);
								m.scoreboard.updateScoreboard(a);
								ent.remove();
								for (String p__ : a.getAllPlayers()) {
									final Player pp = Bukkit.getPlayer(p__);
									if (!MinigamesAPI.getAPI().pinstances.get(m).global_lost.containsKey(p__)) {
										if (m.v1_7_2) {
											ArrayList<MEFallingBlock1_7_2> temp = new ArrayList<MEFallingBlock1_7_2>(psheep1_7_2.get(pp));
											temp.add(register1_7_2.spawnSheep(m, arena, pp.getLocation(), pteam.get(pp.getName())));
											psheep1_7_2.put(pp, temp);
										} else if (m.v1_7_5) {
											ArrayList<MEFallingBlock1_7_5> temp = new ArrayList<MEFallingBlock1_7_5>(psheep1_7_5.get(pp));
											temp.add(register1_7_5.spawnSheep(m, arena, pp.getLocation(), pteam.get(pp.getName())));
											psheep1_7_5.put(pp, temp);
										} else if (m.v1_7_9) {
											ArrayList<MEFallingBlock1_7_9> temp = new ArrayList<MEFallingBlock1_7_9>(psheep1_7_9.get(pp));
											temp.add(register1_7_9.spawnSheep(m, arena, pp.getLocation(), pteam.get(pp.getName())));
											psheep1_7_9.put(pp, temp);
										} else if (m.v1_7_10) {
											ArrayList<MEFallingBlock1_7_10> temp = new ArrayList<MEFallingBlock1_7_10>(psheep1_7_10.get(pp));
											temp.add(register1_7_10.spawnSheep(m, arena, pp.getLocation(), pteam.get(pp.getName())));
											psheep1_7_10.put(pp, temp);
										}
									}
								}
							}
						}

						if (!pvecs.containsKey(p)) {
							pvecs.put(p, new ArrayList<Vector>(Arrays.asList(v.multiply(0.45D))));
						} else {
							ArrayList<Vector> temp = new ArrayList<Vector>(pvecs.get(p));
							if (temp.size() > arenasize.get(arena)) {
								temp.remove(0);
							}
							temp.add(v.multiply(0.45D));
							pvecs.put(p, temp);
						}

						// System.out.println("[A] " + pvecs.size() + pvecs.get(p));

						updateLocs(arena);

					}
				}
			}
		}, 2L, 2L);

		task = t;
	}

	public HashMap<Player, ArrayList<Location>> plocs = new HashMap<Player, ArrayList<Location>>();
	public HashMap<String, Integer> arenasize = new HashMap<String, Integer>();

	public HashMap<Player, ArrayList<MEFallingBlock1_7_2>> psheep1_7_2 = new HashMap<Player, ArrayList<MEFallingBlock1_7_2>>();
	public HashMap<Player, ArrayList<MEFallingBlock1_7_5>> psheep1_7_5 = new HashMap<Player, ArrayList<MEFallingBlock1_7_5>>();
	public HashMap<Player, ArrayList<MEFallingBlock1_7_9>> psheep1_7_9 = new HashMap<Player, ArrayList<MEFallingBlock1_7_9>>();
	public HashMap<Player, ArrayList<MEFallingBlock1_7_10>> psheep1_7_10 = new HashMap<Player, ArrayList<MEFallingBlock1_7_10>>();

	public HashMap<Player, ArrayList<Vector>> pvecs = new HashMap<Player, ArrayList<Vector>>();

	private void updateLocs(String arena) {
		IArena a = this;
		for (String p__ : a.getAllPlayers()) {
			Player p_ = Bukkit.getPlayer(p__);
			if (!MinigamesAPI.getAPI().pinstances.get(m).global_lost.containsKey(p_)) {

				if (!plocs.containsKey(p_)) {
					plocs.put(p_, new ArrayList<Location>(Arrays.asList(p_.getLocation())));
				} else {
					ArrayList<Location> temp = new ArrayList<Location>(plocs.get(p_));
					if (temp.size() > arenasize.get(arena)) {
						temp.remove(0);
					}
					temp.add(p_.getLocation());
					plocs.put(p_, temp);

					int c = 0;

					if (m.v1_7_2) {
						for (MEFallingBlock1_7_2 ms : psheep1_7_2.get(p_)) {
							if (pvecs.containsKey(p_)) {
								if (c < pvecs.get(p_).size()) {
									Vector direction = plocs.get(p_).get(c).toVector().subtract(psheep1_7_2.get(p_).get(c).getBukkitEntity().getLocation().toVector()).normalize();
									psheep1_7_2.get(p_).get(c).setYaw(plocs.get(p_).get(c));
									psheep1_7_2.get(p_).get(c).getBukkitEntity().setVelocity(direction.multiply(0.5D));
									c++;
								}
							} else {
								pvecs.put(p_, new ArrayList<Vector>(Arrays.asList(p_.getLocation().getDirection().normalize().add(new Vector(0.1D, 0.1D, 0.1D)))));
							}
						}
					} else if (m.v1_7_5) {
						for (MEFallingBlock1_7_5 ms : psheep1_7_5.get(p_)) {
							if (pvecs.containsKey(p_)) {
								if (c < pvecs.get(p_).size()) {
									Vector direction = plocs.get(p_).get(c).toVector().subtract(psheep1_7_5.get(p_).get(c).getBukkitEntity().getLocation().toVector()).normalize();
									psheep1_7_5.get(p_).get(c).setYaw(plocs.get(p_).get(c));
									psheep1_7_5.get(p_).get(c).getBukkitEntity().setVelocity(direction.multiply(0.5D));
									c++;
								}
							} else {
								pvecs.put(p_, new ArrayList<Vector>(Arrays.asList(p_.getLocation().getDirection().normalize().add(new Vector(0.1D, 0.1D, 0.1D)))));
							}
						}
					} else if (m.v1_7_9) {
						for (MEFallingBlock1_7_9 ms : psheep1_7_9.get(p_)) {
							if (pvecs.containsKey(p_)) {
								if (c < pvecs.get(p_).size()) {
									Vector direction = plocs.get(p_).get(c).toVector().subtract(psheep1_7_9.get(p_).get(c).getBukkitEntity().getLocation().toVector()).normalize();
									psheep1_7_9.get(p_).get(c).setYaw(plocs.get(p_).get(c));
									psheep1_7_9.get(p_).get(c).getBukkitEntity().setVelocity(direction.multiply(0.5D));
									c++;
								}
							} else {
								pvecs.put(p_, new ArrayList<Vector>(Arrays.asList(p_.getLocation().getDirection().normalize().add(new Vector(0.1D, 0.1D, 0.1D)))));
							}
						}
					} else if (m.v1_7_10) {
						for (MEFallingBlock1_7_10 ms : psheep1_7_10.get(p_)) {
							if (pvecs.containsKey(p_)) {
								if (c < pvecs.get(p_).size()) {
									Vector direction = plocs.get(p_).get(c).toVector().subtract(psheep1_7_10.get(p_).get(c).getBukkitEntity().getLocation().toVector()).normalize();
									psheep1_7_10.get(p_).get(c).setYaw(plocs.get(p_).get(c));
									psheep1_7_10.get(p_).get(c).getBukkitEntity().setVelocity(direction.multiply(0.5D));
									c++;
								}
							} else {
								pvecs.put(p_, new ArrayList<Vector>(Arrays.asList(p_.getLocation().getDirection().normalize().add(new Vector(0.1D, 0.1D, 0.1D)))));
							}
						}
					}
				}
			}
		}
	}

}
