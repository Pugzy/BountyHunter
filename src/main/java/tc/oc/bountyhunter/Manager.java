package tc.oc.bountyhunter;

import java.util.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;
import tc.oc.bountyhunter.runners.BountyRunner;
import tc.oc.bountyhunter.runners.SelectionRunner;
import tc.oc.occ.dispense.events.currency.PlayerEarnCurrencyEvent;
import tc.oc.pgm.api.match.Match;
import tc.oc.pgm.api.match.event.MatchFinishEvent;
import tc.oc.pgm.api.match.event.MatchLoadEvent;
import tc.oc.pgm.api.match.event.MatchStartEvent;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.api.player.ParticipantState;
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent;
import tc.oc.pgm.events.PlayerParticipationStopEvent;

public class Manager implements Listener {

  private Match match;
  private @Nullable BountyRunner bountyRunner;
  private @Nullable SelectionRunner selectionRunner;

  public Manager() {}

  public @Nullable MatchPlayer getTarget() {
    if (bountyRunner == null) return null;

    return bountyRunner.getTarget();
  }

  @EventHandler
  public void onMatchLoad(MatchLoadEvent event) {
    this.match = event.getMatch();
  }

  @EventHandler
  public void onMatchStart(MatchStartEvent event) {
    if (!Config.get().getEnabled()) return;

    startSelectionRunner();
  }

  @EventHandler
  public void onMatchFinish(MatchFinishEvent event) {
    MatchPlayer target = getTarget();
    if (target != null) {
      event.getMatch().sendMessage(Messages.bountyOver(target));
    }

    cancelSelectionRunner();
    cancelExpireRunner();
  }

  @EventHandler
  public void onPlayerLeave(PlayerParticipationStopEvent event) {
    if (event.getPlayer().equals(getTarget())) {

      // Broadcast and start countdown
      event.getPlayer().getMatch().sendMessage(Messages.bountyLeft(event.getPlayer()));
      startSelectionRunner();
      if (bountyRunner != null) {
        bountyRunner.expire();
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onTargetKill(MatchPlayerDeathEvent event) {
    // No target currently active
    if (bountyRunner == null) return;

    MatchPlayer target = getTarget();
    if (target == null) return;

    boolean selfKill = event.getKiller() == null || event.isSelfKill();
    boolean teamKill = event.isTeamKill();

    // Killer was null or self
    if (selfKill) {
      if (event.getPlayer().equals(target)) {
        bountyRunner.collect(null);
        return;
      }
    }

    // Check player was killed by somebody
    ParticipantState killerState = event.getKiller();
    if (killerState == null) return;
    Optional<MatchPlayer> playerOptional = killerState.getPlayer();
    if (!playerOptional.isPresent()) return;
    MatchPlayer killer = playerOptional.get();

    // Dead player was the target
    if (event.getPlayer().equals(target)) {

      if (selfKill) {
        bountyRunner.collect(null);
        return;
      }

      if (teamKill) {
        killer.sendMessage(Messages.betrayalQuote());
        return;
      }

      bountyRunner.collect(killer);
      return;
    }

    if (teamKill) {
      killer.sendMessage(Messages.betrayalQuote());
      return;
    }

    if (target.equals(killer)) {
      bountyRunner.logKill();
    }
  }

  @EventHandler
  public void onRaindropEarn(PlayerEarnCurrencyEvent event) {
    if (!Config.get().isDebug()) return;
    event
        .getPlayer()
        .sendMessage(event.getCustomAmount() + " " + event.getReason() + " " + event.getReason());
  }

  public void startSelectionRunner() {
    cancelSelectionRunner();
    if (match == null || !match.isRunning()) return;
    selectionRunner = new SelectionRunner(this, match);
  }

  public void startExpireRunner(MatchPlayer player) {
    cancelExpireRunner();
    cancelSelectionRunner();

    if (player == null) return;
    bountyRunner =
        new BountyRunner(this, player, Utils.durationTicks(Config.get().getMaxDuration()));
  }

  private void cancelSelectionRunner() {
    if (selectionRunner != null) {
      selectionRunner.cancel();
      selectionRunner = null;
    }
  }

  public void cancelExpireRunner() {
    if (bountyRunner != null) {
      bountyRunner.cancel();
      bountyRunner = null;
    }
  }

  public void reload() {
    // Remove bounty target if not enabled.
    if (!Config.get().getEnabled()) {
      cancelSelectionRunner();
      cancelExpireRunner();
    } else {
      // Start selection countdown if none exist
      if (selectionRunner != null && bountyRunner != null) {
        startSelectionRunner();
      }
    }
  }
}
