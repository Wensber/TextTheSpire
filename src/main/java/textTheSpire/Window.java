package textTheSpire;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.lwjgl.Sys;

public class Window {

    Shell shell;
    Text label;

    String currentText;

    public Window(Display d, String header, int w, int h){
        shell = new Shell(d);
        shell.setSize(w,h);
        shell.setLocation(200,400);
        shell.setText(header);
        label = new Text(shell, SWT.MULTI);
        label.setSize(w,h);
        label.setEditable(false);
        shell.setVisible(true);
        shell.open();

        currentText = "";

    }

    public void setText(String s){

        currentText = s;

        label.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if(!label.isDisposed() && !shell.isDisposed() && !s.equals(label.getText())) {
                    label.setText(s);
                }
            }
        });

    }

    public String getText(){
        return currentText;
    }

    public void setVisible(boolean b){
        shell.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if(!shell.isDisposed()){
                    shell.setVisible(b);
                }
            }
        });
    }


}
