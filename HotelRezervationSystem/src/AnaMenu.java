import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnaMenu extends JFrame {
    public AnaMenu() {
        setTitle("Ana Menü");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Ana menü içeriği
        JLabel lbl = new JLabel("Hoşgeldiniz:");
        lbl.setHorizontalAlignment(JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));

        JButton otelYonetimiButton = new JButton("Otel Yönetimi Girişi");
        JButton rezervasyonButton = new JButton("Otel Rezervasyon Girişi");

        // Otel yönetimi butonuna tıklama olayı
        otelYonetimiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame otelYonetimiFrame = new JFrame("Otel Yönetimi Girişi");
                otelYonetimiFrame.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridx = 0;
                gbc.gridy = 0;

                JLabel kullaniciAdiLabel = new JLabel("Kullanıcı Adı:");
                JTextField kullaniciAdiField = new JTextField(15);
                JLabel sifreLabel = new JLabel("Şifre:");
                JPasswordField sifreField = new JPasswordField(15);
                JButton girisButton = new JButton("Giriş");
                JButton geriDonButton = new JButton("Geri Dön");

                gbc.gridx = 0;
                gbc.gridy = 0;
                otelYonetimiFrame.add(kullaniciAdiLabel, gbc);
                gbc.gridx = 1;
                gbc.gridy = 0;
                otelYonetimiFrame.add(kullaniciAdiField, gbc);
                gbc.gridx = 0;
                gbc.gridy = 1;
                otelYonetimiFrame.add(sifreLabel, gbc);
                gbc.gridx = 1;
                gbc.gridy = 1;
                otelYonetimiFrame.add(sifreField, gbc);
                gbc.gridx = 1;
                gbc.gridy = 2;
                otelYonetimiFrame.add(girisButton, gbc);
                gbc.gridx = 1;
                gbc.gridy = 3;
                otelYonetimiFrame.add(geriDonButton, gbc);

                girisButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String kullaniciAdi = kullaniciAdiField.getText();
                        String sifre = new String(sifreField.getPassword());
                        // Kullanıcı adı ve şifreyi kontrol et
                        if (kullaniciAdi.equals("krmkr") && sifre.equals("krm123")) {
                            JOptionPane.showMessageDialog(otelYonetimiFrame, "Giriş Başarılı!");
                            // Giriş başarılıysa ListeSayfasi'ni aç
                            ListeSayfasi listeSayfasi = new ListeSayfasi();
                            listeSayfasi.setVisible(true);
                            otelYonetimiFrame.dispose(); // Otel Yönetimi Girişi penceresini kapat
                        } else {
                            JOptionPane.showMessageDialog(otelYonetimiFrame, "Kullanıcı adı veya şifre hatalı!");
                        }
                    }
                });

                geriDonButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        otelYonetimiFrame.dispose(); // Otel Yönetimi Girişi penceresini kapat
                        setVisible(true); // Ana Menü penceresini tekrar görünür yap
                    }
                });

                otelYonetimiFrame.setSize(400, 200);
                otelYonetimiFrame.setLocationRelativeTo(null);
                otelYonetimiFrame.setVisible(true);
                setVisible(false); // Ana Menü penceresini geçici olarak gizle
            }
        });

        // Rezervasyon butonuna tıklama olayı
        rezervasyonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Giriş sayfasını aç
                GirisSayfasi lp = new GirisSayfasi();
                dispose(); // Ana Menü penceresini kapat
            }
        });

        // Ana menü düzeni
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lbl, gbc);
        gbc.gridy = 1;
        panel.add(otelYonetimiButton, gbc);
        gbc.gridy = 2;
        panel.add(rezervasyonButton, gbc);

        add(panel, BorderLayout.CENTER);

        // Pencere boyutu ayarlama ve görünür yapma
        setSize(300, 200);
        setLocationRelativeTo(null); // Sayfayı ortalama
        setVisible(true);
    }
}