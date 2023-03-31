package tc.oc.bountyhunter.runners;

import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.sound.Sound.sound;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import tc.oc.bountyhunter.Config;
import tc.oc.bountyhunter.Main;
import tc.oc.bountyhunter.Manager;
import tc.oc.bountyhunter.Messages;
import tc.oc.bountyhunter.Utils;
import tc.oc.pgm.api.match.Match;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.blitz.BlitzMatchModule;
import tc.oc.pgm.stats.PlayerStats;
import tc.oc.pgm.stats.StatsMatchModule;

public class SelectionRunner implements Runnable {

  private final Manager manager;
  private final Match match;
  private final int taskId;

  private static UUID lastTargetUUID;

  public SelectionRunner(Manager manager, Match match) {
    this.manager = manager;
    this.match = match;

    taskId =
        Bukkit.getScheduler()
            .scheduleSyncDelayedTask(
                Main.get(), this, Utils.durationTicks(Config.get().getStartsAfter()));
  }

  @Override
  public void run() {
    MatchPlayer player = selectBountyTarget();

    if (player == null) {
      manager.startSelectionRunner();
    } else {
      lastTargetUUID = player.getId();
      manager.startExpireRunner(player);
    }
  }

  public MatchPlayer selectBountyTarget() {
    if (match == null) return null;

    if (!Config.get().getEnabled()) return null;

    // Don't select a target when the match is Blitz
    if (match.hasModule(BlitzMatchModule.class)) return null;

    StatsMatchModule stats = this.match.getModule(StatsMatchModule.class);
    if (stats == null) return null;

    // Nobody playing.. interesting.
    Collection<MatchPlayer> participants = this.match.getParticipants();
    if (participants.isEmpty()) return null;

    // Find all players that pass the criteria
    boolean allowDuplicate = Config.get().isAllowDuplicate();
    List<MatchPlayer> validPlayers =
        participants.stream()
            .filter(MatchPlayer::isAlive)
            .filter(player -> allowDuplicate || !player.getId().equals(lastTargetUUID))
            .filter(
                player -> {
                  PlayerStats playerStat = stats.getPlayerStat(player);
                  return (playerStat.getKillstreak() >= Config.get().getMinKillstreak());
                })
            .collect(Collectors.toList());

    // No valid players found check again later
    if (validPlayers.isEmpty()) {
      return null;
    }

    Random random = new Random();
    MatchPlayer player = validPlayers.get(random.nextInt(validPlayers.size()));

    player.showTitle(Messages.bountyTitle());
    this.match.sendMessage(Messages.bountySelected(player));
    this.match.playSound(sound(key("mob.horse.donkey.death"), Sound.Source.MASTER, 1f, 0.8f));

    Component component = Messages.bountyAllInfo();
    this.match.getParticipants().stream()
        .filter(p -> !p.equals(player))
        .forEach(p -> p.sendMessage(component));

    player.sendMessage(Messages.bountyPlayerInfo());

    return player;
  }

  public void cancel() {
    Bukkit.getScheduler().cancelTask(taskId);
  }
}
