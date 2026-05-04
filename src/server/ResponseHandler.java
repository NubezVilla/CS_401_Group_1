package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import model.ResponseType;
import model.Wrapper;

/*** 
 * This class is meant to handle responses from the Client.
 * The server needs to know if it received the data that the Server
 * is trying to send. 
 * After I send the data, I need to wait for the Client to acknowledge that 
 * it recieved the data.
 * If it did, do nothing return to listening.
 * If it did not, retry sending the data to the Client.
 * DO NOT use a while loop because it can loop forever.
 ***/
public class ResponseHandler {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static final int MAX_ATTEMPTS = 3;

    //i'm going to have to read and write from the stream
    //I'll need a constructor that can take the streams
    public ResponseHandler(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
    }

    //I need a way to send the data a number of times
    //I have to make sure that I get the expected ResponseType as well
    public boolean sendWithRetry(Wrapper dataToSend, ResponseType expectedResponse) {
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            try {
                out.writeObject(dataToSend);
                out.flush();

                Wrapper clientResponse = (Wrapper) in.readObject();

                if (clientResponse.getResponseType() == expectedResponse) {
                    return true;
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            attempts++;
        }

        return false;
    }
}
