import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class GirisSayfasi extends JFrame {
    private JLabel lblKullaniciAdi;
    private JLabel lblSifre;
    private JTextField tfKullaniciAdi;
    private JPasswordField pfSifre;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton btnGiris;
    private JButton btnKayitOl;
    private JButton btnSifremiUnuttum;
    private JButton btnGeriDon; 
    private GridLayout frameGridLayout;
    private GridLayout topPanelGridLayout;
    private FlowLayout bottomPanelFlowLayout;

    public GirisSayfasi() {
        Container cp = getContentPane();
        frameGridLayout = new GridLayout(2, 1, 3, 3);
        cp.setLayout(frameGridLayout);

        topPanel = new JPanel();
        topPanelGridLayout = new GridLayout(2, 2, 3, 3);
        topPanel.setLayout(topPanelGridLayout);
        lblKullaniciAdi = new JLabel("Kullanıcı Adı:");
        topPanel.add(lblKullaniciAdi);
        tfKullaniciAdi = new JTextField();
        topPanel.add(tfKullaniciAdi);
        lblSifre = new JLabel("Şifre:");
        topPanel.add(lblSifre);
        pfSifre = new JPasswordField();
        pfSifre.setEchoChar('*');
        topPanel.add(pfSifre);
        cp.add(topPanel);

        bottomPanel = new JPanel();
        bottomPanelFlowLayout = new FlowLayout(FlowLayout.CENTER, 3, 3);
        bottomPanel.setLayout(bottomPanelFlowLayout);

        btnGiris = new JButton("Giriş");
        BtnGirisActionListener myGirisListener = new BtnGirisActionListener(this);
        btnGiris.addActionListener(myGirisListener);
        bottomPanel.add(btnGiris);

        btnKayitOl = new JButton("Kayıt Ol");
        BtnKayitOlListener myKayitOlListener = new BtnKayitOlListener(this);
        btnKayitOl.addActionListener(myKayitOlListener);
        bottomPanel.add(btnKayitOl);

        btnSifremiUnuttum = new JButton("Şifremi Unuttum");
        BtnSifremiUnuttumListener mySifremiUnuttumListener = new BtnSifremiUnuttumListener(this);
        btnSifremiUnuttum.addActionListener(mySifremiUnuttumListener);
        bottomPanel.add(btnSifremiUnuttum);

        btnGeriDon = new JButton("Geri Dön"); // Geri Dön butonu oluşturma
        BtnGeriDonListener myGeriDonListener = new BtnGeriDonListener(this);
        btnGeriDon.addActionListener(myGeriDonListener);
        bottomPanel.add(btnGeriDon);

        cp.add(bottomPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Giriş Sayfası");
        setSize(400, 150);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class BtnGirisActionListener implements ActionListener {
        private GirisSayfasi lp;

        public BtnGirisActionListener(GirisSayfasi l) {
            lp = l;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            FileReader fr = null;
            BufferedReader br = null;
            boolean found = false;
            try {
                fr = new FileReader("ziyaretciListesi.txt");
                br = new BufferedReader(fr);
                String line;
                String[] strArray;
                while ((line = br.readLine()) != null) {
                    strArray = line.split(",");
                    if ((strArray[2].equals(tfKullaniciAdi.getText().trim()))
                            && (strArray[3].equals(new String(pfSifre.getPassword()).trim()))) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    JOptionPane.showMessageDialog(lp, "Giriş Başarılı!");
                    RezervasyonGirisi rezervasyonGirisSayfasi = new RezervasyonGirisi();
                    rezervasyonGirisSayfasi.setVisible(true);
                    lp.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(lp, "Kullanıcı adı veya şifre hatalı!");
                }
            } catch (IOException e) {
                System.out.println("Dosya okuma hatası: " + e.getMessage());
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException exp) {
                        System.out.println("Dosya kapatma hatası: " + exp.getMessage());
                    }
                }
            }
        }
    }

    private class BtnKayitOlListener implements ActionListener {
        private GirisSayfasi lp;

        public BtnKayitOlListener(GirisSayfasi l) {
            lp = l;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            lp.setVisible(false);
            KayitOlSayfasi ks = new KayitOlSayfasi(lp);
        }
    }

    private class BtnSifremiUnuttumListener implements ActionListener {
        private GirisSayfasi lp;

        public BtnSifremiUnuttumListener(GirisSayfasi l) {
            lp = l;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            lp.setVisible(false);
            SifremiUnuttumSayfasi fp = new SifremiUnuttumSayfasi(lp);
        }
    }

    private class BtnGeriDonListener implements ActionListener { // Geri Dön butonu için ActionListener
        private GirisSayfasi lp;

        public BtnGeriDonListener(GirisSayfasi l) {
            lp = l;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            lp.dispose(); // Giriş sayfasını kapat
            AnaMenu anaMenu = new AnaMenu(); // Ana menüye dön
            anaMenu.setVisible(true);
        }
    }
}
