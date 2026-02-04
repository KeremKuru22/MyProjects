import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class RezervasyonGirisi extends JFrame {
    private JLabel lblOdaKat;
    private JLabel lblOdaTur;
    private JLabel lblOdaCephe;
    private JLabel lblOdaTv;
    private JLabel lblOdaBuzDolabi;
    private JLabel lblOdaKlima;
    private JLabel lblOdaJakuzi;
    private JLabel lblOdaNumarasi;
    private JLabel lblKullaniciAdi;

    private JComboBox<String> cmbOdaKat;
    private JComboBox<String> cmbOdaTur;
    private JComboBox<String> cmbOdaCephe;
    private JComboBox<String> cmbOdaNumarasi;
    private JCheckBox chkOdaTv;
    private JCheckBox chkOdaBuzDolabi;
    private JCheckBox chkOdaKlima;
    private JCheckBox chkOdaJakuzi;
    private JTextField txtKullaniciAdi;

    private JButton btnKaydet;

    private Set<String> mevcutOdaNumaralari;

    public RezervasyonGirisi() {
        setTitle("Rezervasyon Girişi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(12, 2, 5, 5));

        lblKullaniciAdi = new JLabel("Kullanıcı Adı:");
        txtKullaniciAdi = new JTextField();
        panel.add(lblKullaniciAdi);
        panel.add(txtKullaniciAdi);

        lblOdaTur = new JLabel("Oda Tür:");
        String[] turOptions = {"Tek Kişilik", "Çift Kişilik", "Aile Odası", "Suit"};
        cmbOdaTur = new JComboBox<>(turOptions);
        panel.add(lblOdaTur);
        panel.add(cmbOdaTur);

        lblOdaKat = new JLabel("Oda Kat:");
        cmbOdaKat = new JComboBox<>();
        panel.add(lblOdaKat);
        panel.add(cmbOdaKat);

        lblOdaCephe = new JLabel("Oda Cephe:");
        String[] cepheOptions = {"Ön Cephe", "Arka Cephe", "Yan Cephe"};
        cmbOdaCephe = new JComboBox<>(cepheOptions);
        panel.add(lblOdaCephe);
        panel.add(cmbOdaCephe);

        lblOdaTv = new JLabel("TV:");
        chkOdaTv = new JCheckBox();
        panel.add(lblOdaTv);
        panel.add(chkOdaTv);

        lblOdaBuzDolabi = new JLabel("Buzdolabı:");
        chkOdaBuzDolabi = new JCheckBox();
        panel.add(lblOdaBuzDolabi);
        panel.add(chkOdaBuzDolabi);

        lblOdaKlima = new JLabel("Klima:");
        chkOdaKlima = new JCheckBox();
        panel.add(lblOdaKlima);
        panel.add(chkOdaKlima);

        lblOdaJakuzi = new JLabel("Jakuzi:");
        chkOdaJakuzi = new JCheckBox();
        panel.add(lblOdaJakuzi);
        panel.add(chkOdaJakuzi);

        lblOdaNumarasi = new JLabel("Oda Numarası:");
        cmbOdaNumarasi = new JComboBox<>();
        panel.add(lblOdaNumarasi);
        panel.add(cmbOdaNumarasi);

        btnKaydet = new JButton("Rezervasyon Yap");
        panel.add(btnKaydet);

        add(panel);
        setSize(400, 400); // Pencere boyutunu belirle
        setLocationRelativeTo(null);

        // Panel boyutunu belirledikten sonra bileşenleri ekle
        add(panel, BorderLayout.CENTER); // Paneli eklerken yerleşim yöneticisini belirt
        
        cmbOdaKat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOdaNumarasiOptions((String) cmbOdaKat.getSelectedItem());
            }
        });

        // Mevcut oda numaralarını yükle
        loadSavedRooms();

        // Oda Türü seçiminde Oda Katı seçeneklerini güncelle
        cmbOdaTur.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedTur = (String) cmbOdaTur.getSelectedItem();
                updateOdaKatOptions(selectedTur);
            }
        });

        // Kaydet butonuna tıklama olayını dinle
        btnKaydet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String kullaniciAdi = txtKullaniciAdi.getText();
                if (!kullaniciAdi.isEmpty()) {
                    try (BufferedReader br = new BufferedReader(new FileReader("ziyaretciListesi.txt"))) {
                        boolean found = false;
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] parts = line.split(",");
                            if (parts.length >= 4 && parts[2].trim().equals(kullaniciAdi)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            // Kullanıcı adı eşleştiği için bilgileri kaydet
                            kaydetBilgileri();
                        } else {
                            JOptionPane.showMessageDialog(null, "Kullanıcı adı bulunamadı!");
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Ziyaretçi listesi okunurken bir hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Lütfen kullanıcı adını girin!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });    
        
        // Başlangıçta oda türüne göre oda kat seçeneklerini ayarla
        updateOdaKatOptions((String) cmbOdaTur.getSelectedItem());
}
        
    private void updateOdaKatOptions(String selectedTur) {
        cmbOdaKat.removeAllItems();

        switch (selectedTur) {
            case "Tek Kişilik":
                for (int i = 1; i <= 3; i++) {
                    cmbOdaKat.addItem(String.valueOf(i));
                }
                break;
            case "Çift Kişilik":
                for (int i = 4; i <= 6; i++) {
                    cmbOdaKat.addItem(String.valueOf(i));
                }
                break;
            case "Aile Odası":
                for (int i = 7; i <= 9; i++) {
                    cmbOdaKat.addItem(String.valueOf(i));
                }
                break;
            case "Suit":
                for (int i = 10; i <= 12; i++) {
                    cmbOdaKat.addItem(String.valueOf(i));
                }
                break;
        }
    }
    
    private void updateOdaNumarasiOptions(String selectedKat) {
        cmbOdaNumarasi.removeAllItems();

        if (selectedKat != null) {
            int kat = Integer.parseInt(selectedKat);
            for (int i = (kat - 1) * 3 + 1; i <= kat * 3; i++) {
                String odaNumarasi = String.valueOf(i);
                if (!mevcutOdaNumaralari.contains(odaNumarasi)) {
                    cmbOdaNumarasi.addItem(odaNumarasi);
                }
            }
        }
    }

    private void loadSavedRooms() {
        mevcutOdaNumaralari = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader("ziyaretcibilgisi.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 8) {
                    String odaNumarasi = parts[8].trim();
                    mevcutOdaNumaralari.add(odaNumarasi);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void kaydetBilgileri() throws IOException {
        String kullaniciAdi = txtKullaniciAdi.getText().trim(); // Kullanıcı adını al

        String odaKat = (String) cmbOdaKat.getSelectedItem();
        String odaTur = (String) cmbOdaTur.getSelectedItem();
        String odaCephe = (String) cmbOdaCephe.getSelectedItem();
        String odaNumarasi = (String) cmbOdaNumarasi.getSelectedItem();
        boolean odaTv = chkOdaTv.isSelected();
        boolean odaBuzDolabi = chkOdaBuzDolabi.isSelected();
        boolean odaKlima = chkOdaKlima.isSelected();
        boolean odaJakuzi = chkOdaJakuzi.isSelected();

        try (BufferedReader reader = new BufferedReader(new FileReader("ziyaretcibilgisi.txt"))) {
            String line;
            String[] strArray;

            boolean isUserExists = false;
            while ((line = reader.readLine()) != null) {
                strArray = line.split(",");
                if ((strArray[0].equals(kullaniciAdi))) {
                    isUserExists = true;
                    break;
                }
            }
            if (isUserExists) {
                JOptionPane.showMessageDialog(this, "Bu kullanıcının zaten rezervasyonu var.");
            } else {
                String temp = kullaniciAdi + "," +
                              odaTur + "," +
                              odaKat + "," +
                              odaCephe + "," +
                              (odaTv ? "Evet" : "Hayır") + "," +
                              (odaBuzDolabi ? "Evet" : "Hayır") + "," +
                              (odaKlima ? "Evet" : "Hayır") + "," +
                              (odaJakuzi ? "Evet" : "Hayır") + "," +
                              odaNumarasi + "\r\n";

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("ziyaretcibilgisi.txt", true))) {
                    writer.write(temp);
                    JOptionPane.showMessageDialog(this, "Bilgiler başarıyla kaydedildi.");
                }
            }
        }
    }

    // Kullanıcı adını döndüren bir getter metodu ekle
    public String getKullaniciAdi() {
        return txtKullaniciAdi.getText();
    }
}
