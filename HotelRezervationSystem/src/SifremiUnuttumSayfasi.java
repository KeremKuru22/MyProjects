import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class SifremiUnuttumSayfasi extends JFrame {
    private JLabel lblEmail; 
    private JTextField tfEmail; 
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton btnSubmit;
    private JButton btnBackToLogin;
    private GridLayout frameGridLayout;
    private GridLayout topPanelGridLayout;
    private FlowLayout bottomPanelFlowLayout;

    public SifremiUnuttumSayfasi(GirisSayfasi lp) {
        Container cp = getContentPane();
        frameGridLayout = new GridLayout(2, 1, 10, 10); // 10 piksel boşluklar
        cp.setLayout(frameGridLayout);

        topPanel = new JPanel();
        topPanelGridLayout = new GridLayout(2, 2, 5, 5); // 5 piksel boşluklar
        topPanel.setLayout(topPanelGridLayout);

        lblEmail = new JLabel("E-posta:"); 
        tfEmail = new JTextField(); 

        topPanel.add(lblEmail);
        topPanel.add(tfEmail); 

        cp.add(topPanel);
		
        bottomPanel = new JPanel();
        bottomPanelFlowLayout = new FlowLayout(FlowLayout.CENTER);
        bottomPanel.setLayout(bottomPanelFlowLayout);
			        	 		
        btnSubmit = new JButton("Gönder");
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String vEposta = tfEmail.getText().trim();
                
                if (vEposta.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Lütfen e-posta adresinizi girin", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (BufferedReader br = new BufferedReader(new FileReader("ziyaretciListesi.txt"))) {
                    String line;
                    boolean found = false;
                    while ((line = br.readLine()) != null) {
                        String[] strArray = line.split(",");
                        if (strArray.length >= 5 && strArray[4].equals(vEposta)) {
                            found = true;
                            JOptionPane.showMessageDialog(null, "Kullanıcı adınız: " + strArray[2] + "\r\n Şifreniz: " + strArray[3], "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }

                    if (!found) {
                        JOptionPane.showMessageDialog(null, vEposta + "\r\n kayıtlı bir e-posta adresi değil", "Hata", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Dosya okunurken bir hata oluştu.\r\n Hata Mesajı: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        bottomPanel.add(btnSubmit);
		
        btnBackToLogin = new JButton("Girişe Geri Dön");
        btnBackToLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose(); 
                lp.setLocationRelativeTo(null);
                lp.setVisible(true); 
            }
        });
        bottomPanel.add(btnBackToLogin);
        cp.add(bottomPanel);
		
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        setTitle("Şifremi Unuttum Sayfası"); 
        setSize(300, 200); // Boyutu azaltıldı          
        setLocationRelativeTo(null);
        setVisible(true);          
    }
}