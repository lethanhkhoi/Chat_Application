package vn.edu.hcmus.student.sv19127186.Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.Vector;

/**
 * vn.edu.hcmus.student.sv19127186.Client
 * Created by 84904
 * Date 23/12/2021 - 1:34 PM
 * Description: ...
 */
public class Client implements Runnable{
    private DataInputStream dis;
    private String username;
    private JTextArea chat;
    private JList list;
    private Map<String,JTextArea> chatwindows;
    private JScrollPane chatPanel;
    private JButton send;
    private JButton import_;
    Client(DataInputStream dis,String username,JTextArea chat_box,JList user_box,Map<String,JTextArea> temp,JScrollPane chatPanel,JButton send,JButton import_){
        this.dis=dis;
        this.username=username;
        this.chat = chat_box;
        this.list=user_box;
        chatwindows = temp;
        this.chatPanel=chatPanel;
        this.send=send;
        this.import_=import_;
    }
    public void upload(String name,String content){
        System.out.println(name);
        JTextArea chat_=chatwindows.get(name);
        chat_.setEditable(false);


        String mess = name;
        mess+=": "+content+"\n";
        chat_.append(mess);
    }
    @Override
    public void run() {
        try{
            while(true){

                String response = dis.readUTF();
                if(response.equals("UPDATE")){
                    String ischatting = String.valueOf(list.getSelectedValue());
                    String[] list_user = dis.readUTF().split(",");
                    Vector<String> temp = new Vector<String>();

                    for(String user :list_user){
                        if(user.equals(username)==false){
                            temp.add(user);
                            if(chatwindows.get(user)==null){
                                JTextArea tmp = new JTextArea();
                                tmp.setEditable(false);
                                chatwindows.put(user,tmp);
                            }
                        }
                    }
                    boolean check = false;
                    for(String user:list_user){
                        if(user.equals(ischatting)==true){
                           check = true;
                        }
                    }
                    if(check == false){
                        chat=chatwindows.get(" ");
                        chat.setEditable(false);
                        chatPanel.setViewportView(chat);
                        chatPanel.setPreferredSize(new Dimension(300,200));
                        chatPanel.validate();
                        send.setEnabled(false);
                        import_.setEnabled(false);
                    }

                    list.setListData(temp);
                }
                else if (response.equals("Text")==true){
                    String name = dis.readUTF();
                    String content = dis.readUTF();
                    this.upload(name,content);
                }
                else if(response.equals("CLOSE")==true){
                    break;
                }
                else if (response.equals("FILE")==true){
                    String sender = dis.readUTF();
                    String filename = dis.readUTF();
                    int size = dis.readInt();
                    int buffersize = 4096;
                    byte [] buffer = new byte[size];
                    int pos = 0;

                    FileOutputStream file = new FileOutputStream(filename);
                    BufferedOutputStream bout = new BufferedOutputStream(file);
                    while(size>0){
                        dis.read(buffer,pos,Math.min(size,buffersize));
                        bout.write(buffer,pos,Math.min(size,buffersize));
                        pos+=Math.min(size,buffersize);
                        size-=buffersize;

                    }
                    bout.flush();
                    file.close();
                    bout.close();
                    JOptionPane.showMessageDialog(null,  sender+" send you a file");
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }


}
