package net.tigerclan.KingLinkTiger.HGAdmin;

import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HGAdmin extends JavaPlugin{
	Logger log = Logger.getLogger("Minecraft");
	public boolean GameStart = false;
	Player[] players = null;
	int numDeaths = 0;
	int numPlayers = 0;
	String[] deadPlayers;
	
	public void onDisable() {
		log.info("HGAdmin is disabled.");
	}
	
	 public void onEnable(){
		 PluginManager pm = this.getServer().getPluginManager();

		 pm.registerEvents(new Listener(){
			 
			 //Join Event
			 @EventHandler
			 public void onPlayerJoin(PlayerJoinEvent event){
				 if(GameStart){
					//Allow the spectators to fly
					event.getPlayer().setAllowFlight(true);
						
					//Hide the player form the others
					for (Player players : Bukkit.getOnlinePlayers()){
					    players.hidePlayer(event.getPlayer());
					}
				 }
			 }
			 
			 
			 //Death Event
			 @EventHandler
				public void onPlayerDeath(PlayerDeathEvent event){
				 	if(GameStart){
					 		//Add the player to our dead Player's List
				 			players = (Player[]) ArrayUtils.removeElement(players, event.getEntity());
				 			//deadPlayers[numDeaths] = event.getEntity().getDisplayName();
	
					 		//Set the death Message to nothing because we are going to have a broadcast message
					 		event.setDeathMessage("");
					 		
					 		//Broadcast that the player was eliminated
					 		getServer().broadcastMessage(event.getEntity().getName() + " has been eliminated!");
					 		
					 		//Display their Rank
							getServer().broadcastMessage("Their rank is: " + (numPlayers-numDeaths) + "/" + numPlayers);
							
							//If this player was the 2nd to last player announce the winner
							if(numDeaths == (numPlayers-2)){
					 			getServer().broadcastMessage(players[0].getDisplayName() + " has won the game!");
					 			//GameStart = false;
					 		}
							
							//Allow the spectators to fly
							event.getEntity().setAllowFlight(true);
							
							//Hide the player form the others
						    for (Player players : Bukkit.getOnlinePlayers()){
						    	players.hidePlayer(event.getEntity());
						    }
						    
						    //Add one to the number of deaths
					 		numDeaths++;
				 	}
				}
			 
			 //Disable PvP and Mob Damage Before Game
			 @EventHandler
			 public void onEntityDamage(EntityDamageEvent event) {
				 if(!GameStart){
				 	if(event.getEntity() instanceof Player){
				 		event.setCancelled(true);
				 	}
				 }else if(event.getEntity() instanceof Player && isDead((Player) event.getEntity())){
					 	event.setCancelled(true);
				 }
				 
			}
			 
			 @EventHandler
			 public void onDamageEntity(EntityDamageByEntityEvent event){
				 if(event.getDamager() instanceof Player){
					 if(isDead((Player) event.getDamager())){
						 event.setCancelled(true);
					 }
				 }
			 }
			
			//Disable Hunger
			@EventHandler
			public void onFoodLevelChange(FoodLevelChangeEvent event){
				if(!GameStart){
					if(event.getEntity() instanceof Player){
						Player player = (Player) event.getEntity();
						player.setExhaustion(0);
						event.setFoodLevel(19);
						event.setCancelled(true);
					}
				}else if(isDead((Player) event.getEntity())){
					if(event.getEntity() instanceof Player){
						Player player = (Player) event.getEntity();
						player.setExhaustion(0);
						event.setFoodLevel(19);
						event.setCancelled(true);
					}
				}
			}
			
			//If the player is dead don't let them break blocks
			@EventHandler
			public void onBlockBreak(BlockBreakEvent event){
				if(!event.getPlayer().isOp()){
					event.setCancelled(true);
				}
			}
			
			//If the player is dead don't let them place blocks
			@EventHandler
			public void onBlockPlace(BlockPlaceEvent event){
				if(!event.getPlayer().isOp()){
						event.setCancelled(true);
				}
			}
			
			//If the player is dead block them from picking up items
			@EventHandler
			public void onPickupItem(PlayerPickupItemEvent event){
				if(!event.getPlayer().isOp()){
					if(!GameStart){
						event.setCancelled(true);
					}else if(isDead(event.getPlayer())){
						event.setCancelled(true);
					}
				}
			}
			
			//If the player is dead block them from picking up items
			@EventHandler
			public void onDropItem(PlayerKickEvent event){
				if(!event.getPlayer().isOp()){
					if(!GameStart){
						event.setCancelled(true);
					}else if(isDead(event.getPlayer())){
						event.setCancelled(true);
					}
				}
			}
			
			//Function to check if supplied player is dead
			public boolean isDead(Player p){
				
				if (players == null) {
					  return false;
				}
				
				for(int i=0; i < players.length; i++){
					if(players[i] == p){
						return false;
					}
				}
				return true;
			}
			
		 }, this);
		 log.info("HGAdmin is Enabled!");
	 }
	 
	 public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		 if(!(label.equalsIgnoreCase("hga") || label.equalsIgnoreCase("hgadmin")) || args.length < 1){
			 return false;
		 }
		 
		 if(args[0].equalsIgnoreCase("start")){
			 if(sender.isOp()){
				 GameStart = true;
				 getServer().broadcastMessage("The game is afoot!");
				 players = Bukkit.getOnlinePlayers();
				 numPlayers = players.length;
			 }
		 }else if(args[0].equalsIgnoreCase("rest")){
			 if(sender.isOp()){
				 GameStart = true;
				 getServer().broadcastMessage("The game has been reset");
				 init();
			 }
		 }else if(args[0].equalsIgnoreCase("stop")){
			 if(sender.isOp()){
				 GameStart = false;
				 getServer().broadcastMessage("The game has been stopped");
			 }
		 }else if(args[0].equalsIgnoreCase("newmap")){
			 if(sender.isOp()){
				 GameStart = false;
				 getServer().broadcastMessage("The server is shutting down and will restart with a new map");
				 try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {}
				 getServer().dispatchCommand(sender, "save-all");
				 getServer().dispatchCommand(sender, "stop");
			 }
		 }else if(args[0].equalsIgnoreCase("visible")){
			 if(sender.isOp()){
				 for(Player players : Bukkit.getOnlinePlayers()){
					    players.showPlayer(players);
				}
			 }
		 }
		 
		 return false;
	 }
	 
	 public void init(){
		 GameStart = false;
		 players = null;
		 numDeaths = 0;
		 numPlayers = 0;
		 for(Player players : Bukkit.getOnlinePlayers()){
			    players.showPlayer(players);
		}
	 }
}
