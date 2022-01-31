import java.awt.BorderLayout;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * 	Connection to node via UI
 *
 * */
public class Frame {

    private JFrame frame;
    private JTextArea results;
    private Host nodeHost;


    public Frame(String address, String port)
    {
        nodeHost = new Host(address, port);
        frame = new JFrame("Client");
        frame.setLayout(new BorderLayout());
        addFrameContent();
        frame.pack();
        frame.setSize(1000, 500);
        frame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - frame.getSize().width) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - frame.getSize().height) / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    protected void open()
    {
        frame.setVisible(true);
    }


    private void addFrameContent()
    {

        JPanel generalPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new GridLayout(1,2));

        results = new JTextArea();
        JScrollPane newsScroll = new JScrollPane(results);

        JLabel label1 = new JLabel("Posição a consultar: ");
        JTextField textField1 = new JTextField();
        JLabel label2 = new JLabel("Comprimento: ");
        JTextField textField2 = new JTextField();
        JButton btnSearch = new JButton("Consultar");

        northPanel.add(label1);
        northPanel.add(textField1);
        northPanel.add(label2);
        northPanel.add(textField2);
        northPanel.add(btnSearch);

        generalPanel.add(northPanel, BorderLayout.NORTH);
        generalPanel.add(newsScroll, BorderLayout.CENTER);


        frame.add(generalPanel);


        //Search Button
        btnSearch.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                String position = textField1.getText();
                String length = textField2.getText();

                if(!PositionAndLengthAreValid(position, length)) {
                    JOptionPane.showMessageDialog(null, "Números inválidos para consulta");
                    return;
                }

                int positionInt = Integer.parseInt(position);
                int lengthInt = Integer.parseInt(length);

                CloudByte[] data = getNodeData(positionInt, lengthInt);


                if(data == null)
                    JOptionPane.showMessageDialog(null, "Poderão ainda não existir nós ou o nó selecionado para os dados ainda não está disponível para consulta. Por favor tente novamente");
                else {
                    String result = "";

                    for(CloudByte cb : data) {
                        result = result.concat(cb.toString());
                    }

                    results.setText(result);
                }


            }
        });
    }

    private boolean PositionAndLengthAreValid(String position_, String length_) {
        try {

            int position = Integer.parseInt(position_);
            int length = Integer.parseInt(length_);

            if(position < 0 || length < 0)
                return false;

            return ((position + length) >= 0  && (position + length) < 1000000);
        }
        catch(Exception ex) {
            return false;
        }
    }


    private CloudByte[] getNodeData(int startIndex, int length) {
        try {

            Socket socket = new Socket(nodeHost.getAddress(), nodeHost.getPortInt());
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            objectOutputStream.writeObject(MessageType.GETDATA);
            objectOutputStream.writeObject(startIndex);
            objectOutputStream.writeObject(length);

            while(true) {

                CloudByte[] pieceOfData = (CloudByte[]) objectInputStream.readObject();

                outputStream.close();
                objectOutputStream.close();
                socket.close();

                return pieceOfData;

            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            return null;
        }
    }



    public static void main(String[] args) throws IOException
    {

        Frame frame = new Frame(args[1], args[2]);
        frame.open();
    }



}