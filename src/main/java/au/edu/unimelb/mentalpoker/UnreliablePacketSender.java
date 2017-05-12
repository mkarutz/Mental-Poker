package au.edu.unimelb.mentalpoker;

import java.util.Random;

/**
 * Created by azable on 12/05/17.
 */
public class UnreliablePacketSender implements IPacketSender {

    private Random random;
    private IPacketSender sender;

    private class EvilDispatcher extends Thread {
        private Address destination;
        private Proto.NetworkPacket packet;

        private double sendChance = 1.0;
        private double duplicateChance = 0.0;
        private long maxLatency = 0;

        public EvilDispatcher(Address destination, Proto.NetworkPacket packet) {
            this.destination = destination;
            this.packet = packet;
        }

        public void SetLevelsOfMalevolence(double sendChance, double duplicateChance, long maxLatency) {
            this.sendChance = sendChance;
            this.duplicateChance = duplicateChance;
            this.maxLatency = maxLatency;
        }

        public void run() {
            try {
                Thread.sleep(Math.abs(random.nextLong()) % (maxLatency + 1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (random.nextDouble() < this.sendChance) {
                sender.sendPacket(this.destination, this.packet);
                if (random.nextDouble() < this.duplicateChance) {
                    sender.sendPacket(this.destination, this.packet);
                }
            }
        }
    }

    public UnreliablePacketSender(int seed, IPacketSender reliableSender) {
        this.sender = reliableSender;
        this.random = new Random(seed);
    }

    @Override
    public void sendPacket(Address destination, Proto.NetworkPacket packet) {
        EvilDispatcher d = new EvilDispatcher(destination, packet);
        d.SetLevelsOfMalevolence(0.8, 0.5, 500);
        d.start();
    }
}
