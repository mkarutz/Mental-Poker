package au.edu.unimelb.mentalpoker;

import java.io.*;
import java.net.Socket;

/**
 * Created by azable on 9/05/17.
 */
public class Connection implements Runnable {
    private Socket endPoint;
    private InputStream inStream;
    private OutputStream outStream;
    private IConnectionListener listener;
    private boolean closed = false;
    private int localListenPort;
    private int remoteListenPort;

    public Connection(Socket endPoint, int localListenPort, IConnectionListener listener) throws IOException {
        this.listener = listener;
        this.localListenPort = localListenPort;
        this.endPoint = endPoint;
        this.inStream = endPoint.getInputStream();
        this.outStream = endPoint.getOutputStream();

        try {
            //System.out.println("Sending my local port as: " + localListenPort);
            WriteNext(Integer.toString(localListenPort).getBytes());

            byte[] otherListenPort = ReadNext();
            this.remoteListenPort = Integer.parseInt(new String(otherListenPort));
            //System.out.println("Received remote port as: " + remoteListenPort);
        } catch (Exception e) {
            System.out.println(e.toString());
            this.closed = true;
        }
    }

    public boolean Write(byte[] message) {
        try {
            WriteNext(message);
        } catch (Exception e) {
            //System.out.println(e);
            Close();
            return false;
        }
        return true;
    }

    public void run() {
        while (!this.closed) {
            try {
                byte[] next = ReadNext();
                this.listener.Receive(new Address(this.endPoint.getInetAddress().getHostAddress(), this.remoteListenPort), next);
            } catch (Exception e) {
                Close();
            }
        }
    }

    private void Close() {
        this.listener.ConnectionClosed(new Address(this.endPoint));
        this.closed = true;
    }

    private void WriteNext(byte[] message) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(this.outStream);
        dataOutputStream.writeInt(message.length);
        this.outStream.write(message);
        this.outStream.flush();
    }

    private byte[] ReadNext() throws IOException {
        while (this.inStream.available() == 0);
        DataInputStream dataInputStream = new DataInputStream(this.inStream);
        int bufferSize = dataInputStream.readInt();
        byte[] buffer = new byte[bufferSize];
        this.inStream.read(buffer);
        return buffer;
    }
}