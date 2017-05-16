package au.edu.unimelb.mentalpoker;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static au.edu.unimelb.mentalpoker.PokerGame.PlayerAction.Type.BET;
import static au.edu.unimelb.mentalpoker.PokerGame.PlayerAction.Type.CHECK;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.lang.Math.max;
import static java.lang.Math.min;


public class PokerGame extends Thread implements PeerNetwork.Callbacks {
    private static final int INITIAL_BALANCE = 1000000;
    private static final int CARDS_PER_HAND = 2;

    private final MentalPokerEngine poker;
    private final PeerNetwork network;

    @VisibleForTesting List<PlayerInfo> playerInfoList;

    @Override
    public void onTimeOut(int playerId) {
        System.out.println("\n\nError: Timed out reaching player " + playerId);
        System.exit(1);
    }

    @VisibleForTesting static class PlayerInfo {
        final int playerId;
        boolean isFolded;
        int balance;
        int stake;

        public PlayerInfo(int playerId, int balance) {
            this(playerId, false /* isFolded */, balance, 0 /* stake */);
        }

        public PlayerInfo(int playerId, boolean isFolded, int balance, int stake) {
            this.playerId = playerId;
            this.isFolded = isFolded;
            this.balance = balance;
            this.stake = stake;
        }

        public int getPlayerId() {
            return playerId;
        }

        public boolean isFolded() {
            return isFolded;
        }

        public boolean isAllIn() {
            return stake > 0 && balance == 0;
        }

        public boolean canPlay() {
            return balance > 0;
        }

        public boolean isBankrupt() {
            return stake == 0 && balance == 0;
        }
    }

    public static class PlayerAction {
        private static final PlayerAction FOLD = new PlayerAction(Type.FOLD, 0);
        private static final PlayerAction CHECK = new PlayerAction(Type.CHECK, 0);

        private final Type actionType;
        private final int betAmount;

        public enum Type {
            FOLD,
            CHECK,
            BET;
        }

        private PlayerAction(Type actionType, int betAmount) {
            this.actionType = actionType;
            this.betAmount = betAmount;
        }

        public static PlayerAction bet(int betAmount) {
            return new PlayerAction(BET, betAmount);
        }

        public static PlayerAction check() {
            return CHECK;
        }

        public static PlayerAction fold() {
            return FOLD;
        }

        public Type getActionType() {
            return actionType;
        }

        public int getBetAmount() {
            return betAmount;
        }
    }

    public PokerGame(PeerNetwork network, MentalPokerEngine poker) {
        this.network = network;
        this.network.setListener(this);
        this.poker = poker;
    }

    @Override
    public void run() {
        try {
            init();

            while (!gameOver()) {
                playHand();
            }

            try {
                poker.finish();
                System.out.println("Game finished successfully.");
                System.out.println("Player " + getGameWinner().getPlayerId() + " wins.");
            } catch (CheatingDetectedException e) {
                System.out.println("Cheating detected.");
            }
        } catch (TimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean gameOver() {
        int numPositiveBalance = 0;
        for (PlayerInfo player : playerInfoList) {
            if (!player.isBankrupt()) {
                numPositiveBalance++;
            }
        }
        return numPositiveBalance <= 1;
    }

    private PlayerInfo getGameWinner() {
        for (PlayerInfo player : playerInfoList) {
            if (!player.isBankrupt()) {
                return player;
            }
        }
        throw new RuntimeException();
    }

    private void init() throws TimeoutException {
        System.out.println("Shuffling...");
        poker.init();

        playerInfoList = new ArrayList<>(poker.getNumPlayers());
        for (int i = 0; i < poker.getNumPlayers(); i++) {
            playerInfoList.add(new PlayerInfo(i + 1 /* playerId */, INITIAL_BALANCE));
        }
    }

    private void playHand() throws TimeoutException {
        resetHand();
        doDealing();

        display();
        if (doBetting()) {
            poker.rake();
            return;
        }

        poker.drawPublic();
        poker.drawPublic();
        poker.drawPublic();

        display();
        if (doBetting()) {
            poker.rake();
            return;
        }

        poker.drawPublic();

        display();
        if (doBetting()) {
            poker.rake();
            return;
        }

        poker.drawPublic();

        display();
        if (doBetting()) {
            poker.rake();
            return;
        }

        openCards();
        distribute();

        poker.rake();
    }

    private void openCards() throws TimeoutException {
        for (PlayerInfo player : playerInfoList) {
            if (player.isFolded) {
                continue;
            }

            poker.open(player.playerId);
            System.out.println("Player " + player.playerId + " had: " + getPlayerHand(player).toString());
        }
    }

    /**
     * Distributes chips to players at the end of a hand.
     *
     * <p>Handles split pots, side pots, all-in situations, hand comparison and folded players.
     */
    @VisibleForTesting void distribute() {
        List<PlayerInfo> players = Lists.newArrayList(filter(playerInfoList, p -> p.stake > 0));

        while (players.size() > 1) {
            final int minStake = getMinStake(players);
            final int pot = minStake * players.size();

            final List<PlayerInfo> playersNotFolded = Lists.newArrayList(filter(players, p -> !p.isFolded));

            List<PlayerInfo> winners;
            if (playersNotFolded.size() == 1) {
                winners = playersNotFolded;
            } else if (playersNotFolded.isEmpty()) {
                winners = players;
            } else {
                final List<PokerHand> hands = Lists.newArrayList(transform(playersNotFolded, this::getPlayerHand));
                final PokerHand bestHand = Collections.max(hands);
                winners = Lists.newArrayList(filter(playersNotFolded, p -> getPlayerHand(p).compareTo(bestHand) == 0));
            }

            for (PlayerInfo winner : winners) {
                winner.balance += pot / winners.size();
                System.out.printf("Player %d gets $%d\n", winner.playerId, pot / winners.size());
            }

            for (PlayerInfo playerInfo : players) {
                playerInfo.stake -= minStake;
            }

            players = Lists.newArrayList(filter(players, p -> p.stake > 0));
        }

        if (players.size() == 1) {
            PlayerInfo player = players.get(0);
            player.balance += player.stake;
            player.stake = 0;
        }
    }

    private PokerHand getPlayerHand(PlayerInfo playerInfo) {
        final List<Card> publicCards = poker.getPublicCards();
        final List<Card> playerCards = poker.getPlayerHand(playerInfo.getPlayerId()).getOpenCards();
        return PokerUtils.getPlayerBestHand(playerCards, publicCards);
    }

    private void resetHand() {
        for (PlayerInfo playerInfo : playerInfoList) {
            playerInfo.stake = 0;
            playerInfo.isFolded = false;
        }
    }

    private int getPotAmount() {
        int result = 0;
        for (PlayerInfo playerInfo : playerInfoList) {
            result += playerInfo.stake;
        }
        return result;
    }

    private void display() {
        System.out.println("Pot amount is $" + getPotAmount());
        displayPlayerHand();
        displayPublicCards();
    }

    private void displayPlayerHand() {
        final List<Card> playerHand = poker.getLocalPlayerCards();
        final StringBuilder sb = new StringBuilder("Your cards: ");
        for (Card card : playerHand) {
            sb.append(card);
            sb.append(" ");
        }
        System.out.println(sb);
    }

    private void displayPublicCards() {
        final List<Card> cards = poker.getPublicCards();
        final StringBuilder sb = new StringBuilder("Table cards: ");
        for (Card card : cards) {
            sb.append(card);
            sb.append(" ");
        }
        System.out.println(sb);
    }

    private void doDealing() throws TimeoutException {
        for (int i = 0; i < poker.getNumPlayers(); i++) {
            final PlayerInfo playerInfo = playerInfoList.get(i);
            final int playerId = i + 1;

            if (!playerInfo.canPlay()) {
                continue;
            }

            for (int j = 0; j < CARDS_PER_HAND; j++) {
                poker.draw(playerId);
            }
        }
    }

    private boolean doBetting() throws TimeoutException {
        // Get initial player bets
        for (PlayerInfo player : playerInfoList) {
            if (player.isFolded() || player.isBankrupt() || player.isAllIn()) {
                continue;
            }

            PlayerAction action = getBetCheckFoldAction(player);

            switch (action.getActionType()) {
                case BET:
                    System.out.printf("Player %d bet $%d\n", player.getPlayerId(), action.getBetAmount());
                    player.balance -= action.getBetAmount();
                    player.stake += action.getBetAmount();
                    break;
                case CHECK:
                    System.out.printf("Player %d checked\n", player.getPlayerId());
                    break;
                case FOLD:
                    System.out.printf("Player %d folded\n", player.getPlayerId());
                    player.isFolded = true;
                    if (maybePayWinner()) {
                        return true;
                    }
                    break;
            }
        }

        // Allow players to call max bet if needed.
        for (PlayerInfo player : playerInfoList) {
            if (player.stake == getMaxStake()) {
                break;
            }

            if (player.isFolded() || player.isBankrupt() || player.isAllIn()) {
                continue;
            }

            PlayerAction action = getCallFoldAction(player);

            switch (action.getActionType()) {
                case BET:
                    System.out.printf("Player %d called\n", player.getPlayerId());
                    player.balance -= action.getBetAmount();
                    player.stake += action.getBetAmount();
                    break;
                case FOLD:
                    System.out.printf("Player %d folded\n", player.getPlayerId());
                    player.isFolded = true;
                    if (maybePayWinner()) {
                        return true;
                    }
                    break;
                default:
                    throw new RuntimeException();
            }
        }

        return false;
    }

    private PlayerAction getBetCheckFoldAction(PlayerInfo player) throws TimeoutException {
        final int amountToCall = min(player.balance, max(0, getMaxStake() - player.stake));
        PlayerAction action;
        if (player.getPlayerId() == network.getLocalPlayerId()) {
            System.out.println("It's your turn:");
            action = promptUserBetCheckFold(amountToCall);
            broadcastPlayerAction(action);
        } else {
            action = receivePlayerAction(player.getPlayerId());
        }
        return action;
    }

    private PlayerAction promptUserBetCheckFold(int amountToCall) {
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            if (amountToCall == 0) {
                // No bets yet
                System.out.printf("Bet, Check or Fold (b/c/f)? ");
                System.out.flush();
            } else {
                // Player must call or fold
                System.out.printf("$%d to call. Bet or Fold (b/f)? ", amountToCall);
                System.out.flush();
            }

            String cmd = scanner.nextLine();
            if ("b".equals(cmd)) {
                return PlayerAction.bet(getBetAmount(amountToCall));
            } else if ("c".equals(cmd)) {
                if (amountToCall == 0) {
                    return PlayerAction.check();
                }
            } else if ("f".equals(cmd)) {
                return PlayerAction.fold();
            }
        }
    }

    private PlayerAction getCallFoldAction(PlayerInfo player) throws TimeoutException {
        final int amountToCall = min(player.balance, max(0, getMaxStake() - player.stake));
        PlayerAction action;
        if (player.getPlayerId() == network.getLocalPlayerId()) {
            System.out.println("It's your turn:");
            action = promptUserCallFoldAction(amountToCall);
            broadcastPlayerAction(action);
        } else {
            action = receivePlayerAction(player.getPlayerId());
        }
        return action;
    }

    private PlayerAction promptUserCallFoldAction(int amountToCall) {
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf("$%d to call. Call or Fold (c/f)? ", amountToCall);
            System.out.flush();

            String cmd = scanner.nextLine();
            if ("c".equals(cmd)) {
                return PlayerAction.bet(amountToCall);
            } else if ("f".equals(cmd)) {
                return PlayerAction.fold();
            }
        }
    }

    private int getMaxStake() {
        int result = 0;
        for (PlayerInfo playerInfo : playerInfoList) {
            result = max(result, playerInfo.stake);
        }
        return result;
    }

    private int getMinStake(Iterable<PlayerInfo> players) {
        int result = Integer.MAX_VALUE;
        for (PlayerInfo playerInfo : players) {
            result = min(result, playerInfo.stake);
        }
        return result;
    }

    private boolean maybePayWinner() {
        if (getNumPlayersStillIn() == 1) {
            distribute();
            return true;
        }
        return false;
    }

    private int getNumPlayersStillIn() {
        int result = 0;
        for (PlayerInfo playerInfo : playerInfoList) {
            if (!playerInfo.isFolded()) {
                result++;
            }
        }
        return result;
    }

    private void broadcastPlayerAction(PlayerAction playerAction) throws TimeoutException {
        Proto.PlayerActionMessage.Type actionType;
        if (playerAction.getActionType() == BET) {
            actionType = Proto.PlayerActionMessage.Type.BET;
        } else if (playerAction.getActionType() == CHECK) {
            actionType = Proto.PlayerActionMessage.Type.CHECK;
        } else {
            actionType = Proto.PlayerActionMessage.Type.FOLD;
        }

        Proto.NetworkMessage msg =
                Proto.NetworkMessage.newBuilder()
                        .setType(Proto.NetworkMessage.Type.PLAYER_ACTION)
                        .setPlayerActionMessage(
                                Proto.PlayerActionMessage.newBuilder()
                                        .setType(actionType)
                                        .setBetAmount(playerAction.getBetAmount()))
                        .build();

        network.broadcast(msg);
    }

    private PlayerAction receivePlayerAction(int playerId) throws TimeoutException {
        Proto.NetworkMessage msg = network.receive(playerId);
        Proto.PlayerActionMessage actionMessage = msg.getPlayerActionMessage();

        if (actionMessage.getType() == Proto.PlayerActionMessage.Type.BET) {
            return PlayerAction.bet(actionMessage.getBetAmount());
        } else if (actionMessage.getType() == Proto.PlayerActionMessage.Type.CHECK) {
            return PlayerAction.check();
        } else {
            return PlayerAction.fold();
        }
    }

    private int getBetAmount(int minBet) {
        final int balance = getLocalPlayerBalance();
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.printf("You have $%d in chips. How much do you want to bet? ", getLocalPlayerBalance());
            System.out.flush();

            try {
                int amount = Integer.parseInt(scanner.nextLine());
                if (amount >= minBet && amount <= balance) {
                    return amount;
                }
            } catch (NumberFormatException e) {
                // Just prompt the user again.
            }
        }
    }

    private int getLocalPlayerBalance() {
        return playerInfoList.get(network.getLocalPlayerId() - 1).balance;
    }
}
