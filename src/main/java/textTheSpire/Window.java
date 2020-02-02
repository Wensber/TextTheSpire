package textTheSpire;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class Window {

    Display display;
    Shell shell;
    Text label;

    public Window(String header, int w, int h){
        display = new Display();
        shell = new Shell(display);
        shell.setSize(w,h);
        shell.setLocation(200,400);
        setText(header);
        label = new Text(shell, SWT.NONE);

    }

    public void setText(String s){
        if(!s.equals(label.getText())) {

            label.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if(!label.isDisposed()){
                        label.setText(s);
                    }
                }
            });
        }
    }

    public void visible(){
        if(!shell.isVisible())
            shell.open();
    }
    public void invisible(){
        if(shell.isVisible())
            shell.close();
    }

    public void dispose(){
        display.dispose();
    }

}
