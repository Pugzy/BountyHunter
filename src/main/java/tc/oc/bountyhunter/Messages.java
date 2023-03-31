package tc.oc.bountyhunter;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.title.Title.title;
import static tc.oc.pgm.util.TimeUtils.fromTicks;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.util.named.NameStyle;

public class Messages {

  public static Component bountySelected(MatchPlayer player) {
    return text("")
        .append(player.getName(NameStyle.SIMPLE_COLOR))
        .append(text(" has been selected as a bounty target.", NamedTextColor.YELLOW));
  }

  public static Title bountyTitle() {
    return title(
        text("Hunted", NamedTextColor.RED, TextDecoration.BOLD),
        text("You are the bounty target", NamedTextColor.YELLOW),
        Title.Times.of(fromTicks(20), fromTicks(60), fromTicks(20)));
  }

  public static Component bountyAllInfo() {
    int killReward = Config.get().getKillReward();
    return text("Take them out to receive a ")
        .append(text(killReward + " raindrop reward", NamedTextColor.AQUA))
        .append(text(".", NamedTextColor.YELLOW));
  }

  public static Component bountyPlayerInfo() {
    long seconds = Config.get().getMaxDuration().getSeconds();
    int reward = Config.get().getSurvivalReward();

    return text("You have been selected as the bounty target. Survive for ")
        .append(text(seconds + " seconds", NamedTextColor.GREEN))
        .append(text(". You receive "))
        .append(text(reward + " extra raindrops", NamedTextColor.AQUA))
        .append(text(" per kill."));
  }

  public static Component bountyOver(MatchPlayer player, int bountyKills) {
    String killMessage =
        "They survived and got " + bountyKills + " kill" + ((bountyKills != 1) ? "s" : "") + ".";

    return text("Bounty for ", NamedTextColor.YELLOW)
        .append(player.getName(NameStyle.SIMPLE_COLOR))
        .append(
            text(" has expired. " + killMessage, NamedTextColor.YELLOW)
                .append(newline())
                .append(text("A new target will be selected soon.", NamedTextColor.WHITE)));
  }

  public static Component bountyOver(MatchPlayer player) {
    return text("Bounty for ", NamedTextColor.YELLOW)
        .append(player.getName(NameStyle.SIMPLE_COLOR))
        .append(text(" has been cancelled.", NamedTextColor.YELLOW));
  }

  public static Component bountyLeft(MatchPlayer player) {
    return text("")
        .append(player.getName(NameStyle.SIMPLE_COLOR))
        .append(text(" left the match so bounty cancelled.", NamedTextColor.YELLOW));
  }

  public static Component bountyKilled(MatchPlayer target, MatchPlayer killer) {
    return text("Bounty on ", NamedTextColor.YELLOW)
        .append(target.getName(NameStyle.SIMPLE_COLOR))
        .append(text(" claimed by ", NamedTextColor.YELLOW))
        .append(killer.getName(NameStyle.SIMPLE_COLOR));
  }

  public static Component bountyDied(MatchPlayer player) {
    return text("Bounty reward for ", NamedTextColor.YELLOW)
        .append(player.getName(NameStyle.SIMPLE_COLOR))
        .append(
            text(" has been cancelled. ", NamedTextColor.YELLOW)
                .append(text("Player died.", NamedTextColor.RED)));
  }

  public static Component betrayalQuote() {
    Random random = new Random();
    List<String> quotes =
        Arrays.asList(
            "It's treason then.",
            "All men betray, all lose heart.",
            "The betrayal of a trust is the most poisonous of all actions.",
            "Betrayal is the deepest wound, the one that rarely heals.",
            "It is easier to forgive an enemy than to forgive a friend.",
            "The saddest thing about betrayal is that it never comes from your enemies, it comes from those you trust the most.");

    return text(quotes.get(random.nextInt(quotes.size())), NamedTextColor.GRAY);
  }
}
