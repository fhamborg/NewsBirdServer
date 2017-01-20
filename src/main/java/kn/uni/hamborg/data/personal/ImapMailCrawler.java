/*
 * Author: Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
package kn.uni.hamborg.data.personal;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import kn.uni.hamborg.data.light.LightDoc;

/**
 * http://www.hascode.com/2010/04/snippet-simple-one-minute-imap-client/
 *
 * @author Felix Hamborg <felix.hamborg@uni-konstanz.de>
 */
public class ImapMailCrawler {

    private static final Logger LOG = Logger.getLogger(ImapMailCrawler.class.getSimpleName());

    private Session session = null;
    private Store store = null;
    private String host = null;
    private String userName = null;
    private String password = null;

    public ImapMailCrawler(String host, String userName, String password) {
        this.host = host;
        this.userName = userName;
        this.password = password;
    }

    public boolean getMail() throws MessagingException {
        ArrayList<LightDoc> docs = new ArrayList<>();

        session = Session.getDefaultInstance(System.getProperties(), null);
//        session.setDebug(true);
        System.out.println("get store..");
        store = session.getStore("imaps");
        System.out.println("connect..");
        store.connect(this.host, this.userName, this.password);
        System.out.println("get default folder ..");
        Folder folder = store.getDefaultFolder();
        folder = folder.getFolder("inbox");
        folder.open(Folder.READ_ONLY);
        System.out.println("reading messages..");
        Message[] messages = folder.getMessages();
        System.out.println("got " + messages.length + " messages");
        int i = 0;
        for (Message m : messages) {
            try {
                System.out.println("" + i + ": " + m.getReceivedDate() + " \t" + m.getSubject());
                
               
                
              //  docs.add(new LightDoc(host, host, host, password, userName, host, null, password, host, i))
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }

        return false;
    }

    public static void main(String[] args) throws MessagingException {
        System.out.println("provide password: ");
        String password = new Scanner(System.in).nextLine();
        ImapMailCrawler c = new ImapMailCrawler("imap.gmail.com", "felixhamborg@gmail.com",
                password
        );
        c.getMail();
    }
}
