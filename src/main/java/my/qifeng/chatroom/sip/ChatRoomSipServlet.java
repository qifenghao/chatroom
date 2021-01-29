package my.qifeng.chatroom.sip;

import javax.servlet.ServletException;
import javax.servlet.sip.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChatRoomSipServlet extends SipServlet {
    /**
     *  Context attribute key to store user list.
     */
    public static final String CHATROOM_USER_LIST_KEY = "chatroom.userList";

    public static final String CHATROOM_SERVER_NAME_KEY = "chatroomserver.name";
    public static final String CHATROOM_SERVER_PORT_KEY = "chatroomserver.port";

    /**
     *  This chat room server's address.
     *  eg. sip:chatroomname@192.168.0.165:5080
     */
    public String serverAddress;

    /**
     *  This is called by the container when starting up the service.
     */
    public void init() throws ServletException {
        super.init(); // TODO Remove
        getServletContext().setAttribute(CHATROOM_USER_LIST_KEY, new ArrayList());
        String serverName = getServletConfig().getInitParameter(CHATROOM_SERVER_NAME_KEY);
        int serverPort = Integer.parseInt(getServletConfig().getInitParameter(CHATROOM_SERVER_PORT_KEY));
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new ServletException(e);
        }
        serverAddress = String.format("sip:%s@%s:%d", serverName, ip, serverPort);
    }

    /**
     *  This is called by the container when shutting down the service.
     */
    public void destroy() {
        try
        {
            sendToAll(serverAddress, "Server is shutting down -- goodbye!");
        } catch (Throwable e)
        { //ignore all errors when shutting down.
            e.printStackTrace();
        }
        super.destroy();
    }

    /** This is called by the container when a MESSAGE message arrives. */
    protected void doMessage(SipServletRequest request) throws
            ServletException, IOException {

        request.createResponse(SipServletResponse.SC_OK).send();

        String message = request.getContent().toString();
        String from = request.getFrom().toString();

        //A user asked to quit.
        if(message.equalsIgnoreCase("/quit")) {
            sendToUser(from, "Bye");
            removeUser(from);
            return;
        }

        //Add user to the list
        if(!containsUser(from)) {
            sendToUser(from, "Welcome to chatroom " + serverAddress +
                    ". Type '/quit' to exit.");
            addUser(from);
        }

        //If the user is joining the chat room silently, no message
        //to broadcast, return.
        if(message.equalsIgnoreCase("/join")) {
            return;
        }

        //We could implement more IRC commands here,
        //see http://www.mirc.com/cmds.html
        sendToAll(from, message);
    }

    /**
     * This is called by the container when an error is received
     * regarding a sent message, including timeouts.
     */
    protected void doErrorResponse(SipServletResponse response)
            throws ServletException, IOException {
        super.doErrorResponse(response);
        //The receiver of the message probably dropped off. Remove
        //the receiver from the list.
        String receiver = response.getTo().toString();
        removeUser(receiver);
    }

    /**
     * This is called by the container when a 2xx-OK message is
     * received regarding a sent message.
     */
    protected void doSuccessResponse(SipServletResponse response)
            throws ServletException, IOException {
        super.doSuccessResponse(response);
        //We created the app session, we have to destroy it too.
        response.getApplicationSession().invalidate();
    }

    private void sendToAll(String from, String message)
            throws ServletParseException, IOException {
        SipFactory factory = (SipFactory)getServletContext().
                getAttribute("javax.servlet.sip.SipFactory");

        List list = (List)getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        Iterator users = list.iterator();
        while (users.hasNext()) { //Send this message to all on the list.
            String user = (String) users.next();

            SipApplicationSession session =
                    factory.createApplicationSession();
            SipServletRequest request = factory.createRequest(session,
                    "MESSAGE", serverAddress, user);
            String msg = from + " sent message: \n" + message;
            request.setContent(msg.getBytes(), "text/plain");
            request.send();
        }
    }

    private void sendToUser(String to, String message)
            throws ServletParseException, IOException {
        SipFactory factory = (SipFactory)getServletContext().
                getAttribute("javax.servlet.sip.SipFactory");
        SipApplicationSession session = factory.createApplicationSession();
        SipServletRequest request = factory.createRequest(session,
                "MESSAGE", serverAddress, to);
        request.setContent(message.getBytes(), "text/plain");
        request.send();
    }

    private boolean containsUser(String from) {
        List list = (List)getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        return list.contains(from);
    }

    private void addUser(String from) {
        List list = (List)getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        list.add(from);
    }

    private void removeUser(String from) {
        List list = (List)getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        list.remove(from);
    }
}
