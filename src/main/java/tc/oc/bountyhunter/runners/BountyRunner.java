package tc.oc.bountyhunter.runners;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import tc.oc.bountyhunter.Config;
import tc.oc.bountyhunter.Main;
import tc.oc.bountyhunter.Manager;
import tc.oc.bountyhunter.Messages;
import tc.oc.occ.dispense.events.currency.CurrencyType;
import tc.oc.occ.dispense.events.currency.PlayerEarnCurrencyEvent;
import tc.oc.pgm.api.player.MatchPlayer;

public class BountyRunner implements Runnable {

  private final Manager manager;
  private long remainingTicks;
  private final int taskId;
  private static final long TICK_FREQUENCY = 10;

  private final MatchPlayer player;
  private int bountyKills;

  public BountyRunner(Manager manager, MatchPlayer player, long ticks) {
    this.manager = manager;
    this.player = player;
    this.remainingTicks = ticks;
    this.bountyKills = 0;

    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.get(), this, 0, TICK_FREQUENCY);
  }

  @Override
  public void run() {
    remainingTicks -= TICK_FREQUENCY;

    spawnParticles();

    if (remainingTicks <= 0) {
      expire();
    }
  }

  public void collect(@Nullable MatchPlayer killer) {
    if (killer != null) {
      killer.getMatch().sendMessage(Messages.bountyKilled(getTarget(), killer));

      // Give raindrops to the killerState
      Bukkit.getPluginManager()
          .callEvent(
              new PlayerEarnCurrencyEvent(
                  killer.getBukkit(),
                  CurrencyType.CUSTOM,
                  true,
                  Config.get().getKillReward(),
                  "Bounty Reward"));
    } else {
      player.getMatch().sendMessage(Messages.bountyDied(player));
    }

    manager.cancelExpireRunner();
    manager.startSelectionRunner();
  }

  public void expire() {
    player.getMatch().sendMessage(Messages.bountyOver(getTarget(), bountyKills));

    manager.cancelExpireRunner();
    manager.startSelectionRunner();
  }

  public void cancel() {
    Bukkit.getScheduler().cancelTask(taskId);
  }

  private void spawnParticles() {
    if (!player.getBukkit().isOnline()) return;

    Location location = player.getLocation();
    player
        .getWorld()
        .spigot()
        .playEffect(
            location.clone().add(0, 1, 0),
            Effect.FLAME,
            0,
            0,
            0.15f, // radius on each axis of the particle ball
            1f,
            0.15f,
            0.05f, // initial horizontal velocity
            10, // number of particles
            200); // radius in blocks to show particles;
  }

  public MatchPlayer getTarget() {
    return this.player;
  }

  public void logKill() {
    bountyKills++;

    // Killer was the target ive raindrops to the bounty target
    Bukkit.getPluginManager()
        .callEvent(
            new PlayerEarnCurrencyEvent(
                player.getBukkit(),
                CurrencyType.CUSTOM,
                true,
                Config.get().getSurvivalReward(),
                "Bounty Reward"));
  }
}
