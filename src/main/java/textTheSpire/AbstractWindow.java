package textTheSpire;

abstract class AbstractWindow {
    
    Window window;

    boolean isVisible;

    public void update(){
        if(isVisible) {
            String s = getText();

            if (s.length() > 0) {
                window.setVisible(true);
                window.setText(s);
            } else {
                window.setVisible(false);
            }
        }else{
            window.setVisible(false);
        }
    }

    abstract String getText();
    
}
