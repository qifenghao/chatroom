package my.qifeng.chatroom.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ChatRoomServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.doGet(req, resp);
        System.out.println("====== Greeting from ChatRoom Web Console ======");
        PrintWriter writer = resp.getWriter();
        writer.println("ChatRoom Web Console");
        writer.close();
    }
}
