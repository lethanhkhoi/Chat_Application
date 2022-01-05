package vn.edu.hcmus.student.sv19127186.Server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * vn.edu.hcmus.student.sv19127186.Server
 * Created by 84904
 * Date 22/12/2021 - 5:53 PM
 * Description: ...
 */
public class Server {
    private Object lock;
    private ServerSocket s;
    private Socket socket;
    static ArrayList<User> clients = new ArrayList<User>();

    public void load_data(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("account.txt"));
            String line = br.readLine();
            while(line!=null){
                String [] str=line.split(",");
                User newuser = new User(str[0],str[1],false,lock);
                clients.add(newuser);
                line=br.readLine();
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void save_data(){
        try{
            FileWriter fow = new FileWriter("account.txt");
            for(User user:clients){
                fow.write(user.getUsername()+","+user.getPassword()+"\n");
            }
            fow.close();
        }catch(IOException e){
            e.getMessage();
        }
    }

    public Server() throws IOException {
        try {
            lock = new Object();
            this.load_data();
            s = new ServerSocket(9000);

            while (true) {
                socket = s.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                String request = dis.readUTF();
                System.out.println("REQUEST: " + request);

                if (request.equals("Login")) {
                    // Yêu cầu đăng nhập từ user

                    String username = dis.readUTF();
                    String password = dis.readUTF();

                    if (isExisted(username) == true) {
                        for (User client : clients) {
                            if (client.getUsername().equals(username)) {
                                if (password.equals(client.getPassword())) {
                                    if (client.checkLogin() == false) {
                                        User newuser = client;
                                        newuser.connected(socket);
                                        newuser.updateStatus();


                                        dos.writeBoolean(true);
                                        dos.flush();

                                        // Tạo một Thread để giao tiếp với user này
                                        Thread t = new Thread(newuser);
                                        t.start();

                                        UpdateOnlineUsers();
                                    }
                                    else{
                                        dos.writeBoolean(false);
                                        dos.flush();
                                    }
                                }
                                else{
                                    dos.writeBoolean(false);
                                    dos.flush();
                                }
                                break;
                            }
                        }
                    }
                    else {
                        dos.writeBoolean(false);
                        dos.flush();
                    }
                }else if(request.equals("Register")==true){
                    String username = dis.readUTF();
                    String password = dis.readUTF();

                    // Kiểm tra tên đăng nhập có tồn tại hay không
                    if (isExisted(username) == false) {
                        User newuser = new User(username,password,true,lock);
                        clients.add(newuser);
                        newuser.connected(socket);
                        this.save_data();
                        dos.writeBoolean(true);
                        dos.flush();
                        Thread t = new Thread(newuser);
                        t.start();
                        UpdateOnlineUsers();
                    }
                    else{
                        dos.writeBoolean(false);
                    }
                }
            }
        }catch (Exception e){
            System.err.println(e);
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }
    public boolean isExisted(String username){
        for(User user:clients){
            if(user.getUsername().equals(username)==true)
            {
                return true;
            }
        }
            return false;
    }

    public static void UpdateOnlineUsers(){

        String mess="";
        for(User user:clients){
            if(user.checkLogin()==true){
               mess+=user.getUsername();
               mess+=",";
            }
        }
        for(User user:clients){
            if(user.checkLogin()==true){
                try{
                    user.getDos().writeUTF("UPDATE");
                    user.getDos().writeUTF(mess);
                    user.getDos().flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    public void close_server(){
        for(User users: clients){
            try {
                users.getDos().writeUTF("STOP");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
class User implements Runnable{
    private String username;
    private String password;
    private boolean status; //1 la online 0 la offline
    private Object lock;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Socket socket;

    User(String id,String pass,Boolean check,Object lock){
        username = id;
        password=pass;
        status=check;
        this.lock = lock;
    }

    public String getUsername(){
        return username;
    }
    public void updateStatus(){
        this.status=true;
    }
    public void connected(Socket s){
        this.socket=s;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPassword() {
        return password;
    }
    public boolean checkLogin(){
        return status;
    }

    public DataOutputStream getDos(){
        return dos;
    }
    @Override
    public void run() {
        while(true){
            try {
                String request = null;
                request=dis.readUTF();
                if(request.equals("Logout")){
                    dos.writeUTF("CLOSE");
                    dos.flush();
                    socket.close();
                    this.status = false;
                    Server.UpdateOnlineUsers();
                    break;
                }
                else if(request.equals("Text")){
                    String username_ = dis.readUTF();
                    String content = dis.readUTF();

                    for(User client : Server.clients){
                        if(client.getUsername().equals(username_)==true){
                            synchronized (lock) {
                                client.getDos().writeUTF("Text");
                                client.getDos().writeUTF(this.username);
                                client.getDos().writeUTF(content);
                                client.getDos().flush();
                                break;
                            }
                        }
                    }

                }
                else if (request.equals("FILE")){
                    String filename = dis.readUTF();
                    String username_ = dis.readUTF();
                    int size = dis.readInt();

                    int buffersize = 4096;
                    byte[] buffer = new byte[buffersize];

                    for(User client : Server.clients){
                        if(client.getUsername().equals(username_)==true){
                            synchronized (lock) {
                                client.getDos().writeUTF("FILE");
                                client.getDos().writeUTF(this.username);
                                client.getDos().writeUTF(filename);
                                client.getDos().writeInt(size);
                                int pos = 0;
                                while(size>0){
                                    dis.read(buffer,0,Math.min(size,buffersize));
                                    client.getDos().write(buffer,0,Math.min(size,buffersize));
                                    size-=buffersize;
                                }
                                client.getDos().flush();
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());

            }
        }
    }
}