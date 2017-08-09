package com.simmike.flappybird;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	public static int spaceAllowed = 80;
	public static boolean gameActive = false;
	public static List<String> players = new ArrayList<String>();
	public static List<Boolean> alive = new ArrayList<Boolean>();
	private static List<Double> dx = new ArrayList<Double>();
 	private static List<Double> dy = new ArrayList<Double>();
 	private static List<Integer> pz = new ArrayList<Integer>();
	private static List<Integer> powerups = new ArrayList<Integer>();
	private static List<Boolean> closerActive = new ArrayList<Boolean>();
	private static List<Integer> rgapy = new ArrayList<Integer>();
	private static List<Integer> gapy = new ArrayList<Integer>();
	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(new Handler(),this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,new Runnable() {
			@Override
		    public void run() {
				if ( gameActive ) Main.manageTick();
		    }
		},0,1);
	}
	@Override
	public void onDisable() {}
	@Override
	public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
		if ( command.getName().equalsIgnoreCase("startgame") ) {
			gameActive = true;
			return true;
		} else if ( command.getName().equalsIgnoreCase("powerup") ) {
			if ( args.length != 1 ) {
				sender.sendMessage(ChatColor.RED + "Usage: /powerup <1-4>");
				return false;
			}
			int index;
			try {
				index = Integer.parseInt(args[0]);
			} catch (NumberFormatException error) {
				sender.sendMessage(ChatColor.RED + "Usage: /powerup <1-4>");
				return false;
			}
			if ( index < 1 || index > 4 ) {
				sender.sendMessage(ChatColor.RED + "Usage: /powerup <1-4>");
				return false;
			}
			index--;
			int[] colors = {8,14,5,2};
			World w = Bukkit.getServer().getWorlds().get(0);
			Location l = Bukkit.getServer().getPlayer(sender.getName()).getLocation();
			fill((int) l.getX() + 1,(int) l.getY(),(int) l.getZ() - 1,(int) l.getX() + 3,(int) l.getY() + 2,(int) l.getZ() + 1,Material.CONCRETE,colors[index]);
			return true;
		}
		return false;
	}
	class Handler implements Listener {
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			Player p = event.getPlayer();
			players.add(p.getName());
			dx.add((double) 0.3);
			dy.add((double) 0);
			pz.add(50 * (players.size() - 1));
			powerups.add(0);
			closerActive.add(false);
			rgapy.add(-1);
			gapy.add(-1);
			alive.add(true);
			p.teleport(new Location(Bukkit.getServer().getWorlds().get(0),0,128,50 * (players.size() - 1)));
		}
	}
	public static void manageTick() {
		for ( String pname : players ) {
			Player p = Bukkit.getServer().getPlayer(pname);
			if ( p == null ) continue;
			int index = players.indexOf(pname);
			if ( ! alive.get(index) ) continue;
			p.setVelocity(new Vector(dx.get(index),dy.get(index),0));
			if ( dy.get(index) >= -1 ) dy.set(index,dy.get(index) - 0.02);
			PlayerInventory inventory = p.getInventory();
			if ( ! inventory.contains(Material.EMERALD_BLOCK) ) {
				ItemStack stack = new ItemStack(Material.EMERALD_BLOCK,1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("DROP TO GO UP");
				stack.setItemMeta(meta);
				inventory.setItem(4,stack);
				dy.set(index,(double) 0.5);
			}
			Location l = p.getLocation();
			if ( (int) l.getX() % spaceAllowed == 0 ) {
				Random r = new Random();
				int gap = buildPipe((int) l.getX() + spaceAllowed,pz.get(index),index,r.nextInt(3) == 0);
				gapy.set(index,gap);
				if ( ! closerActive.get(index) ) {
					rgapy.set(index,gap);
				} else {
					closerActive.set(index,false);
				}
				System.out.println(closerActive);
				l.setX(l.getX() + 1);
				l.setY(l.getY() + 1);
				p.teleport(l);
				dx.set(index,0.3);
			}
			World w = Bukkit.getServer().getWorlds().get(0);
			l = p.getLocation();
			l.setX(l.getX() + 1);
			if ( ( w.getBlockAt(l).getType() == Material.WOOL || l.getY() <= 0 || l.getY() >= 256 ) && alive.get(index) ) {
				p.setHealth((double) 0); 
				alive.set(index,false);
			}
			Location a = p.getLocation();
			a.setY(a.getY() + 1);
			Location b = p.getLocation();
			b.setY(b.getY() - 1);
			if ( w.getBlockAt(l).getType() == Material.CONCRETE || w.getBlockAt(a).getType() == Material.CONCRETE || w.getBlockAt(b).getType() == Material.CONCRETE ) {
				int color = 0;
				if ( w.getBlockAt(l).getType() == Material.CONCRETE ) color = w.getBlockAt(l).getData();
				else if ( w.getBlockAt(a).getType() == Material.CONCRETE ) color = w.getBlockAt(a).getData();
				else if ( w.getBlockAt(b).getType() == Material.CONCRETE ) color = w.getBlockAt(b).getData();
				ItemStack stack = new ItemStack(Material.CONCRETE,1);
				stack.setDurability((short) color);
				ItemMeta meta = stack.getItemMeta();
				if ( color == 8 ) {
					meta.setDisplayName("GET CLOSER GAPS");
					stack.setItemMeta(meta);
					inventory.setItem(0,stack);
					powerups.set(index,powerups.get(index) | 1 << 0);
				} else if ( color == 14 ) {
					meta.setDisplayName("MOVE SLOWER");
					stack.setItemMeta(meta);
					inventory.setItem(2,stack);
					powerups.set(index,powerups.get(index) | 1 << 1);
				} else if ( color == 5 ) {
					meta.setDisplayName("MOVE EVERYONE FASTER");
					stack.setItemMeta(meta);
					inventory.setItem(6,stack);
					powerups.set(index,powerups.get(index) | 1 << 2);
				} else if ( color == 2 ) {
					meta.setDisplayName("MOVE TO THE GAP");
					stack.setItemMeta(meta);
					inventory.setItem(8,stack);
					powerups.set(index,powerups.get(index) | 1 << 3);
				}
			}
			if ( inventory.getItem(0) == null && ( (powerups.get(index) >> 0) & 1 ) == 1 ) {
				closerActive.set(index,true);
			}
			if ( inventory.getItem(2) == null && ( (powerups.get(index) >> 1) & 1 ) == 1 ) {
				dx.set(index,0.15);
				powerups.set(index,powerups.get(index) & ~(1 << 1));
			}
			if ( inventory.getItem(6) == null && ( (powerups.get(index) >> 2) & 1 ) == 1 ) {
				for ( String other : players ) {
					if ( other == pname ) continue;
					dx.set(players.indexOf(other),0.6);
				}
				powerups.set(index,powerups.get(index) & ~(1 << 2));
			}
			if ( inventory.getItem(8) == null && ( (powerups.get(index) >> 3) & 1 ) == 1 ) {
				l = p.getLocation();
				l.setY(gapy.get(index));
				p.teleport(l);
				powerups.set(index,powerups.get(index) & ~(1 << 3));
			}
		}
	}
	private static void fill(int x1,int y1,int z1,int x2,int y2,int z2,Material block,int data) {
		World w = Bukkit.getWorlds().get(0);
		if ( x1 > x2 ) {
			int temp = x2;
			x2 = x1;
			x1 = temp;
		}
		if ( y1 > y2 ) {
			int temp = y2;
			y2 = y1;
			y1 = temp;
		}
		if ( z1 > z2 ) {
			int temp = z2;
			z2 = z1;
			z1 = temp;
		}
		for ( int x = x1; x <= x2; x++ ) {
			for ( int y = y1; y <= y2; y++ ) {
				for ( int z = z1; z <= z2; z++ ) {
					w.getBlockAt(new Location(w,x,y,z)).setType(block);
					w.getBlockAt(new Location(w,x,y,z)).setData((byte) data);
				}
			}
		}
	}
	private static int buildPipe(int x,int z,int index,boolean powerup) {
		Random r = new Random();
		fill(x,1,z - 4,x,255,z + 4,Material.WOOL,5);
		fill(x,1,z - 4,x + 9,255,z - 4,Material.WOOL,5);
		fill(x,1,z + 4,x + 9,255,z + 4,Material.WOOL,5);
		fill(x + 9,1,z - 4,x + 9,255,z + 4,Material.WOOL,5);
		fill(x,1,z - 4,x + 9,1,z + 4,Material.WOOL,15);
		fill(x,255,z - 4,x + 9,255,z + 4,Material.WOOL,15);
		int gap;
		if ( closerActive.get(index) ) gap = rgapy.get(index) + r.nextInt(20) - 9;
		else gap = r.nextInt(255 - 22) + 11;
		fill(x - 1,gap,z - 5,x + 10,gap,z + 5,Material.WOOL,15);
		fill(x - 1,gap + 11,z - 5,x + 10,gap + 11,z + 5,Material.WOOL,15);
		fill(x,gap + 1,z - 4,x + 9,gap + 10,z + 4,Material.AIR,0);
		if ( powerup ) {
			int level = r.nextInt(4);
			int position = r.nextInt(255 - 22) + 11;
			int[] colors = {8,14,5,2};
			fill(x - (spaceAllowed / 2) - 1,position - 1,z - 1,x - (spaceAllowed / 2) + 1,position + 1,z + 1,Material.CONCRETE,colors[level]);
		}
		return gap;
	}
}
