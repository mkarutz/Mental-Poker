package au.edu.unimelb.mentalpoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static au.edu.unimelb.mentalpoker.PokerGame.PlayerAction.Type.BET;
import static au.edu.unimelb.mentalpoker.PokerGame.PlayerAction.Type.CHECK;
import static au.edu.unimelb.mentalpoker.PokerGame.PlayerAction.Type.FOLD;

public class PokerGame extends Thread {
    private static final int INITIAL_BALANCE = 1000000;
    private static final int CARDS_PER_HAND = 2;

    private final MentalPokerEngine poker;
    private final PeerNetwork network;

    private int potAmount = 0;
    private List<PlayerInfo> playerInfoList;

    private static class PlayerInfo {
        boolean isFolded;
        int balance;
        int stake;

        public PlayerInfo(int balance) {
            this(false /* isFolded */, balance, 0 /* stake */);
        }

        public PlayerInfo(boolean isFolded, int balance, int stake) {
            this.isFolded = isFolded;
            this.balance = balance;
            this.stake = stake;
        }

        public boolean isFolded() {
            return isFolded;
        }

        public boolean canPlay() {
            return balance > 0;
        }
    }

    public static class PlayerAction {
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
            return new PlayerAction(CHECK, 0);
        }

        public static PlayerAction fold() {
            return new PlayerAction(FOLD, 0);
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
        this.poker = poker;
    }

    @Override
    public void run() {
        poker.init();

        init();
        playHand();

        try {
            poker.finish();
            System.out.println("Game finished successfully.");
        } catch (CheatingDetectedException e) {
            System.out.println("Cheating detected.");
        }
    }

    private void init() {
        playerInfoList = new ArrayList<>(poker.getNumPlayers());
        for (int i = 0; i < poker.getNumPlayers(); i++) {
            playerInfoList.add(new PlayerInfo(INITIAL_BALANCE));
        }
    }

    private void playHand() {
        potAmount = 0;
        doDealing();

        doBetting();

        if (maybePayWinnings()) {
            return;
        }

        poker.drawPublic();
        poker.drawPublic();
        poker.drawPublic();
        doBetting();

        if (maybePayWinnings()) {
            return;
        }

        poker.drawPublic();
        doBetting();

        if (maybePayWinnings()) {
            return;
        }

        poker.drawPublic();
        doBetting();

        if (maybePayWinnings()) {
            return;
        }

//        checkWinner();
    }

    private boolean maybePayWinnings() {
        int numPlayersPlaying = 0;
        for (PlayerInfo playerInfo : playerInfoList) {
            if (!playerInfo.isFolded()) {
                numPlayersPlaying++;
            }
        }

//        if (numPlayersPlaying)

        return false;
    }

    private void doDealing() {
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

    private void doBetting() {
        int minBet = 0;

        for (int i = 0; i < poker.getNumPlayers(); i++) {
            final PlayerInfo playerInfo = playerInfoList.get(i);
            final int playerId = i + 1;

            if (playerInfo.isFolded()) {
                continue;
            }

            PlayerAction action;
            if (playerId == network.getLocalPlayerId()) {
                action = getPlayerAction(minBet);
//                sendPlayerAction(action);
            } else {
//                action = receivePlayerAction(playerId);
                return;
            }

            if (action.getActionType() == BET) {
                playerInfo.balance -= action.getBetAmount();
                playerInfo.stake += action.getBetAmount();
                minBet = action.getBetAmount();
            } else if (action.getActionType() == CHECK) {
                assert(minBet == 0);
            } else if (action.getActionType() == FOLD) {
                playerInfo.isFolded = true;
            }
        }
    }

    private PlayerAction getPlayerAction(int minBet) {
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            if (minBet == 0) {
                System.out.printf("Bet, Check or Fold (b/c/f)? ");
                System.out.flush();
            } else {
                System.out.printf("Min bet is $%d. Bet or Fold (b/f)? ", minBet);
                System.out.flush();
            }

            String cmd = scanner.nextLine();
            if ("b".equals(cmd)) {
                return PlayerAction.bet(getBetAmount(minBet));
            } else if ("c".equals(cmd)) {
                if (minBet == 0) {
                    return PlayerAction.check();
                }
            } else if ("f".equals(cmd)) {
                return PlayerAction.fold();
            }
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
                if (amount == balance || amount >= minBet && amount <= balance) {
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

    private void displayHand() {
        final StringBuilder sb = new StringBuilder("Your hand: ");
        for (Card card : poker.getLocalPlayerCards()) {
            sb.append(card);
            sb.append(" ");
        }
        System.out.println(sb);
    }
}
