package vn.edu.hcmus.student.sv19127186.Client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * vn.edu.hcmus.student.sv19127186.Client
 * Created by 84904
 * Date 22/12/2021 - 8:14 PM
 * Description: ...
 */
class Login extends JFrame implements ActionListener {
    private JTextField username_;
    private JTextField pass_;

    private Socket socket;
    private String host = "127.0.0.1";
    private int port = 9000;
    private DataInputStream dis;
    private DataOutputStream dos;

    Login()
    {
        setDefaultLookAndFeelDecorated(true);
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel ui = new JPanel(new BorderLayout());
        this.setMinimumSize(new Dimension(350,230));
        JPanel panel= new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx= 0;
        gbc.gridy=0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5,5,5,5);
        panel.add(new JLabel("Username: "),gbc);
        gbc.gridy++;
        panel.add(new JLabel("Password: "),gbc);
        gbc.gridy++;


        gbc.gridx++;
        gbc.gridy=0;
        gbc.anchor=GridBagConstraints.WEST;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        username_ = new JTextField(15);
        panel.add(username_,gbc);
        gbc.gridy++;
        pass_= new JTextField(15);
        panel.add(pass_,gbc);


        JPanel footerpanel = new JPanel();
        footerpanel.setLayout(new BoxLayout(footerpanel,BoxLayout.LINE_AXIS));
        JButton okbtn = new JButton("Login");
        okbtn.setActionCommand("LOGIN");
        JButton cancelbtn = new JButton("Register");
        cancelbtn.setActionCommand("REGISTER");

        footerpanel.add(okbtn);
        footerpanel.add(Box.createRigidArea(new Dimension(20,0)));
        footerpanel.add(cancelbtn);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.add(footerpanel);

        ui.add(panel);
        ui.add(panel2,BorderLayout.SOUTH);
        okbtn.addActionListener(this);
        cancelbtn.addActionListener(this);


        this.add(ui);

        this.pack();
        this.setSize(new Dimension(500,200));
        this.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command=="LOGIN")
        {
            this.Connect_socket();

            Boolean check = isLogin();

            if(check == true){
                this.hide();
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        ClientGui client = new ClientGui(username_.getText(),dis,dos);
                    }
                });

            }else{
                JOptionPane.showMessageDialog(null,"Cannot login","Error Message", JOptionPane.ERROR_MESSAGE);
            }

        }
        else if (command =="REGISTER"){
            this.Connect_socket();
            Boolean check = regis();

            if(check == true){
                this.hide();
                ClientGui client = new ClientGui(username_.getText(),dis,dos);

            }else{
                JOptionPane.showMessageDialog(null,"Cannot register this account","Error Message", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    public void Connect_socket(){
        try {
            if (socket != null) {
                socket.close();
            }

            this.socket = new Socket(host,port);

            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public boolean regis(){
        try {
            dos.writeUTF("Register");
            dos.writeUTF(this.username_.getText());
            dos.writeUTF(this.pass_.getText());
            dos.flush();

            Boolean check = dis.readBoolean();

            return check;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean isLogin(){
        try {
            dos.writeUTF("Login");
            dos.writeUTF(this.username_.getText());
            dos.writeUTF(this.pass_.getText());
            dos.flush();

            Boolean check = dis.readBoolean();

            return check;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main (String []args) {
        new Login();
    }
}

public class ClientGui extends JFrame implements ActionListener {
    private String username;
    private JList list;
    private JTextArea chat;
    private Vector<String> user_online=new Vector<String>();
    private JTextField mess;
    private JButton send;
    private JButton import_;
    private JScrollPane chatPanel;
    Thread Client;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Map<String,JTextArea> chatwindows = new HashMap<String,JTextArea>();
    ClientGui(String username,DataInputStream dis,DataOutputStream dos){
        this.username = username;
        this.dis=dis;
        this.dos=dos;

        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Client Chat");
        setMinimumSize(new Dimension(640,400));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new FlowLayout());
        JLabel head = new JLabel();
        head.setText("Hi "+username);
        header.add(head);

        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,10,10,10);
        gbc.gridx=0;
        gbc.gridy=0;

        gbc.anchor = GridBagConstraints.WEST;

        chat = new JTextArea();
        chatwindows.put(" ", new JTextArea());
        chat = chatwindows.get(" ");
        chat.setEditable(false);
        chatPanel = new JScrollPane();
        chatPanel.setViewportView(chat);
        chatPanel.setPreferredSize(new Dimension(300,200));
        chatPanel.validate();

        content.add(chatPanel,gbc);

        gbc.gridx++;

        list = new JList();

        list.setListData(user_online);
        list.setPreferredSize(new Dimension(100,200));


        content.add(new JScrollPane(list),gbc);

        mess = new JTextField(27);
        gbc.gridx=0;
        gbc.gridy++;
        content.add(mess,gbc);

        import_ = new JButton("File");
        import_.setActionCommand("FILE");
        import_.addActionListener(this);
        JPanel panel1 = new JPanel(new FlowLayout());
        panel1.add(import_);
        import_.setEnabled(false);
        panel1.add(Box.createRigidArea(new Dimension(10,0)));

        send =new JButton("Send");
        panel1.add(send);
        gbc.gridx++;
        content.add(panel1,gbc);
        send.setActionCommand("SEND");
        send.addActionListener(this);
        send.setEnabled(false);
        panel.add(header,BorderLayout.PAGE_START);

        panel.add(content,BorderLayout.CENTER);

        add(panel);
        pack();
        setSize(new Dimension(650,400));
        setVisible(true);
        Client = new Thread(new Client(dis,username,chat,list,chatwindows,chatPanel,send,import_));
        Client.start();
        System.out.println(list.getSelectedValue());


        mess.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(mess.getText().isBlank()||list.getSelectedValue()==null){
                    send.setEnabled(false);
                }
                else{
                    send.setEnabled(true);
                }

            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try{
                    dos.writeUTF("Logout");
                    dos.flush();
                    try{
                        Client.join();
                    }catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    if(dos!=null){
                        dos.close();
                    }
                    if(dis!=null){
                        dis.close();
                    }
                }catch(IOException e2){
                    e2.printStackTrace();
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e!=null){
                    import_.setEnabled(true);
                    send.setEnabled(false);
                    if(chat!=chatwindows.get(list.getSelectedValue()))
                    {
                        chat = chatwindows.get(list.getSelectedValue());

                        chat.setEditable(false);
                        chatPanel.setViewportView(chat);
                        chatPanel.setPreferredSize(new Dimension(300, 200));
                        chatPanel.validate();
                    }
                    if(mess.getText().equals("")!=true){
                        send.setEnabled(true);
                    }
                }
                else{
                    import_.setEnabled(false);
                }
            }
        });

    }

    public void upload(){
        String messenger = "YOU";
        messenger+=": "+mess.getText()+"\n";
        chat=chatwindows.get(String.valueOf(list.getSelectedValue()));
        chat.setEditable(false);
        chatPanel.setViewportView(chat);
        chatPanel.setPreferredSize(new Dimension(300,200));
        chatPanel.validate();

        chat.append(messenger);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command =="SEND"){
            try{
                dos.writeUTF("Text");
                dos.writeUTF(String.valueOf(list.getSelectedValue()));
                dos.writeUTF(mess.getText());
                dos.flush();

                upload();
                mess.setText("");
            }catch(IOException error){
                error.printStackTrace();
            }
        }
        else if(command=="FILE"){
            JFileChooser fileChooser = new JFileChooser();

            int returnValue = fileChooser.showOpenDialog(this);
            if(returnValue==JFileChooser.APPROVE_OPTION){

                byte[] selectedfile = new byte[(int)fileChooser.getSelectedFile().length()];
                try{
                    BufferedInputStream br = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));

                    br.read(selectedfile,0,selectedfile.length);

                    dos.writeUTF("FILE");

                    dos.writeUTF(fileChooser.getSelectedFile().getName());

                    dos.writeUTF(String.valueOf(list.getSelectedValue()));
                    int size = selectedfile.length;

                    dos.writeInt(size);

                    int pos = 0;
                    int buffersize = 4096;

                    while(size >0){
                        dos.write(selectedfile,pos,Math.min(size,buffersize));
                        pos += Math.min(size,buffersize);
                        size-=buffersize;
                    }
                    dos.flush();
                    br.close();

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                mess.setText("");
            }

        }
    }

}
