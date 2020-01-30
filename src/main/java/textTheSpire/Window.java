package textTheSpire;

import javax.swing.*;


public class Window {

    public JFrame f;
    public JTextArea t;

    public Window(String header, int w, int h){
        f = new JFrame(header);
        f.setResizable(true);
        f.setSize(w, h);
        f.setLocation(200,400);
        t = new JTextArea("");
        t.setEditable(false);
        t.setLineWrap(true);
        t.setSize(w, h);
        f.add(t);
        f.setVisible(false);
    }

    public void setText(String s){
        t.setText(s);
        t.repaint();
    }

    public void visible(){
        if(!f.isVisible())
            f.setVisible(true);
    }
    public void invisible(){
        if(f.isVisible())
            f.setVisible(false);
    }

}
