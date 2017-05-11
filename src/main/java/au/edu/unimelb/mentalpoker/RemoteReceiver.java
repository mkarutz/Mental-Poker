package au.edu.unimelb.mentalpoker;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Created by azable on 11/05/17.
 */
public class RemoteReceiver extends Thread {

    private Remote.Callbacks remoteListener;
    private Queue<IncomingMessage> incomingMessages;

    private class IncomingMessage {
        public Address source;
        public byte[] message;

        public IncomingMessage(Address source, byte[] message) {
            this.source = source;
            this.message = message;
        }
    }

    public RemoteReceiver() {
        this.incomingMessages = new ArrayDeque<>();
    }

    public synchronized void setListener(Remote.Callbacks listener) {
        this.remoteListener = listener;
    }

    public void run() {
        while (true) {
            processIncomingMessage();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void pushIncomingMessage(Address source, byte[] message) {
        this.incomingMessages.add(new IncomingMessage(source, message));
    }

    private void processIncomingMessage() {
        //System.out.println(this.incomingMessages.isEmpty());
        if (!this.incomingMessages.isEmpty()) {
            IncomingMessage next = this.incomingMessages.remove();
            System.out.println("Proc");
            System.out.flush();
            try {
                Proto.NetworkMessage networkMessage = Proto.NetworkMessage.parseFrom(next.message);
                if (this.remoteListener != null) {
                    //System.out.println("Calling onReceive");
                    this.remoteListener.onReceive(next.source, networkMessage);
                }
            } catch (InvalidProtocolBufferException e) {
                System.out.println("Error: Invalid protobuf message");
            }
        }
    }
}
