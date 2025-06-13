package me.darumba.playerVaults;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerVaults extends JavaPlugin implements CommandExecutor {
   private File vaultsFile;
   private FileConfiguration vaultsConfig;
   private HashMap<UUID, HashMap<Integer, Inventory>> vaults = new HashMap();

   public void onEnable() {
      this.setupVaultFile();
      this.getCommand("playervaults").setExecutor(this);
      this.getLogger().info("PlayerVaults has been enabled!");
   }

   public void onDisable() {
      this.saveVaults();
      this.getLogger().info("PlayerVaults has been disabled!");
   }

   private void setupVaultFile() {
      this.vaultsFile = new File(this.getDataFolder(), "vaults.yml");
      if (!this.vaultsFile.exists()) {
         try {
            this.vaultsFile.createNewFile();
         } catch (IOException var2) {
            var2.printStackTrace();
         }
      }

      this.vaultsConfig = YamlConfiguration.loadConfiguration(this.vaultsFile);
   }

   private Inventory getVault(UUID uuid, int vaultNumber) {
      this.vaults.putIfAbsent(uuid, new HashMap());
      if (!((HashMap)this.vaults.get(uuid)).containsKey(vaultNumber)) {
         Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "Kho #" + vaultNumber);
         ((HashMap)this.vaults.get(uuid)).put(vaultNumber, inv);
      }

      return (Inventory)((HashMap)this.vaults.get(uuid)).get(vaultNumber);
   }

   private void saveVaults() {
      Iterator var1 = this.vaults.keySet().iterator();

      while(var1.hasNext()) {
         UUID uuid = (UUID)var1.next();
         Iterator var3 = ((HashMap)this.vaults.get(uuid)).keySet().iterator();

         while(var3.hasNext()) {
            int vaultNumber = (Integer)var3.next();
            Inventory inv = (Inventory)((HashMap)this.vaults.get(uuid)).get(vaultNumber);
            if (this.isInventoryEmpty(inv)) {
               this.vaultsConfig.set(String.valueOf(uuid) + ".vaults." + vaultNumber, (Object)null);
            } else {
               this.vaultsConfig.set(String.valueOf(uuid) + ".vaults." + vaultNumber, inv.getContents());
            }
         }
      }

      try {
         this.vaultsConfig.save(this.vaultsFile);
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   private boolean isInventoryEmpty(Inventory inv) {
      ItemStack[] var2 = inv.getContents();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ItemStack item = var2[var4];
         if (item != null) {
            return false;
         }
      }

      return true;
   }

   private boolean hasVaultPermission(Player player, int vaultNumber) {
      if (player.hasPermission("pv.admin")) {
         return true;
      } else {
         if (player.hasPermission("pv.vault." + vaultNumber)) {
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("Only players can use this command!");
         return true;
      } else {
         Player player = (Player)sender;
         String var10001;
         if (args.length == 1) {
            try {
               int vaultNumber = Integer.parseInt(args[0]);
               if (vaultNumber < 1 || vaultNumber > 21) {
                  player.sendMessage(String.valueOf(ChatColor.RED) + "Số kho không hợp lệ! Chọn từ 1-21.");
                  return true;
               }

               if (!this.hasVaultPermission(player, vaultNumber)) {
                  player.sendMessage(String.valueOf(ChatColor.RED) + "Bạn không có quyền để mở kho này!");
                  return true;
               }

               Inventory vault = this.getVault(player.getUniqueId(), vaultNumber);
               player.openInventory(vault);
               var10001 = String.valueOf(ChatColor.GREEN);
               player.sendMessage(var10001 + "Đang mở kho #" + vaultNumber + "...");
            } catch (NumberFormatException var9) {
               player.sendMessage(String.valueOf(ChatColor.RED) + "Số kho không hợp lệ! Chọn từ 1-21.");
            }

            return true;
         } else if (args.length == 2) {
            if (!player.hasPermission("pv.admin")) {
               player.sendMessage(String.valueOf(ChatColor.RED) + "Bạn không có quyền để mở kho này!");
               return true;
            } else {
               Player target = Bukkit.getPlayer(args[0]);
               if (target != null && target.isOnline()) {
                  try {
                     int vaultNumber = Integer.parseInt(args[1]);
                     if (vaultNumber < 1 || vaultNumber > 21) {
                        player.sendMessage(String.valueOf(ChatColor.RED) + "Số kho không hợp lệ! Chọn từ 1-21.");
                        return true;
                     }

                     Inventory vault = this.getVault(target.getUniqueId(), vaultNumber);
                     player.openInventory(vault);
                     var10001 = String.valueOf(ChatColor.GREEN);
                     player.sendMessage(var10001 + "Đang mở kho #" + vaultNumber + " của" + target.getName() + "...");
                  } catch (NumberFormatException var10) {
                     player.sendMessage(String.valueOf(ChatColor.RED) + "Số kho không hợp lệ! Chọn từ 1-21.");
                  }

                  return true;
               } else {
                  player.sendMessage(String.valueOf(ChatColor.RED) + "Người chơi đó hiện không trực tuyến!");
                  return true;
               }
            }
         } else {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Sử dụng: /pv [player] <1-21>");
            return true;
         }
      }
   }
}
