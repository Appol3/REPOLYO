/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.sql_trial;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
/**
 *
 * @author Hazel
 */
public class POS extends javax.swing.JFrame {
    
    private double totalPrice = 0.0;
    private static POS instance;
    
    public POS() {
        initComponents();
        loadMenuDataCombined();
        
       
    }
    public static POS getInstance() {
        if (instance == null) {
            instance = new POS();
        }
        return instance;
    }

     private void loadMenuDataCombined() {
         clearPanel(ORDER_MENU); // Clear existing items

    // Create a container panel with a custom layout (to arrange items manually)
    JPanel containerPanel = new JPanel();
    containerPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for flexible placement

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10); // Add spacing between components
    gbc.anchor = GridBagConstraints.NORTHWEST; // Align items to top-left

    // Load meals from the "ultra" table into the first column
    String mealDbUrl = "jdbc:mysql://localhost:3306/ultra";
    String mealDbUser = "root";
    String mealDbPassword = "Password123!";
    String mealQuery = "SELECT * FROM ultra"; // Query to fetch meals

    // Load meals data into first column (Column 0)
    loadDataIntoPanel(mealDbUrl, mealDbUser, mealDbPassword, mealQuery, containerPanel, gbc, 0);

    // Load drinks from the "tanginumin" table into the second column
    String drinksDbUrl = "jdbc:mysql://localhost:3306/ultra";
    String drinksDbUser = "root";
    String drinksDbPassword = "Password123!";
    String drinksQuery = "SELECT * FROM tanginumin"; // Query to fetch drinks

    // Load drinks data into second column (Column 1)
    loadDataIntoPanel(drinksDbUrl, drinksDbUser, drinksDbPassword, drinksQuery, containerPanel, gbc, 1);

    // Add the container panel to a scroll pane
    JScrollPane menuScrollPane = new JScrollPane(containerPanel);
    menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    menuScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    // Add the scroll pane to the main panel
    ORDER_MENU.setLayout(new BorderLayout());
    ORDER_MENU.setPreferredSize(new Dimension(364, 544));
    ORDER_MENU.add(menuScrollPane, BorderLayout.CENTER);

    // Force revalidate and repaint for the ORDER_MENU panel
    SwingUtilities.invokeLater(() -> {
        ORDER_MENU.revalidate();
        ORDER_MENU.repaint();
    });
}

// Helper method to load data into the container panel using GridBagLayout
private void loadDataIntoPanel(String dbUrl, String dbUser, String dbPassword, String query, JPanel targetPanel, GridBagConstraints gbc, int column) {
    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        boolean dataLoaded = false; // Flag to check if data is loaded
        int row = 0; // Row counter for GridBagLayout

        while (rs.next()) {
            String itemName = rs.getString("mealName");
            double itemPrice = rs.getDouble("mealPrice");
            String imagePath = rs.getString("imagePath");

            // Debugging: Print out the data loaded from the database
            System.out.println("Loaded item: " + itemName + " - Price: " + itemPrice);

            // Create a panel for each item
            JPanel itemPanel = createMealPanel(itemName, itemPrice, imagePath);

            // Add mouse listener to handle item clicks and add to the order
            itemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    addToOrder(itemName, itemPrice); // Add item to order on click
                }
            });

            // Add the item panel to the container panel in the specified column (column 0 for meals, column 1 for drinks)
            gbc.gridx = column;  // Set the column index for items
            gbc.gridy = row;  // Set the row index for items
            targetPanel.add(itemPanel, gbc);

            // Update GridBagConstraints for the next item in the same column
            row++;

            dataLoaded = true;
        }

        // If no data was loaded, show a message
        if (!dataLoaded) {
            JLabel noDataLabel = new JLabel("No items available");
            targetPanel.add(noDataLabel, gbc);
        }

        // Refresh the targetPanel layout after all items are added
        targetPanel.revalidate();
        targetPanel.repaint();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        e.printStackTrace(); // Print error to the console for debugging
    }
}

// Helper method to create a panel for each meal or drink item
private JPanel createMealPanel(String itemName, double itemPrice, String imagePath) {
    // Main container for the item
    JPanel itemPanel = new JPanel();
    itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
    itemPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    itemPanel.setPreferredSize(new Dimension(150, 150)); // Adjust dimensions as needed

    // Image label
    JLabel imageLabel = new JLabel();
    imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    imageLabel.setPreferredSize(new Dimension(100, 100));

    try {
        ImageIcon icon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        imageLabel.setIcon(icon);
    } catch (Exception e) {
        imageLabel.setText("No Image");
    }

    // Text label for name and price
    JLabel textLabel = new JLabel(String.format("<html><center><b>%s</b><br>Price: %.2f</center></html>", itemName, itemPrice));
    textLabel.setHorizontalAlignment(SwingConstants.CENTER);

    // Add components to the item panel
    itemPanel.add(imageLabel);
    itemPanel.add(textLabel);

    // Return the item panel
    return itemPanel;
}


    // Add a meal to the order panel
    private void addToOrder(String mealName, double mealPrice) {
        boolean itemExists = false;
    
    // Database connection parameters
    String dbUrl = "jdbc:mysql://localhost:3306/ultra";
    String dbUser = "root";
    String dbPassword = "Password123!";

    // Check if the item already exists in the order panel
    for (Component comp : ORDER.getComponents()) {
        if (comp instanceof JLabel) {
            JLabel existingLabel = (JLabel) comp;
            String labelText = existingLabel.getText();

            if (labelText.contains(mealName)) {
                itemExists = true;

                // Extract current quantity from label text
                String[] parts = labelText.split("Qty: ");
                int currentQty = parts.length > 1 ? Integer.parseInt(parts[1].split("<")[0]) : 1;
                int updatedQty = currentQty + 1;
                double updatedTotalPrice = updatedQty * mealPrice;

                // Update the label with the new quantity and total
                existingLabel.setText(String.format(
                    "<html><b>%s</b><br>Price: %.2f<br>Qty: %d<br>Total: %.2f</html>",
                    mealName, mealPrice, updatedQty, updatedTotalPrice
                ));

                // Save the updated order to the database
                updateOrderInDatabase(mealName, updatedQty, updatedTotalPrice);
                break;
            }
        }
    }

    // If the item does not exist in the order, add it to the panel and the database
    if (!itemExists) {
        JLabel newMealLabel = new JLabel(String.format(
            "<html><b>%s</b><br>Price: %.2f<br>Qty: 1<br>Total: %.2f</html>",
            mealName, mealPrice, mealPrice
        ));
        newMealLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        newMealLabel.setHorizontalAlignment(SwingConstants.LEFT);
        newMealLabel.setPreferredSize(new Dimension(338, 100));

        // Add the new meal to the panel
        ORDER.setLayout(new BoxLayout(ORDER, BoxLayout.Y_AXIS));
        ORDER.add(newMealLabel);

        // Save the new order to the database
        saveOrderToDatabase(mealName, 1, mealPrice);
    }

    ORDER.revalidate();
    ORDER.repaint();
}
    private void saveOrderToDatabase(String mealName, int quantity, double totalPrice) {
    // Modify the insert query to include the "status" column
    String insertQuery = "INSERT INTO orders (mealName, mealPrice, quantity, totalPrice, status) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ultra", "root", "Password123!");
         PreparedStatement pst = conn.prepareStatement(insertQuery)) {

        pst.setString(1, mealName);
        pst.setDouble(2, totalPrice / quantity); // Meal price divided by quantity
        pst.setInt(3, quantity);
        pst.setDouble(4, totalPrice);
        pst.setString(5, "pending"); // Set the status as 'pending'

        pst.executeUpdate();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error saving order to database: " + e.getMessage());
    }
}


// Method to update the existing order in the database
private void updateOrderInDatabase(String mealName, int quantity, double totalPrice) {
    String updateQuery = "UPDATE orders SET quantity = ?, totalPrice = ? WHERE mealName = ?";

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ultra", "root", "Password123!");
         PreparedStatement pst = conn.prepareStatement(updateQuery)) {

        pst.setInt(1, quantity);
        pst.setDouble(2, totalPrice);
        pst.setString(3, mealName);

        pst.executeUpdate();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error updating order in database: " + e.getMessage());
    }
}

    // Clear all components from a panel
    private void clearPanel(JPanel panel) {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();
    }

    // Generate and display the receipt
    private void generateReceipt() {
    try {
        totalPrice = 0.0; // Reset total price
        StringBuilder receipt = new StringBuilder();

        // Header
        receipt.append("*******************************************\n");
        receipt.append("           LUNAR RAMEN\n");
        receipt.append("           THANK YOU FOR ORDERING\n");
        receipt.append("*******************************************\n\n");
        receipt.append("ITEMS:\n");

        boolean itemsFound = false;

        // Process each meal in the order
        for (Component comp : ORDER.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel mealLabel = (JLabel) comp;
                String labelText = mealLabel.getText().replaceAll("<[^>]*>", ""); // Remove HTML tags
                receipt.append(labelText).append("\n");

                // Calculate the total from the label
                if (labelText.contains("Total:")) {
                    String[] parts = labelText.split("Total:");
                    if (parts.length > 1) {
                        try {
                            double itemTotal = Double.parseDouble(parts[1].trim());
                            totalPrice += itemTotal;
                            itemsFound = true;
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid price format: " + parts[1]);
                        }
                    }
                }
            }
        }

        if (!itemsFound) {
            JOptionPane.showMessageDialog(this, "No items found! Please add items to the order.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add total section
        receipt.append("\n*******************************************\n");
        receipt.append(String.format("TOTAL: %.2f\n", totalPrice));
        receipt.append("*******************************************\n");

        // Display the receipt in the text area
        jTextArea1.setText(receipt.toString());

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error generating receipt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Add payment details to the receipt
public void displayPaymentDetails(String cash, String change) {
   try {
        // Append the payment details to the current receipt content in jTextArea1
        StringBuilder receipt = new StringBuilder(jTextArea1.getText());

        // Show the payment summary dialog
        JOptionPane.showMessageDialog(this,
            "Payment Successful!\nCASH: " + cash + "\nCHANGE: " + change,
            "Payment Summary",
            JOptionPane.INFORMATION_MESSAGE);
        
        // Add payment details and thank you message
        receipt.append("\n*******************************************\n");
        receipt.append(String.format("CASH: %s\n", cash));
        receipt.append(String.format("CHANGE: %s\n", change));
        receipt.append("*******************************************\n");
        receipt.append("\n*************THANK YOU*******************\n");

        // Update the jTextArea with the full receipt content
        jTextArea1.setText(receipt.toString());

    } catch (Exception e) {
        // Show an error message if something goes wrong
        JOptionPane.showMessageDialog(this, "Error displaying payment details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


private double extractTotalFromReceipt() {
    String[] lines = jTextArea1.getText().split("\n"); // Split text area content by lines
    for (String line : lines) {
        if (line.startsWith("Total:")) { // Assuming the line starts with "Total:"
            try {
                return Double.parseDouble(line.replace("Total:", "").trim()); // Extract and parse the total
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
    return 0; // Default if no total found
}
    
    
     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        ORDER_MENU = new javax.swing.JPanel();
        ADD_MENU = new javax.swing.JButton();
        ORDER_SCROLL = new javax.swing.JScrollPane();
        ORDER = new javax.swing.JPanel();
        RESIBO = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        PAY_BILLS = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setBackground(new java.awt.Color(204, 0, 0));

        javax.swing.GroupLayout ORDER_MENULayout = new javax.swing.GroupLayout(ORDER_MENU);
        ORDER_MENU.setLayout(ORDER_MENULayout);
        ORDER_MENULayout.setHorizontalGroup(
            ORDER_MENULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        ORDER_MENULayout.setVerticalGroup(
            ORDER_MENULayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 331, Short.MAX_VALUE)
        );

        ADD_MENU.setText("ADD MENU");
        ADD_MENU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ADD_MENUActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ORDERLayout = new javax.swing.GroupLayout(ORDER);
        ORDER.setLayout(ORDERLayout);
        ORDERLayout.setHorizontalGroup(
            ORDERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 349, Short.MAX_VALUE)
        );
        ORDERLayout.setVerticalGroup(
            ORDERLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 542, Short.MAX_VALUE)
        );

        ORDER_SCROLL.setViewportView(ORDER);

        RESIBO.setText("ORDER KO KUNIN NYO NA");
        RESIBO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RESIBOActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        PAY_BILLS.setText("PAY BILLS");
        PAY_BILLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PAY_BILLSActionPerformed(evt);
            }
        });

        jButton3.setText("ACCOUNT");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(98, 98, 98)
                .addComponent(ORDER_MENU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(ORDER_SCROLL, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                                .addContainerGap())
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ADD_MENU)
                                    .addComponent(PAY_BILLS)
                                    .addComponent(jButton3))
                                .addGap(208, 208, 208))))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addComponent(RESIBO)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addComponent(PAY_BILLS)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ADD_MENU)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(ORDER_SCROLL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(RESIBO, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ORDER_MENU, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(966, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ADD_MENUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ADD_MENUActionPerformed
        setVisible(false);
        ADD_MENU addMenuwindow = new ADD_MENU();
        addMenuwindow.setVisible(true);
    }//GEN-LAST:event_ADD_MENUActionPerformed

    private void RESIBOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RESIBOActionPerformed
        generateReceipt();

    }//GEN-LAST:event_RESIBOActionPerformed

    private void PAY_BILLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PAY_BILLSActionPerformed
        try {
        PAY_BILLS payBillsFrame = new PAY_BILLS(totalPrice, this);
        payBillsFrame.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error retrieving total: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    }//GEN-LAST:event_PAY_BILLSActionPerformed

    public static void main(String args[]) {
        
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(POS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(POS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(POS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(POS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new POS().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ADD_MENU;
    private javax.swing.JPanel ORDER;
    private javax.swing.JPanel ORDER_MENU;
    private javax.swing.JScrollPane ORDER_SCROLL;
    private javax.swing.JButton PAY_BILLS;
    private javax.swing.JButton RESIBO;
    private javax.swing.JButton jButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables
}
