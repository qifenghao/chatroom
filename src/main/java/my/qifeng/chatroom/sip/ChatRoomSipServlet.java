package my.qifeng.chatroom.sip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.sip.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomSipServlet extends SipServlet {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomSipServlet.class);

    private SipFactory factory;

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
        factory = (SipFactory) getServletContext().getAttribute(SipServlet.SIP_FACTORY);
        getServletContext().setAttribute(CHATROOM_USER_LIST_KEY, new ArrayList<String>());

        String serverName = getServletConfig().getInitParameter(CHATROOM_SERVER_NAME_KEY);
        int serverPort = Integer.parseInt(getServletConfig().getInitParameter(CHATROOM_SERVER_PORT_KEY));
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
        serverAddress = String.format("sip:%s@%s:%d", serverName, ip, serverPort);
        logger.info("Chatroom Server Address: {}", serverAddress);
    }

    /**
     *  This is called by the container when shutting down the service.
     */
    public void destroy() {
        try {
            sendToAll(serverAddress, "Server is shutting down -- goodbye!");
        } catch (Throwable e) {
            // Ignore all errors when shutting down.
            e.printStackTrace();
        }
    }

    /**
     *  This is called by the container when a MESSAGE message arrives.
     */
    protected void doMessage(SipServletRequest request) throws ServletException, IOException {

        request.createResponse(SipServletResponse.SC_OK).send();

        String message = request.getContent().toString();
        String from = request.getFrom().toString();

        logger.info("message: {}, from: {}", message, from);

        // A user asked to quit.
        if("/quit".equalsIgnoreCase(message)) {
            sendToUser(from, "Bye");
            removeUser(from);
            return;
        }

        // Add user to the list
        if(!containsUser(from)) {
            sendToUser(from, "Welcome to chatroom " + serverAddress + ". Type '/quit' to exit.");
            addUser(from);
        }

        // If the user is joining the chat room silently, no message to broadcast.
        if("/join".equalsIgnoreCase(message)) {
            return;
        }

        // Broadcast
        sendToAll(from, message);
    }

    /**
     * This is called by the container when an error is received
     * regarding a sent message, including timeouts.
     */
    protected void doErrorResponse(SipServletResponse response) throws ServletException, IOException {
        // The receiver of the message probably dropped off. Remove the receiver from the list.
        String receiver = response.getTo().toString();
        removeUser(receiver);
    }

    /**
     * This is called by the container when a 2xx-OK message is
     * received regarding a sent message.
     */
    protected void doSuccessResponse(SipServletResponse response) throws ServletException, IOException {
        // We created the app session, we have to destroy it too.
        response.getApplicationSession().invalidate();
    }

    @SuppressWarnings("unchecked")
    private void sendToAll(String from, String message) throws ServletParseException, IOException {
        List<String> userList = (List<String>) getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        for (String user : userList) {
            String msg = from + " sent message: \n" + message;
            sendToUser(user, msg);
        }
    }

    private void sendToUser(String to, String message) throws ServletParseException, IOException {
        SipApplicationSession session = factory.createApplicationSession();
        SipServletRequest request = factory.createRequest(session, "MESSAGE", serverAddress, to);
        request.setContent(message.getBytes(), "text/plain");
        request.send();
    }

    @SuppressWarnings("unchecked")
    private boolean containsUser(String from) {
        List<String> list = (List<String>) getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        return list.contains(from);
    }

    @SuppressWarnings("unchecked")
    private void addUser(String from) {
        List<String> list = (List<String>) getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        list.add(from);
    }

    @SuppressWarnings("unchecked")
    private void removeUser(String from) {
        List<String> list = (List<String>) getServletContext().getAttribute(CHATROOM_USER_LIST_KEY);
        list.remove(from);
    }
}
