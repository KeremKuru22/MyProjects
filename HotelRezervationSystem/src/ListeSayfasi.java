import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ListeSayfasi extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public ListeSayfasi() {
        setTitle("Yönetici Paneli");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Sol paneldeki butonlar
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // 3 satır 1 sütun düzeni
        JButton ziyaretciListesiButton = new JButton("Kayıtlı Listesi");
        JButton ziyaretciBilgiButton = new JButton("Rezervasyon Bilgileri");
        buttonPanel.add(ziyaretciListesiButton);
        buttonPanel.add(ziyaretciBilgiButton);

        // Butonların boyutunu ayarlama
        Dimension buttonSize = new Dimension(180, 30);
        ziyaretciListesiButton.setPreferredSize(buttonSize);
        ziyaretciBilgiButton.setPreferredSize(buttonSize);

        // Orta paneldeki tablo
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tablodaki hücrelerin düzenlenmesini devre dışı bırak
            }
        };
        table = new JTable(tableModel);
        table.setPreferredScrollableViewportSize(new Dimension(600, 400)); // Tablo boyutunu ayarlama
        table.setFillsViewportHeight(true); // Tablonun tüm alanı kaplamasını sağlama
        JScrollPane scrollPane = new JScrollPane(table);

        // Sil butonu
        JButton silButton = new JButton("Sil");

        // Tablo başlıklarını kalın yapma
        Font boldFont = table.getTableHeader().getFont().deriveFont(Font.BOLD);
        table.getTableHeader().setFont(boldFont);

        // Tablo hücrelerini hizalama
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        silButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) { // Bir satır seçildiyse
                    String kullaniciAdi = (String) tableModel.getValueAt(selectedRow, 0); // Kullanıcı adını al

                    // Dosyalardan ilgili satırı sil
                    deleteRowFromFile("ziyaretciListesi.txt", kullaniciAdi);
                    deleteRowFromFile("ziyaretcibilgisi.txt", kullaniciAdi);

                    tableModel.removeRow(selectedRow); // Seçili satırı tablodan kaldır
                    JOptionPane.showMessageDialog(ListeSayfasi.this, "Silindi.");
                } else {
                    JOptionPane.showMessageDialog(ListeSayfasi.this, "Lütfen bir rezervasyon seçin.", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        ziyaretciListesiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTableData("ziyaretciListesi.txt", new String[]{"Ad", "Soyad", "Kullanıcı Adı", "Şifre", "E-Posta", "TC", "Telefon Numarası"});
            }
        });

        ziyaretciBilgiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTableData("ziyaretcibilgisi.txt", new String[]{"Kullanıcı Adı", "Oda Türü", "Oda Katı", "Oda Cephesi", "TV", "Buzdolabı", "Klima", "Jakuzi", "Oda Numarası"});
            }
        });

        // Geri Dön butonu
        JButton geriDonButton = new JButton("Geri Dön");
        geriDonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // ListeSayfasi penceresini kapat
                new AnaMenu(); // AnaMenu penceresini yeniden aç
            }
        });

        // Sil ve Geri Dön butonlarını içeren panel
        JPanel southPanel = new JPanel();
        southPanel.add(silButton);
        southPanel.add(geriDonButton);

        // Ana panelin oluşturulması
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        // Ana panelin JFrame'e eklenmesi
        add(mainPanel);

        // Pencere boyutu ayarlama ve görünür yapma
        setSize(1400, 500);
        setLocationRelativeTo(null); // Pencereyi ekranın ortasına yerleştir
        setVisible(true);
    }

    private void loadTableData(String filePath, String[] columnNames) {
        tableModel.setRowCount(0); // Mevcut verileri temizle
        tableModel.setColumnIdentifiers(columnNames);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                tableModel.addRow(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ListeSayfasi.this, "Dosya okunurken bir hata oluştu: " + filePath, "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRowFromFile(String filePath, String kullaniciAdi) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(kullaniciAdi + ",")) { // Kullanıcı adı ile başlayan satırı atla
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ListeSayfasi.this, "Dosya okunurken bir hata oluştu: " + filePath, "Hata", JOptionPane.ERROR_MESSAGE);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ListeSayfasi.this, "Dosya yazılırken bir hata oluştu: " + filePath, "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}