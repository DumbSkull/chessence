package com.chessence.gui.pages.components;

import com.chessence.Message;
import com.chessence.gui.pages.CreateRoomPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class ChatBox extends JPanel {
    public static Dimension size = null;

    public static JPanel box = new JPanel();
    public static JScrollPane scrollPane = null;
    public static JPanel mainChatWindow = new JPanel();
    public static JTextPane textBox = new JTextPane();

    public static int W;
    public static int H;

    public static ArrayList<ChatMessage> MESSAGES = new ArrayList<>();
    public static int totalHeighOfWindow = 0;
    public static String textBoxInput = "";
    public ObjectOutputStream objectOutputStream;

    public ChatBox(int W, int H, ObjectOutputStream objectOutputStream) {

        this.W = W;
        this.H = H;
        this.objectOutputStream = objectOutputStream;

        FlowLayout layout = (FlowLayout) super.getLayout();
        layout.setHgap(10);
        layout.setVgap(10);
        layout.setAlignment(FlowLayout.LEADING);
        this.size = new Dimension(W, H);
        super.setPreferredSize(size);
        super.setOpaque(false);
        setBackground(new Color(0xC88275));

        //------------------------- LABEL---------------------------
        JLabel label = new JLabel("C h a t");
        label.setFont(new Font("Colonna MT", Font.BOLD, 25));
        label.setForeground(new Color(0x321F28));
        super.add(label);
        super.add(new HorizontalLine((int) (W * 0.95), 6, new Color(0x321F28)));

        //------------------------MESSAGES--------------------------

        scrollPane = new JScrollPane(mainChatWindow,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(0, 0, W, H);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(0xC88275));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.setPreferredSize(new Dimension((int) (W * 0.95), (int) (3.5 * H / 5)));

        mainChatWindow.setLayout(new FlowLayout());
        mainChatWindow.setOpaque(false);

        for (ChatMessage item : MESSAGES) {
            displayNewMessage(item.getMessage(), item.isOpponent());
        }

        mainChatWindow.setPreferredSize(new Dimension((int) (W * 0.92), totalHeighOfWindow));
        //JScrollBar vertical = scrollPane.getVerticalScrollBar();
        scrollPane.getViewport().setViewPosition(new Point(0, totalHeighOfWindow));
        //vertical.setValue(totalHeighOfWindow+100);
        super.add(scrollPane);

        //------------------------TEXT BOX--------------------------

        Border roundedBorder = new LineBorder(new Color(0x734046), 3, true); // the third parameter - true, says it's round
        textBox.setBorder(roundedBorder);
        textBox.setEditable(true);
        textBox.setPreferredSize(new Dimension((int) (W * 0.95), H / 10));

        textBox.setBackground(new Color(0xD79E92));
        textBox.setBounds(0, H - textBox.getHeight(), textBox.getWidth(), textBox.getHeight());
        box.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(W, (int) (H / 8.5)));
        box.add(textBox);
        super.add(box, BorderLayout.SOUTH);


        textBox.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == 10) {
                    if(CreateRoomPanel.Player_Status=='S')
                    {
                        textBox.setText("Only players can chat!");
                        textBoxInput = "Only players can chat!";
                        return;
                    }

                    if (textBox.getText().trim() != "") {
                        String text = textBox.getText().trim();
                        textBox.setText("");
                        textBoxInput = "";
                        addNewMessage(text, false);

                        //also send to the other client//
                        Message messageToSend = new Message(text, "chat");
                        try {
                            objectOutputStream.writeObject(messageToSend);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                            System.out.println("\nError sending the message to the server!");
                        }

                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                /*if(e.getKeyChar()== ' ' && textBoxInput == "")
                {
                    return;
                }*/
                //textBoxInput = textBoxInput + e.getKeyChar();
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });
    }

    public static void addNewMessage(String text, boolean isOpponent) {

        MESSAGES.add(new ChatMessage(text, isOpponent));
        displayNewMessage(text, isOpponent);
    }

    public static void displayNewMessage(String text, boolean isOpponent) {
        int textWidth = getTextWidth(text);
        int numberOfLines = (int) Math.ceil(textWidth / (W * 0.95));

        JPanel message = new JPanel();
        message.setLayout(new FlowLayout(isOpponent ? FlowLayout.LEFT : FlowLayout.RIGHT, 10, 10));
        message.setOpaque(false);
        totalHeighOfWindow += 60 * numberOfLines + 10;
        message.setPreferredSize(new Dimension((int) (W * 0.80), 50 * numberOfLines + 10));
        message.add(new TextBubble(text, isOpponent, W));
        mainChatWindow.add(message);
        mainChatWindow.add(new HorizontalSpace((int) W, 0));
        mainChatWindow.setPreferredSize(new Dimension((int) (W * 0.92), totalHeighOfWindow));
        scrollPane.getViewport().setViewPosition(new Point(0, totalHeighOfWindow));
        if (!mainChatWindow.isValid())
            mainChatWindow.validate();
        //scrollPane.revalidate();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getSize().width - 1,
                getSize().height - 1, 20, 20);

    }

    private static int getTextWidth(String text) {
        //calculating the number of lines required for this particular message:
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 13);
        int textWidth = (int) (font.getStringBounds(text, frc).getWidth());
        return textWidth;
    }
}

class ChatMessage {
    private String message;
    private boolean isOpponent;

    ChatMessage(String message, boolean isOpponent) {
        this.message = message;
        this.isOpponent = isOpponent;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOpponent() {
        return isOpponent;
    }
}
