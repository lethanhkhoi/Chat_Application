package vn.edu.hcmus.student.sv19127186.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * vn.edu.hcmus.student.sv19127186.Server
 * Created by 84904
 * Date 22/12/2021 - 6:51 PM
 * Description: ...
 */
public class ServerGUI extends JPanel implements ActionListener {
    private JButton openbtn;

    ServerGUI(){
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        openbtn = new JButton("Open Server");
        openbtn.setActionCommand("OPEN");
        openbtn.addActionListener(this);
        openbtn.setPreferredSize(new Dimension(200,200));
        panel.add(openbtn,gbc);
        add(panel,BorderLayout.CENTER);


    }
    public void createGui(){
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setMinimumSize(new Dimension(250,250));

        this.setOpaque(true);
        frame.setContentPane(this);
        frame.pack();
        frame.setSize(new Dimension(400,400));
        frame.setVisible(true);


    }
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command =="OPEN"){

            Thread t =new Thread(){
                public void run(){

                    try {

                        new Server();

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            };
            t.start();
            openbtn.setEnabled(false);
        }
    }

    public static void main(String[] args) {

        ServerGUI server = new ServerGUI();
        server.createGui();
    }
}
