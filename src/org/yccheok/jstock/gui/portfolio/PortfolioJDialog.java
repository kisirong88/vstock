/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2010 Yan Cheng CHEOK <yccheok@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

/*
 * PortfolioJDialog.java
 *
 * Created on Sep 23, 2009, 4:26:50 AM
 */

package org.yccheok.jstock.gui.portfolio;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import org.yccheok.jstock.engine.Country;
import org.yccheok.jstock.gui.JStockOptions;
import org.yccheok.jstock.gui.MainFrame;
import org.yccheok.jstock.gui.Utils;
import org.yccheok.jstock.internationalization.MessagesBundle;

/**
 *
 * @author yccheok
 */
public class PortfolioJDialog extends javax.swing.JDialog {

    /** Creates new form PortfolioJDialog */
    public PortfolioJDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        init();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jXHeader1 = new org.jdesktop.swingx.JXHeader();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/yccheok/jstock/data/gui"); // NOI18N
        setTitle(bundle.getString("PortfolioJDialog_MultiplePortfolios")); // NOI18N
        setResizable(false);
        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));

        jXHeader1.setDescription(bundle.getString("PortfolioJDialog_Description")); // NOI18N
        jXHeader1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32x32/calc.png"))); // NOI18N
        jXHeader1.setTitle(bundle.getString("PortfolioJDialog_MultiplePortfolios")); // NOI18N
        getContentPane().add(jXHeader1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.BorderLayout(5, 5));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/filenew.png"))); // NOI18N
        jButton1.setText(bundle.getString("New...")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/editdelete.png"))); // NOI18N
        jButton2.setText(bundle.getString("Delete")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/edit.png"))); // NOI18N
        jButton3.setText(bundle.getString("Rename...")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16x16/apply.png"))); // NOI18N
        jButton4.setText(bundle.getString("OK")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton4);

        jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PortfolioJDialog_Portfolio"))); // NOI18N
        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setCellRenderer(getListCellRenderer());
        this.jList1.setModel(new DefaultListModel());
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel4, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-362)/2, (screenSize.height-341)/2, 362, 341);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        final int select = jList1.getSelectedIndex();
        if (select < 0 || select >= jList1.getModel().getSize()) {
            JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_you_must_select_portfolio"), MessagesBundle.getString("warning_title_you_must_select_portfolio"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        final String oldPortfolioName = (String)jList1.getSelectedValue();
        if (oldPortfolioName == null) {
            JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_you_must_select_portfolio"), MessagesBundle.getString("warning_title_you_must_select_portfolio"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newPortfolioName = null;

        boolean needToReload = false;
        final MainFrame mainFrame = MainFrame.getInstance();
        if ( mainFrame.getJStockOptions().getPortfolioName().equals(oldPortfolioName)) {
            needToReload = true;
        }

        root:
        while (true) {
            newPortfolioName = JOptionPane.showInputDialog(this, MessagesBundle.getString("info_message_enter_rename_portfolio_name"), oldPortfolioName);

            if (newPortfolioName == null) {
                return;
            }

            // Make it same rule as Android's
            if (newPortfolioName.length() > 50) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_invalid_portfolio_name"), MessagesBundle.getString("warning_title_invalid_portfolio_name"), JOptionPane.WARNING_MESSAGE);
                continue;                
            }
            
            newPortfolioName = newPortfolioName.trim();
            
            if (newPortfolioName.isEmpty()) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_you_need_to_specific_portfolio_name"), MessagesBundle.getString("warning_title_you_need_to_specific_portfolio_name"), JOptionPane.WARNING_MESSAGE);
                continue;
            }

            if (isValidFolderName(newPortfolioName) == false) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_invalid_portfolio_name"), MessagesBundle.getString("warning_title_invalid_portfolio_name"), JOptionPane.WARNING_MESSAGE);
                continue;
            }
            
            if (Utils.isFileOrDirectoryExist(org.yccheok.jstock.portfolio.Utils.getPortfolioDirectory(newPortfolioName))) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_already_a_portfolio_with_same_name"), MessagesBundle.getString("warning_title_already_a_portfolio_with_same_name"), JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // In Linux, creating "My Portfolio" and "my portfolio" are allowed. We
            // want to prevent this from happening, as user might upload such 2 folders
            // in Linux, and download into Windows.
            final JStockOptions jStockOptions = MainFrame.getInstance().getJStockOptions();
            final File file = new File(org.yccheok.jstock.gui.Utils.getUserDataDirectory() +  jStockOptions.getCountry() + File.separator + "portfolios" + File.separator);
            File[] children = file.listFiles();   
            if (children != null) {
                for (File f : children) {
                    if (newPortfolioName.equalsIgnoreCase(f.getName())) {
                        JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_already_a_portfolio_with_same_name"), MessagesBundle.getString("warning_title_already_a_portfolio_with_same_name"), JOptionPane.WARNING_MESSAGE);
                        continue root;
                    }
                }
            }

            File oldFile = new File(org.yccheok.jstock.portfolio.Utils.getPortfolioDirectory(oldPortfolioName));
            File newFile = new File(org.yccheok.jstock.portfolio.Utils.getPortfolioDirectory(newPortfolioName));

            if (false == oldFile.renameTo(newFile))
            {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("error_message_unknown_error_during_renaming"), MessagesBundle.getString("error_title_unknown_error_during_renaming"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            else
            {
                if (needToReload) {
                    jStockOptions.setPortfolioName(newPortfolioName);
                }
                init();
            }

            break;
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        final int select = jList1.getSelectedIndex();
        if (select < 0 || select >= jList1.getModel().getSize()) {
            JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_you_must_select_portfolio"), MessagesBundle.getString("warning_title_you_must_select_portfolio"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        final String selectedValue = (String)jList1.getSelectedValue();
        if (selectedValue == null) {
            JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_you_must_select_portfolio"), MessagesBundle.getString("warning_title_you_must_select_portfolio"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        final MainFrame mainFrame = MainFrame.getInstance();
        final JStockOptions jStockOptions = mainFrame.getJStockOptions();
        if (jStockOptions.getPortfolioName().equals(selectedValue)) {
            JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_cannot_delete_current_active_portflio"), MessagesBundle.getString("warning_title_cannot_delete_current_active_portflio"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        final String output = MessageFormat.format(MessagesBundle.getString("question_message_delete_template"), selectedValue);
        final int result = javax.swing.JOptionPane.showConfirmDialog(MainFrame.getInstance(), output, MessagesBundle.getString("question_title_delete"), javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (result != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        if (org.yccheok.jstock.gui.Utils.deleteDir(new File(org.yccheok.jstock.portfolio.Utils.getPortfolioDirectory(selectedValue))))
        {
            init();
        }
        else
        {
            JOptionPane.showMessageDialog(this, MessagesBundle.getString("error_message_unknown_error_during_delete"), MessagesBundle.getString("error_title_unknown_error_during_delete"), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String newPortfolioName = null;
        root:
        while (true) {
            newPortfolioName = JOptionPane.showInputDialog(this, MessagesBundle.getString("info_message_enter_new_portfolio_name"));

            if (newPortfolioName == null) {
                return;
            }

            // Make it same rule as Android's
            if (newPortfolioName.length() > 50) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_invalid_portfolio_name"), MessagesBundle.getString("warning_title_invalid_portfolio_name"), JOptionPane.WARNING_MESSAGE);
                continue;                
            }
                
            newPortfolioName = newPortfolioName.trim();
            
            if (newPortfolioName.length() <= 0) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_you_need_to_specific_portfolio_name"), MessagesBundle.getString("warning_title_you_need_to_specific_portfolio_name"), JOptionPane.WARNING_MESSAGE);
                continue;
            }

            if (isValidFolderName(newPortfolioName) == false) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_invalid_portfolio_name"), MessagesBundle.getString("warning_title_invalid_portfolio_name"), JOptionPane.WARNING_MESSAGE);
                continue;
            }
            
            if (Utils.isFileOrDirectoryExist(org.yccheok.jstock.portfolio.Utils.getPortfolioDirectory(newPortfolioName))) {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_already_a_portfolio_with_same_name"), MessagesBundle.getString("warning_title_already_a_portfolio_with_same_name"), JOptionPane.WARNING_MESSAGE);
                continue;
            }

            // In Linux, creating "My Portfolio" and "my portfolio" are allowed. We
            // want to prevent this from happening, as user might upload such 2 folders
            // in Linux, and download into Windows.
            final JStockOptions jStockOptions = MainFrame.getInstance().getJStockOptions();
            final File file = new File(org.yccheok.jstock.gui.Utils.getUserDataDirectory() +  jStockOptions.getCountry() + File.separator + "portfolios" + File.separator);
            File[] children = file.listFiles(); 
            if (children != null) {
                for (File f : children) {
                    if (newPortfolioName.equalsIgnoreCase(f.getName())) {
                        JOptionPane.showMessageDialog(this, MessagesBundle.getString("warning_message_already_a_portfolio_with_same_name"), MessagesBundle.getString("warning_title_already_a_portfolio_with_same_name"), JOptionPane.WARNING_MESSAGE);
                        continue root;
                    }
                }
            }
            
            if (false == org.yccheok.jstock.portfolio.Utils.createEmptyPortfolio(newPortfolioName))
            {
                JOptionPane.showMessageDialog(this, MessagesBundle.getString("error_message_unknown_error_during_new"), MessagesBundle.getString("error_title_unknown_error_during_new"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            else
            {
                init();
            }
            break;
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        final JList list = (JList)evt.getSource();
        // Double-click
        if (evt.getClickCount() == 2) {          
            // Get item index
            final int index = list.locationToIndex(evt.getPoint());
            final String portfolio = list.getModel().getElementAt(index).toString();
            final JStockOptions jStockOptions = MainFrame.getInstance().getJStockOptions();
            if (jStockOptions.getPortfolioName().equals(portfolio) == false) {
                MainFrame.getInstance().selectActivePortfolio(portfolio);
                // Ensure Bold effect on active portfolio.    
                this.jList1.repaint();
            }
        }
    }//GEN-LAST:event_jList1MouseClicked

    private void init() {
        ((DefaultListModel)(this.jList1.getModel())).clear();
        final List<String> names = org.yccheok.jstock.portfolio.Utils.getPortfolioNames();
        for (String name : names) {
            ((DefaultListModel)(this.jList1.getModel())).addElement(name);
        }
    }
    
    private ListCellRenderer getListCellRenderer() {
        return new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (component != null && value != null) {
                    final MainFrame mainFrame = MainFrame.getInstance();
                    final JStockOptions jStockOptions = mainFrame.getJStockOptions();
                    final String portfolioName = jStockOptions.getPortfolioName();

                    if (value.toString().equals(portfolioName)) {
                        final Font oldFont = component.getFont();
                        component.setFont(Utils.getBoldFont(oldFont));
                    }
                }
                return component;
            }
        };
    }
    
    private static boolean isValidFolderName(String folderName) {
        char[] chars = folderName.toCharArray();
        for (char c : chars) {
            if (ILLEGAL_CHARACTERS.contains(c)) {
                return false;
            }
        }
        return true;
    }
    
    // http://stackoverflow.com/questions/893977/java-how-to-find-out-whether-a-file-name-is-valid
    private static final Set<Character> ILLEGAL_CHARACTERS = new HashSet<Character>(Arrays.asList('/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'));
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXHeader jXHeader1;
    // End of variables declaration//GEN-END:variables

}