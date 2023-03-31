package tc.oc.bountyhunter;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;

@CommandAlias("bountyhunter")
public class Commands extends BaseCommand {

  @Subcommand("reload")
  @CommandPermission("bountyhunter.reload")
  public void reload(CommandSender sender) {
    Main.get().reloadConfig();
    Config.create(Main.get().getConfig());
    Main.get().getManager().reload();
    sender.sendMessage("Bounty config has been reloaded.");
  }
}
