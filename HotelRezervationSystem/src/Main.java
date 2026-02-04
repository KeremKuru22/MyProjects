import javax.swing.*;    // Using Swing components and containers

public class Main {
	public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AnaMenu();
            }
        });
    }	
}