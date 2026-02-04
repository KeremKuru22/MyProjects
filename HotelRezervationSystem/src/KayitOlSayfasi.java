import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;

public class KayitOlSayfasi extends JFrame {
    private JLabel lblAd;
    private JLabel lblSoyad;
    private JLabel lblKullaniciAdi;
    private JLabel lblSifre;
    private JLabel lblEposta;
    private JLabel lblTCyaDaPasaportNo;
    private JLabel lblTelefonNo;
    private JTextField tfAd;
    private JTextField tfSoyad;
    private JTextField tfKullaniciAdi;
    private JPasswordField pfSifre;
    private JTextField tfEposta;
    private JTextField tfTCyaDaPasaportNo;
    private JTextField tfTelefonNo;
    private JPanel ustPanel;
    private JPanel altPanel;
    private JButton btnKayitOl;
    private GridLayout frameGridDuzen;
    private GridLayout ustPanelGridDuzen;
    private FlowLayout altPanelAkisDuzen;

    public KayitOlSayfasi(GirisSayfasi lp) {
        Container cp = getContentPane();
        frameGridDuzen = new GridLayout(2, 1, 3, 3);
        cp.setLayout(frameGridDuzen);

        ustPanel = new JPanel();
        ustPanelGridDuzen = new GridLayout(7, 2, 3, 3);
        ustPanel.setLayout(ustPanelGridDuzen);

        lblAd = new JLabel("Ad:");
        ustPanel.add(lblAd);
        tfAd = new JTextField();
        ustPanel.add(tfAd);

        lblSoyad = new JLabel("Soyad:");
        ustPanel.add(lblSoyad);
        tfSoyad = new JTextField();
        ustPanel.add(tfSoyad);

        lblKullaniciAdi = new JLabel("Kullanıcı Adı:");
        ustPanel.add(lblKullaniciAdi);
        tfKullaniciAdi = new JTextField();
        ustPanel.add(tfKullaniciAdi);

        lblSifre = new JLabel("Şifre:");
        ustPanel.add(lblSifre);
        pfSifre = new JPasswordField();
        pfSifre.setEchoChar('*');
        ustPanel.add(pfSifre);

        lblEposta = new JLabel("E-posta:");
        ustPanel.add(lblEposta);
        tfEposta = new JTextField();
        ustPanel.add(tfEposta);

        lblTCyaDaPasaportNo = new JLabel("TC veya Pasaport:");
        ustPanel.add(lblTCyaDaPasaportNo);
        tfTCyaDaPasaportNo = new JTextField();
        ((AbstractDocument) tfTCyaDaPasaportNo.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(11));
        ustPanel.add(tfTCyaDaPasaportNo);

        lblTelefonNo = new JLabel("Telefon Numarası:");
        ustPanel.add(lblTelefonNo);
        tfTelefonNo = new JTextField();
        ((AbstractDocument) tfTelefonNo.getDocument()).setDocumentFilter(new LengthRestrictedDocumentFilter(10));
        ustPanel.add(tfTelefonNo);

        cp.add(ustPanel);

        altPanel = new JPanel();
        altPanelAkisDuzen = new FlowLayout(FlowLayout.CENTER);
        altPanel.setLayout(altPanelAkisDuzen);

        btnKayitOl = new JButton("Kayıt Ol");
        btnKayitOl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                BufferedReader br = null;
                FileReader fr = null;
                boolean found = false;

                String vEposta = tfEposta.getText().trim();
                try {
                    fr = new FileReader("ziyaretciListesi.txt");
                    br = new BufferedReader(fr);
                    String line;
                    String[] strArray;

                    while ((line = br.readLine()) != null) {
                        strArray = line.split(",");

                        if (strArray[4].equals(tfEposta.getText().trim())) {
                            found = true;
                            JOptionPane.showMessageDialog(null, vEposta + "\r\n E-posta adresi zaten kayıtlı. Eğer kullanıcı adınızı ya da şifrenizi unuttuysanız, 'Şifremi Unuttum' butonuna basınız.", "HATA", JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }

                    if (!found) {
                        if (tfAd.getText().isEmpty() || tfSoyad.getText().isEmpty() || tfKullaniciAdi.getText().isEmpty() || new String(pfSifre.getPassword()).isEmpty() || tfEposta.getText().isEmpty() || tfTCyaDaPasaportNo.getText().isEmpty() || tfTelefonNo.getText().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Tüm alanların doldurulması zorunludur!", "HATA", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try (FileWriter fWriter = new FileWriter("ziyaretciListesi.txt", true)) {
                            String temp = tfAd.getText().trim() + "," +
                                          tfSoyad.getText().trim() + "," +
                                          tfKullaniciAdi.getText().trim() + "," +
                                          new String(pfSifre.getPassword()).trim() + "," +
                                          tfEposta.getText().trim() + "," +
                                          tfTCyaDaPasaportNo.getText().trim() + "," +
                                          tfTelefonNo.getText().trim() + "\r\n";

                            fWriter.write(temp);

                            System.out.println("Kayıt işlemi başarılı...");
                            dispose();
                            lp.setLocationRelativeTo(null);
                            lp.setVisible(true);
                        } catch (IOException exp) {
                            System.out.println("Kayıt işlemi başarısız...");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Dosya okuma hatası...");
                } finally {
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (IOException exp) {
                            System.out.println("Okuma işlemi başarılı ancak kapatma başarısız...");
                        }
                    }
                }
            }
        });

        altPanel.add(btnKayitOl);
        cp.add(altPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Kayıt Sayfası");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Sınırlandıran DocumentFilter sınıfı
    class LengthRestrictedDocumentFilter extends DocumentFilter {
        private int limit;

        LengthRestrictedDocumentFilter(int limit) {
            this.limit = limit;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) {
                return;
            }

            if ((fb.getDocument().getLength() + string.length()) <= limit) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) {
                return;
            }

            if ((fb.getDocument().getLength() + text.length() - length) <= limit) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
