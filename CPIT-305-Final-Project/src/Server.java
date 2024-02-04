import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.ServerSocket;

public class Server {

    public static void main(String[] args) throws Exception {



        ServerSocket server = new ServerSocket(8800);

        System.out.println("Server waiting Connection...");
        while (true) {
            Socket client = server.accept();

            myThread m = new myThread(client);
            m.start();
        }
        //server.close();

    }
}

class myThread extends Thread {

    Socket client;

    public myThread(Socket client) {
        this.client = client;
    }


    @Override
    public void run() {
        String url = ""

        System.out.println("Client connect via: " + client.getInetAddress().getHostAddress());
        try(Connection con = DriverManager.getConnection(url);
            Statement statement = con.createStatement();) {
            try {
                Scanner in = new Scanner(client.getInputStream());
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                ObjectOutputStream objectOut = new ObjectOutputStream(client.getOutputStream());


                PrintWriter logs = new PrintWriter(".\\logs.txt");

                while (in.hasNextLine()) {
                    //logging in
                    String message = in.nextLine();

                    if (message.charAt(0) == '0') {
                        String username = message.substring(1);
                        int id = login(statement, username);
                        if (id != -1) {
                            out.println("logged in as " + username + id);
                            logs.println("logged in as " + username);

                        } else {
                            out.println("try again");
                        }
                    }

                    //logging out
                    else if (message.charAt(0) == '8') {
                        out.println("logged out");
                        logs.println("logged out from " + message);
                    }

                    //making a request
                    else if (message.charAt(0) == '1') {
                        String query = message.substring(1);

                        Statement insert = con.createStatement();
                        insert.executeUpdate(query);
                        logs.println("inserted with the following query: " + query);

                    }

                    //removing a request
                    else if (message.charAt(0) == '2') {
                        String query = message.substring(1);
                        Statement delete = con.createStatement();
                        delete.executeUpdate(query);
                        logs.println("deleted with the following query: " + query);

                    }

                    //view all requests
                    else if (message.charAt(0) == '3') {
                        String query = message.substring(1);
                        ResultSet rs = statement.executeQuery(query);
                        ArrayList<String> results = new ArrayList<String>();
                        while(rs.next()){
                            results.add("Request Num: " +  rs.getInt(1) +" | "+ rs.getString(4) + " requests " + rs.getInt(6));
                        }

                        logs.println("viewed all requests with the following query: " + query);
                        objectOut.writeObject(results);
                    }

                    //accept requests
                    else if (message.charAt(0) == '4') {
                        String query = message.substring(1);
                        Statement update = con.createStatement();
                        update.executeUpdate(query);

                        logs.println("accepted a request with the following query with the following query: " + query);
                    }


                    //give money
                    else if (message.charAt(0) == '5') {
                        String query = message.substring(1);

                        Statement update = con.createStatement();
                        update.execute(query);
                        logs.println("settled debt wtih the following query: " + query);
                    }

                    //owed list
                    else if (message.charAt(0) == '6') {
                        String query = message.substring(1);

                        ResultSet rs = statement.executeQuery(query);
                        ArrayList<String> results = new ArrayList<String>();
                        while(rs.next()){
                            results.add("Request Num: " +  rs.getInt(1) +" | "+ rs.getString(4) + " owes you " + rs.getInt(6));
                        }
                        logs.println("Viewed the owed list with the following query: " + query);
                        objectOut.writeObject(results);
                    }

                    //owing list
                    else if (message.charAt(0) == '7') {
                        String query = message.substring(1);

                        ResultSet rs = statement.executeQuery(query);
                        ArrayList<String> results = new ArrayList<String>();

                        while(rs.next()){
                            results.add("Request Num: " +  rs.getInt(1) +" | you owe "+ rs.getString(5) + " " + rs.getInt(6));
                        }
                        logs.println("viewed the owing list with the following query: " + query);
                        objectOut.writeObject(results);
                    }
                    logs.flush();


                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int login(Statement statement, String username) throws SQLException {
        String sql = "Select * from Usersinfo";
        ResultSet rs = statement.executeQuery(sql);

        while(rs.next()){

            if(rs.getString(2).equals(username)){
                System.out.println(rs.getInt(1));
                System.out.println(rs.getString(2));
                return rs.getInt(1);
            }
        }


        return -1;
    }

}



