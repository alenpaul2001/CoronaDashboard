/*
 * Copyright (C) 2021 AlenPaulVarghese <alenpaul2001@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.alenpaul2001.coronadashboard;

import Db.Database;
import Http.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author AlenPaulVarghese <alenpaul2001@gmail.com>
 */
public class Main extends javax.swing.JFrame {

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
    }

    // function to refresh covid statitics
    private void refreshEntrys() {
        // first we get the already exisiting mouse listerner of both refresh icon to safe keep
        java.awt.event.MouseListener mouse_listener = home_refresh_icon.getMouseListeners()[0];
        /**
         * then we remove both of the mouse listener why? cause we don't want
         * the user to spam the refresh icon in this way the user can only call
         * the refresh function only after the previous one is finished.
         */
        home_refresh_icon.removeMouseListener(mouse_listener);
        db_refresh_icon.removeMouseListener(mouse_listener);
        // then we set fetching api message to both information panel
        this.setInformation(new java.awt.Color(198, 246, 213), "fetching api...", null);
        try {
            // this function fetches the api and returns the json -> java Object version
            Response res = Request.request();
            // now we set `writing into database` message to both information panel.
            this.setInformation(new java.awt.Color(198, 246, 213), "writing into database...", null);
            // expicitly add global as a country
            res.global.countryName = "Global";
            res.global.countryCode = "GL";
            // add global in front of the countrys List
            res.countries.add(0, res.global);
            // we update the database with new entries
            Database.updateCountries(res.countries);
            // now we set `updating components` message to both information panel.
            this.setInformation(new java.awt.Color(198, 246, 213), "updating components...", null);
            Thread.sleep(1000);
            // following codes will update both panels then sleep for 2s then hide the info panels
            Connection db = Database.getConnection();
            try {
                this.loadDashboard(db, false);
                this.loadTable(db);
            } finally {
                db.close();
            }
            this.setInformation(new java.awt.Color(198, 246, 213), "successfully refreshed data...", null);
            Thread.sleep(2000);
            this.setInformation(null, null, null);
        } catch (SQLException ex) {
            // handling database related errors
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Failed writing to database",
                    ex.getMessage()
            );
        } catch (java.net.UnknownHostException ex) {
            // handling connectivity related errors.
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Failed connecting to internet",
                    ex.getMessage()
            );
        } catch (Exception ex) {
            // catch general errors.
            System.out.println(ex.getMessage());
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    ex.getMessage(),
                    ex.getMessage()
            );
        } finally {
            // finnaly we change the color of refresh icon to normal
            this.home_refresh_icon_area.setBackground(new java.awt.Color(88, 104, 220));
            this.db_refresh_icon_area.setBackground(new java.awt.Color(88, 104, 220));
            // and add the mouse  listerner we safe keeped before.
            home_refresh_icon.addMouseListener(mouse_listener);
            db_refresh_icon.addMouseListener(mouse_listener);
        }

    }

    // Hover animation to change color when mouse entered
    public void hoverAnimation(boolean exit, java.awt.event.MouseEvent evt, java.awt.Color c) {
        javax.swing.JLabel lbl = (javax.swing.JLabel) evt.getComponent();
        javax.swing.JPanel pnl = (javax.swing.JPanel) lbl.getParent();
        // if else onliner; basically this means if exit is true then use the foreground color else color
        java.awt.Color color = ((exit == true) ? lbl.getForeground() : c);
        // https://gist.github.com/TabsPH/4057899
        pnl.setBackground(color);
    }

    // this function literraly adds hover animation to every icons
    public void addMouseEvent() {
        // for refresh icon
        java.awt.event.MouseAdapter refresh_adapter = new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hoverAnimation(false, evt, new java.awt.Color(3, 218, 198));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                hoverAnimation(true, evt, null);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // run the function in a new thread.
                // why? we don't want the function to block our main thread,
                // this way we can interact with the ui even though the
                // function is running..,
                new Thread(() -> refreshEntrys()).start();
            }
        };
        home_refresh_icon.addMouseListener(refresh_adapter);
        db_refresh_icon.addMouseListener(refresh_adapter);
        for (javax.swing.JLabel label : new javax.swing.JLabel[]{
            home_dashboard_icon,
            home_settings_icon,
            home_database_icon,
            home_about_icon,
            db_dashboard_icon,
            db_database_icon,
            db_settings_icon,
            db_about_icon,
            stg_dashboard_icon,
            stg_database_icon,
            stg_settings_icon,
            stg_about_icon}) {
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    hoverAnimation(false, evt, new java.awt.Color(88, 104, 220));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    hoverAnimation(true, evt, null);
                }
            });
        }
    }

    public void loadModel(String countryname) {
        try {
            Connection db = Database.getConnection();
            try {
                ResultSet result = Database.queryCountry(db, countryname);
                result.next();
                confirmed_text.setText(String.valueOf(result.getInt("confirmed")));
                recovered_text.setText(String.valueOf(result.getInt("recovered")));
                death_text.setText(String.valueOf(result.getInt("death")));
            } finally {
                db.close();
            }
        } catch (SQLException ex) {
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Could not connect into database",
                    ex.getMessage()
            );
        }
    }

    public void loadDashboard(Connection db, boolean autorefresh) throws SQLException {
        ResultSet result = Database.getSettings(db, false);
        result.next();
        String default_country = result.getString("defaultcountry");
        stg_auto_refresh_checkbox.setSelected(result.getBoolean("autorefresh"));
        this.loadModel(default_country);
        if (result.getBoolean("autorefresh") && autorefresh) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    System.out.println("refresh failed");
                }
                refreshEntrys();
            }
            ).start();
        }
        javax.swing.DefaultComboBoxModel model = new javax.swing.DefaultComboBoxModel();
        result = Database.queryCountryNames(db);
        while (result.next()) {
            model.addElement(result.getString(1));
        }
        model.setSelectedItem(default_country);
        result = Database.getIndex(db);
        while (result.next()) {
            total_country_placeholder.setText(String.valueOf(result.getInt(1)));
            most_confirmed_placeholder.setText(result.getString(2));
            most_recovered_placeholder.setText(result.getString(3));
            most_death_placeholder.setText(result.getString(4));
        }
        result.close();
        home_stat_combo_box.setModel(model);
        stg_stat_combo_box.setModel(model);
        home_stat_combo_box.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == 1) {
                    loadModel(evt.getItem().toString());
                }
            }
        });
        home_stat_clear_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadModel("Global");
                home_stat_combo_box.setSelectedIndex(0);
            }
        });

        stg_stat_clear_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stg_stat_combo_box.setSelectedIndex(0);
            }
        });
    }

    public void loadTable(Connection db) throws SQLException {
        ResultSet result = Database.queryCountries(db);
        DefaultTableModel model = (DefaultTableModel) db_table_panel.getModel();
        db_table_panel.setAutoCreateRowSorter(false);
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        while (result.next()) {
            model.addRow(new Object[]{
                result.getString("countryname"),
                result.getString("countrycode"),
                result.getInt("confirmed"),
                result.getInt("recovered"),
                result.getInt("death")
            });
        }
        // setting this here to avoid duplicate entries
        db_table_panel.setAutoCreateRowSorter(true);
    }

    public void loadSettings() {
        stg_save_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Connection db = Database.getConnection();
                    Database.updateSettings(
                            String.valueOf(stg_stat_combo_box.getSelectedItem()),
                            stg_auto_refresh_checkbox.isSelected(),
                            db
                    );
                    JOptionPane.showMessageDialog(null, "Updated");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Database Connection Failure");
                }
            }
        });
        stg_cancel_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Connection db = Database.getConnection();
                    ResultSet result = Database.getSettings(db, false);
                    result.next();
                    stg_stat_combo_box.setSelectedItem(result.getString("defaultcountry"));
                    stg_auto_refresh_checkbox.setSelected(result.getBoolean("autorefresh"));
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Database Connection Failure");
                }
            }
        });
        stg_default_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Connection db = Database.getConnection();
                    Database.createDefaultSettings(db);
                    stg_stat_combo_box.setSelectedIndex(0);
                    stg_auto_refresh_checkbox.setSelected(false);
                    JOptionPane.showMessageDialog(null, "Settings set to default");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Database Connection Failure");
                }
            }
        });
    }

    private void setInformation(java.awt.Color color, String message, String error) {
        if (color == null) {
            home_information_panel.setBackground(new java.awt.Color(238, 238, 237));
            db_information_panel.setBackground(new java.awt.Color(34, 41, 57));
            stg_information_panel.setBackground(new java.awt.Color(27, 29, 36));
            home_information_text_area.setText(null);
            db_information_text_area.setText(null);
            stg_information_text_area.setText(null);
            home_information_text_area.setToolTipText(null);
            db_information_text_area.setToolTipText(null);
            stg_information_text_area.setToolTipText(null);
        } else {
            home_information_panel.setBackground(color);
            db_information_panel.setBackground(color);
            stg_information_panel.setBackground(color);
            home_information_text_area.setText(message);
            db_information_text_area.setText(message);
            stg_information_text_area.setText(message);
            home_information_text_area.setToolTipText(error);
            db_information_text_area.setToolTipText(error);
            stg_information_text_area.setToolTipText(error);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dashboard_panel = new javax.swing.JPanel();
        home_side_panel = new javax.swing.JPanel();
        home_app_icon_area = new javax.swing.JPanel();
        home_app_icon = new javax.swing.JLabel();
        home_dashboard_icon_area = new javax.swing.JPanel();
        home_dashboard_icon = new javax.swing.JLabel();
        home_database_icon_area = new javax.swing.JPanel();
        home_database_icon = new javax.swing.JLabel();
        home_settings_icon_area = new javax.swing.JPanel();
        home_settings_icon = new javax.swing.JLabel();
        home_about_icon_area = new javax.swing.JPanel();
        home_about_icon = new javax.swing.JLabel();
        home_refresh_icon_area = new javax.swing.JPanel();
        home_refresh_icon = new javax.swing.JLabel();
        home_recovered_main = new javax.swing.JPanel();
        home_recovered_text_area = new javax.swing.JPanel();
        recovered_text = new javax.swing.JLabel();
        home_text_recovered = new javax.swing.JLabel();
        home_death_main = new javax.swing.JPanel();
        home_death_text_area = new javax.swing.JPanel();
        death_text = new javax.swing.JLabel();
        home_text_death = new javax.swing.JLabel();
        home_confirmed_main = new javax.swing.JPanel();
        home_confirmed_text_area = new javax.swing.JPanel();
        confirmed_text = new javax.swing.JLabel();
        home_text_confirmed = new javax.swing.JLabel();
        home_free_panel = new javax.swing.JPanel();
        home_information_panel = new javax.swing.JPanel();
        home_information_text_area = new javax.swing.JLabel();
        home_database_index_area = new javax.swing.JPanel();
        database_index_text = new javax.swing.JLabel();
        database_index_underline = new javax.swing.JSeparator();
        total_countries_text = new javax.swing.JLabel();
        most_confirmed_text = new javax.swing.JLabel();
        most_deaths_text = new javax.swing.JLabel();
        most_recovered_text = new javax.swing.JLabel();
        total_country_placeholder = new javax.swing.JLabel();
        most_death_placeholder = new javax.swing.JLabel();
        most_confirmed_placeholder = new javax.swing.JLabel();
        most_recovered_placeholder = new javax.swing.JLabel();
        key_value_separator = new javax.swing.JSeparator();
        home_stats_area = new javax.swing.JPanel();
        home_stat_combo_box = new javax.swing.JComboBox<>();
        home_stats_area_text = new javax.swing.JLabel();
        home_stat_clear_button = new javax.swing.JButton();
        database_panel = new javax.swing.JPanel();
        db_side_panel = new javax.swing.JPanel();
        db_app_icon_area = new javax.swing.JPanel();
        db_app_icon = new javax.swing.JLabel();
        db_dashboard_icon_area = new javax.swing.JPanel();
        db_dashboard_icon = new javax.swing.JLabel();
        db_database_icon_area = new javax.swing.JPanel();
        db_database_icon = new javax.swing.JLabel();
        db_settings_icon_area = new javax.swing.JPanel();
        db_settings_icon = new javax.swing.JLabel();
        db_about_icon_area = new javax.swing.JPanel();
        db_about_icon = new javax.swing.JLabel();
        db_refresh_icon_area = new javax.swing.JPanel();
        db_refresh_icon = new javax.swing.JLabel();
        db_free_panel = new javax.swing.JPanel();
        db_operator_combobox = new javax.swing.JComboBox<>();
        db_field_combobox = new javax.swing.JComboBox<>();
        db_filter_keyword = new javax.swing.JTextField();
        db_filter_button = new javax.swing.JButton();
        db_arrow_one = new javax.swing.JLabel();
        db_arrow_two = new javax.swing.JLabel();
        home_stat_clear_button1 = new javax.swing.JButton();
        db_information_panel = new javax.swing.JPanel();
        db_information_text_area = new javax.swing.JLabel();
        db_table_scrollpane = new javax.swing.JScrollPane();
        db_table_panel = new javax.swing.JTable();
        settings_panel = new javax.swing.JPanel();
        stg_side_panel = new javax.swing.JPanel();
        stg_app_icon_area = new javax.swing.JPanel();
        stg_app_icon = new javax.swing.JLabel();
        stg_dashboard_icon_area = new javax.swing.JPanel();
        stg_dashboard_icon = new javax.swing.JLabel();
        stg_database_icon_area = new javax.swing.JPanel();
        stg_database_icon = new javax.swing.JLabel();
        stg_settings_icon_area = new javax.swing.JPanel();
        stg_settings_icon = new javax.swing.JLabel();
        stg_about_icon_area = new javax.swing.JPanel();
        stg_about_icon = new javax.swing.JLabel();
        stg_options_panel = new javax.swing.JPanel();
        stg_stat_combo_box = new javax.swing.JComboBox<>();
        stg_combo_box_text = new javax.swing.JLabel();
        stg_stat_clear_button = new javax.swing.JButton();
        stg_auto_refresh_text = new javax.swing.JLabel();
        stg_auto_refresh_checkbox = new javax.swing.JCheckBox();
        stg_save_button = new javax.swing.JButton();
        stg_cancel_button = new javax.swing.JButton();
        stg_default_button = new javax.swing.JButton();
        stg_information_panel = new javax.swing.JPanel();
        stg_information_text_area = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(new java.awt.CardLayout());

        dashboard_panel.setBackground(new java.awt.Color(238, 238, 237));
        dashboard_panel.setPreferredSize(new java.awt.Dimension(940, 500));

        home_side_panel.setBackground(new java.awt.Color(28, 38, 61));
        home_side_panel.setPreferredSize(new java.awt.Dimension(100, 500));

        home_app_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        home_app_icon_area.setMinimumSize(new java.awt.Dimension(100, 70));
        home_app_icon_area.setPreferredSize(new java.awt.Dimension(100, 70));

        home_app_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_app_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/covid-50x50.png"))); // NOI18N
        home_app_icon.setToolTipText("");
        home_app_icon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        home_app_icon.setPreferredSize(new java.awt.Dimension(100, 70));

        javax.swing.GroupLayout home_app_icon_areaLayout = new javax.swing.GroupLayout(home_app_icon_area);
        home_app_icon_area.setLayout(home_app_icon_areaLayout);
        home_app_icon_areaLayout.setHorizontalGroup(
            home_app_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_app_icon_areaLayout.createSequentialGroup()
                .addComponent(home_app_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        home_app_icon_areaLayout.setVerticalGroup(
            home_app_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_app_icon_areaLayout.createSequentialGroup()
                .addComponent(home_app_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        home_dashboard_icon_area.setBackground(new java.awt.Color(39, 49, 70));

        home_dashboard_icon.setForeground(new java.awt.Color(39, 49, 70));
        home_dashboard_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_dashboard_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons8-home-24.png"))); // NOI18N
        home_dashboard_icon.setToolTipText("null");
        home_dashboard_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout home_dashboard_icon_areaLayout = new javax.swing.GroupLayout(home_dashboard_icon_area);
        home_dashboard_icon_area.setLayout(home_dashboard_icon_areaLayout);
        home_dashboard_icon_areaLayout.setHorizontalGroup(
            home_dashboard_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_dashboard_icon_areaLayout.createSequentialGroup()
                .addComponent(home_dashboard_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        home_dashboard_icon_areaLayout.setVerticalGroup(
            home_dashboard_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_dashboard_icon_areaLayout.createSequentialGroup()
                .addComponent(home_dashboard_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        home_database_icon_area.setBackground(new java.awt.Color(28, 38, 61));

        home_database_icon.setForeground(new java.awt.Color(28, 38, 61));
        home_database_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_database_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/piechart-30x30.png"))); // NOI18N
        home_database_icon.setToolTipText("");
        home_database_icon.setPreferredSize(new java.awt.Dimension(100, 60));
        home_database_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                home_database_iconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout home_database_icon_areaLayout = new javax.swing.GroupLayout(home_database_icon_area);
        home_database_icon_area.setLayout(home_database_icon_areaLayout);
        home_database_icon_areaLayout.setHorizontalGroup(
            home_database_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, home_database_icon_areaLayout.createSequentialGroup()
                .addComponent(home_database_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        home_database_icon_areaLayout.setVerticalGroup(
            home_database_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_database_icon_areaLayout.createSequentialGroup()
                .addComponent(home_database_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        home_settings_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        home_settings_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        home_settings_icon.setForeground(new java.awt.Color(28, 38, 61));
        home_settings_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_settings_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons8-settings-24.png"))); // NOI18N
        home_settings_icon.setToolTipText("");
        home_settings_icon.setPreferredSize(new java.awt.Dimension(100, 60));
        home_settings_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                home_settings_iconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout home_settings_icon_areaLayout = new javax.swing.GroupLayout(home_settings_icon_area);
        home_settings_icon_area.setLayout(home_settings_icon_areaLayout);
        home_settings_icon_areaLayout.setHorizontalGroup(
            home_settings_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, home_settings_icon_areaLayout.createSequentialGroup()
                .addComponent(home_settings_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        home_settings_icon_areaLayout.setVerticalGroup(
            home_settings_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_settings_icon_areaLayout.createSequentialGroup()
                .addComponent(home_settings_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        home_about_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        home_about_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        home_about_icon.setForeground(new java.awt.Color(28, 38, 61));
        home_about_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_about_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/about-icon31.png"))); // NOI18N
        home_about_icon.setToolTipText("null");
        home_about_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout home_about_icon_areaLayout = new javax.swing.GroupLayout(home_about_icon_area);
        home_about_icon_area.setLayout(home_about_icon_areaLayout);
        home_about_icon_areaLayout.setHorizontalGroup(
            home_about_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, home_about_icon_areaLayout.createSequentialGroup()
                .addComponent(home_about_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        home_about_icon_areaLayout.setVerticalGroup(
            home_about_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_about_icon_areaLayout.createSequentialGroup()
                .addComponent(home_about_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        home_refresh_icon_area.setBackground(new java.awt.Color(88, 104, 220));
        home_refresh_icon_area.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        home_refresh_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        home_refresh_icon.setForeground(new java.awt.Color(88, 104, 220));
        home_refresh_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_refresh_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/baseline_refresh_white_18dp.png"))); // NOI18N
        home_refresh_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout home_refresh_icon_areaLayout = new javax.swing.GroupLayout(home_refresh_icon_area);
        home_refresh_icon_area.setLayout(home_refresh_icon_areaLayout);
        home_refresh_icon_areaLayout.setHorizontalGroup(
            home_refresh_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_refresh_icon_areaLayout.createSequentialGroup()
                .addComponent(home_refresh_icon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        home_refresh_icon_areaLayout.setVerticalGroup(
            home_refresh_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_refresh_icon_areaLayout.createSequentialGroup()
                .addComponent(home_refresh_icon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout home_side_panelLayout = new javax.swing.GroupLayout(home_side_panel);
        home_side_panel.setLayout(home_side_panelLayout);
        home_side_panelLayout.setHorizontalGroup(
            home_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_side_panelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(home_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(home_app_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(home_dashboard_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(home_database_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(home_settings_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(home_about_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(home_refresh_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        home_side_panelLayout.setVerticalGroup(
            home_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_side_panelLayout.createSequentialGroup()
                .addComponent(home_app_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(home_dashboard_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(home_database_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(home_settings_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(home_about_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(106, 106, 106)
                .addComponent(home_refresh_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        home_recovered_main.setBackground(new java.awt.Color(198, 246, 213));
        home_recovered_main.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(56, 161, 105), 3));
        home_recovered_main.setPreferredSize(new java.awt.Dimension(200, 150));
        home_recovered_main.setLayout(new java.awt.BorderLayout());

        home_recovered_text_area.setBackground(new java.awt.Color(240, 255, 244));

        recovered_text.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        recovered_text.setForeground(new java.awt.Color(56, 161, 105));
        recovered_text.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        recovered_text.setText("0");
        recovered_text.setToolTipText("");

        javax.swing.GroupLayout home_recovered_text_areaLayout = new javax.swing.GroupLayout(home_recovered_text_area);
        home_recovered_text_area.setLayout(home_recovered_text_areaLayout);
        home_recovered_text_areaLayout.setHorizontalGroup(
            home_recovered_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 194, Short.MAX_VALUE)
            .addGroup(home_recovered_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(home_recovered_text_areaLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(recovered_text)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        home_recovered_text_areaLayout.setVerticalGroup(
            home_recovered_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
            .addGroup(home_recovered_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(home_recovered_text_areaLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(recovered_text)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        home_recovered_main.add(home_recovered_text_area, java.awt.BorderLayout.PAGE_START);

        home_text_recovered.setFont(new java.awt.Font("Segoe UI Semibold", 1, 24)); // NOI18N
        home_text_recovered.setForeground(new java.awt.Color(56, 161, 105));
        home_text_recovered.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_text_recovered.setText("Recovered");
        home_recovered_main.add(home_text_recovered, java.awt.BorderLayout.CENTER);

        home_death_main.setBackground(new java.awt.Color(226, 232, 240));
        home_death_main.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(113, 128, 150), 3));
        home_death_main.setPreferredSize(new java.awt.Dimension(200, 150));
        home_death_main.setLayout(new java.awt.BorderLayout());

        home_death_text_area.setBackground(new java.awt.Color(237, 242, 247));

        death_text.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        death_text.setForeground(new java.awt.Color(113, 128, 150));
        death_text.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        death_text.setText("0");
        death_text.setToolTipText("");

        javax.swing.GroupLayout home_death_text_areaLayout = new javax.swing.GroupLayout(home_death_text_area);
        home_death_text_area.setLayout(home_death_text_areaLayout);
        home_death_text_areaLayout.setHorizontalGroup(
            home_death_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 194, Short.MAX_VALUE)
            .addGroup(home_death_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(home_death_text_areaLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(death_text)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        home_death_text_areaLayout.setVerticalGroup(
            home_death_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
            .addGroup(home_death_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(home_death_text_areaLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(death_text)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        home_death_main.add(home_death_text_area, java.awt.BorderLayout.PAGE_START);

        home_text_death.setFont(new java.awt.Font("Segoe UI Semibold", 1, 24)); // NOI18N
        home_text_death.setForeground(new java.awt.Color(113, 128, 150));
        home_text_death.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_text_death.setText("Deaths");
        home_death_main.add(home_text_death, java.awt.BorderLayout.CENTER);

        home_confirmed_main.setBackground(new java.awt.Color(254, 215, 215));
        home_confirmed_main.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 62, 62), 3));
        home_confirmed_main.setPreferredSize(new java.awt.Dimension(200, 150));
        home_confirmed_main.setLayout(new java.awt.BorderLayout());

        home_confirmed_text_area.setBackground(new java.awt.Color(255, 245, 245));

        confirmed_text.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        confirmed_text.setForeground(new java.awt.Color(229, 62, 62));
        confirmed_text.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        confirmed_text.setText("0");
        confirmed_text.setToolTipText("");

        javax.swing.GroupLayout home_confirmed_text_areaLayout = new javax.swing.GroupLayout(home_confirmed_text_area);
        home_confirmed_text_area.setLayout(home_confirmed_text_areaLayout);
        home_confirmed_text_areaLayout.setHorizontalGroup(
            home_confirmed_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(confirmed_text, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        );
        home_confirmed_text_areaLayout.setVerticalGroup(
            home_confirmed_text_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(confirmed_text, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );

        home_confirmed_main.add(home_confirmed_text_area, java.awt.BorderLayout.PAGE_START);

        home_text_confirmed.setFont(new java.awt.Font("Segoe UI Semibold", 1, 24)); // NOI18N
        home_text_confirmed.setForeground(new java.awt.Color(229, 62, 62));
        home_text_confirmed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        home_text_confirmed.setText("Confirmed");
        home_confirmed_main.add(home_text_confirmed, java.awt.BorderLayout.CENTER);

        home_free_panel.setPreferredSize(new java.awt.Dimension(840, 280));

        home_information_panel.setBackground(new java.awt.Color(238, 238, 237));
        home_information_panel.setPreferredSize(new java.awt.Dimension(840, 40));

        home_information_text_area.setBackground(new java.awt.Color(238, 238, 237));
        home_information_text_area.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        home_information_text_area.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout home_information_panelLayout = new javax.swing.GroupLayout(home_information_panel);
        home_information_panel.setLayout(home_information_panelLayout);
        home_information_panelLayout.setHorizontalGroup(
            home_information_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_information_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(home_information_text_area, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
                .addContainerGap())
        );
        home_information_panelLayout.setVerticalGroup(
            home_information_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(home_information_text_area, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        home_database_index_area.setBackground(new java.awt.Color(238, 238, 237));
        home_database_index_area.setForeground(new java.awt.Color(0, 0, 0));
        home_database_index_area.setPreferredSize(new java.awt.Dimension(440, 240));

        database_index_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 24)); // NOI18N
        database_index_text.setForeground(new java.awt.Color(0, 0, 0));
        database_index_text.setText("Database Index");

        database_index_underline.setBackground(new java.awt.Color(99, 130, 192));
        database_index_underline.setForeground(new java.awt.Color(99, 130, 192));

        total_countries_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        total_countries_text.setForeground(new java.awt.Color(88, 104, 220));
        total_countries_text.setText("Total Countries ");

        most_confirmed_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        most_confirmed_text.setForeground(new java.awt.Color(113, 128, 150));
        most_confirmed_text.setText("Most Mortality ");

        most_deaths_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        most_deaths_text.setForeground(new java.awt.Color(229, 62, 62));
        most_deaths_text.setText("Most Confirmed ");

        most_recovered_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        most_recovered_text.setForeground(new java.awt.Color(56, 161, 105));
        most_recovered_text.setText("Most Recovered ");

        total_country_placeholder.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        total_country_placeholder.setForeground(new java.awt.Color(0, 0, 0));
        total_country_placeholder.setText("0");
        total_country_placeholder.setToolTipText("");
        total_country_placeholder.setPreferredSize(new java.awt.Dimension(200, 22));

        most_death_placeholder.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        most_death_placeholder.setForeground(new java.awt.Color(0, 0, 0));
        most_death_placeholder.setText("Global");
        most_death_placeholder.setPreferredSize(new java.awt.Dimension(200, 22));

        most_confirmed_placeholder.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        most_confirmed_placeholder.setForeground(new java.awt.Color(0, 0, 0));
        most_confirmed_placeholder.setText("Global");
        most_confirmed_placeholder.setPreferredSize(new java.awt.Dimension(200, 22));

        most_recovered_placeholder.setFont(new java.awt.Font("Segoe UI Semibold", 1, 16)); // NOI18N
        most_recovered_placeholder.setForeground(new java.awt.Color(0, 0, 0));
        most_recovered_placeholder.setText("Global");
        most_recovered_placeholder.setPreferredSize(new java.awt.Dimension(200, 22));

        key_value_separator.setBackground(new java.awt.Color(99, 130, 191));
        key_value_separator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        key_value_separator.setMinimumSize(new java.awt.Dimension(50, 2));
        key_value_separator.setPreferredSize(new java.awt.Dimension(10, 160));

        javax.swing.GroupLayout home_database_index_areaLayout = new javax.swing.GroupLayout(home_database_index_area);
        home_database_index_area.setLayout(home_database_index_areaLayout);
        home_database_index_areaLayout.setHorizontalGroup(
            home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_database_index_areaLayout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(database_index_underline, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(database_index_text, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(home_database_index_areaLayout.createSequentialGroup()
                        .addGroup(home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(most_recovered_text)
                            .addGroup(home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(most_deaths_text)
                                .addComponent(most_confirmed_text, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(total_countries_text, javax.swing.GroupLayout.Alignment.LEADING)))
                        .addGap(25, 25, 25)
                        .addComponent(key_value_separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(most_confirmed_placeholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(most_recovered_placeholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(most_death_placeholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(total_country_placeholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(0, 2, Short.MAX_VALUE))
        );
        home_database_index_areaLayout.setVerticalGroup(
            home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_database_index_areaLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(database_index_text)
                .addGap(1, 1, 1)
                .addComponent(database_index_underline, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(home_database_index_areaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(total_countries_text)
                        .addGap(18, 18, 18)
                        .addComponent(most_confirmed_text, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(most_deaths_text)
                        .addGap(18, 18, 18)
                        .addComponent(most_recovered_text)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(home_database_index_areaLayout.createSequentialGroup()
                        .addGroup(home_database_index_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(key_value_separator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(home_database_index_areaLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(total_country_placeholder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(most_death_placeholder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(most_confirmed_placeholder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(most_recovered_placeholder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        home_stats_area.setBackground(new java.awt.Color(238, 238, 237));
        home_stats_area.setPreferredSize(new java.awt.Dimension(400, 240));

        home_stat_combo_box.setBackground(new java.awt.Color(255, 255, 255));
        home_stat_combo_box.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        home_stat_combo_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Global" }));
        home_stat_combo_box.setToolTipText("Choose country to see its stats");
        home_stat_combo_box.setPreferredSize(new java.awt.Dimension(216, 34));

        home_stats_area_text.setBackground(new java.awt.Color(238, 238, 237));
        home_stats_area_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 30)); // NOI18N
        home_stats_area_text.setForeground(new java.awt.Color(0, 0, 0));
        home_stats_area_text.setText("Stats Overview  ");

        home_stat_clear_button.setFont(new java.awt.Font("Dialog", 1, 5)); // NOI18N
        home_stat_clear_button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/baseline_close_white_18dp.png"))); // NOI18N
        home_stat_clear_button.setPreferredSize(new java.awt.Dimension(34, 34));

        javax.swing.GroupLayout home_stats_areaLayout = new javax.swing.GroupLayout(home_stats_area);
        home_stats_area.setLayout(home_stats_areaLayout);
        home_stats_areaLayout.setHorizontalGroup(
            home_stats_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, home_stats_areaLayout.createSequentialGroup()
                .addGap(0, 85, Short.MAX_VALUE)
                .addComponent(home_stat_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(home_stat_clear_button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53))
            .addGroup(home_stats_areaLayout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(home_stats_area_text)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        home_stats_areaLayout.setVerticalGroup(
            home_stats_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, home_stats_areaLayout.createSequentialGroup()
                .addContainerGap(103, Short.MAX_VALUE)
                .addComponent(home_stats_area_text)
                .addGap(28, 28, 28)
                .addGroup(home_stats_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(home_stat_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(home_stat_clear_button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34))
        );

        javax.swing.GroupLayout home_free_panelLayout = new javax.swing.GroupLayout(home_free_panel);
        home_free_panel.setLayout(home_free_panelLayout);
        home_free_panelLayout.setHorizontalGroup(
            home_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(home_free_panelLayout.createSequentialGroup()
                .addGroup(home_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(home_information_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(home_free_panelLayout.createSequentialGroup()
                        .addComponent(home_database_index_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(home_stats_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        home_free_panelLayout.setVerticalGroup(
            home_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, home_free_panelLayout.createSequentialGroup()
                .addGroup(home_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(home_database_index_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(home_stats_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(home_information_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout dashboard_panelLayout = new javax.swing.GroupLayout(dashboard_panel);
        dashboard_panel.setLayout(dashboard_panelLayout);
        dashboard_panelLayout.setHorizontalGroup(
            dashboard_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboard_panelLayout.createSequentialGroup()
                .addComponent(home_side_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(dashboard_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dashboard_panelLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(home_confirmed_main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(home_recovered_main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60)
                        .addComponent(home_death_main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60))
                    .addComponent(home_free_panel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        dashboard_panelLayout.setVerticalGroup(
            dashboard_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboard_panelLayout.createSequentialGroup()
                .addGroup(dashboard_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(dashboard_panelLayout.createSequentialGroup()
                        .addGroup(dashboard_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(home_confirmed_main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(home_death_main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(home_recovered_main, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(home_free_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(home_side_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        getContentPane().add(dashboard_panel, "card2");

        database_panel.setPreferredSize(new java.awt.Dimension(940, 500));

        db_side_panel.setBackground(new java.awt.Color(28, 38, 61));
        db_side_panel.setPreferredSize(new java.awt.Dimension(100, 500));

        db_app_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        db_app_icon_area.setMinimumSize(new java.awt.Dimension(100, 70));

        db_app_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_app_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/covid-50x50.png"))); // NOI18N
        db_app_icon.setToolTipText("");
        db_app_icon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        db_app_icon.setPreferredSize(new java.awt.Dimension(100, 70));

        javax.swing.GroupLayout db_app_icon_areaLayout = new javax.swing.GroupLayout(db_app_icon_area);
        db_app_icon_area.setLayout(db_app_icon_areaLayout);
        db_app_icon_areaLayout.setHorizontalGroup(
            db_app_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_app_icon_areaLayout.createSequentialGroup()
                .addComponent(db_app_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        db_app_icon_areaLayout.setVerticalGroup(
            db_app_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_app_icon_areaLayout.createSequentialGroup()
                .addComponent(db_app_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        db_dashboard_icon_area.setBackground(new java.awt.Color(28, 38, 61));

        db_dashboard_icon.setForeground(new java.awt.Color(28, 38, 61));
        db_dashboard_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_dashboard_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons8-home-24.png"))); // NOI18N
        db_dashboard_icon.setToolTipText("");
        db_dashboard_icon.setPreferredSize(new java.awt.Dimension(100, 60));
        db_dashboard_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                db_dashboard_iconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout db_dashboard_icon_areaLayout = new javax.swing.GroupLayout(db_dashboard_icon_area);
        db_dashboard_icon_area.setLayout(db_dashboard_icon_areaLayout);
        db_dashboard_icon_areaLayout.setHorizontalGroup(
            db_dashboard_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_dashboard_icon_areaLayout.createSequentialGroup()
                .addComponent(db_dashboard_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        db_dashboard_icon_areaLayout.setVerticalGroup(
            db_dashboard_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_dashboard_icon_areaLayout.createSequentialGroup()
                .addComponent(db_dashboard_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        db_database_icon_area.setBackground(new java.awt.Color(39, 49, 70));

        db_database_icon.setForeground(new java.awt.Color(39, 49, 70));
        db_database_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_database_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/piechart-30x30.png"))); // NOI18N
        db_database_icon.setToolTipText("");
        db_database_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout db_database_icon_areaLayout = new javax.swing.GroupLayout(db_database_icon_area);
        db_database_icon_area.setLayout(db_database_icon_areaLayout);
        db_database_icon_areaLayout.setHorizontalGroup(
            db_database_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, db_database_icon_areaLayout.createSequentialGroup()
                .addComponent(db_database_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        db_database_icon_areaLayout.setVerticalGroup(
            db_database_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_database_icon_areaLayout.createSequentialGroup()
                .addComponent(db_database_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        db_settings_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        db_settings_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        db_settings_icon.setForeground(new java.awt.Color(28, 38, 61));
        db_settings_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_settings_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons8-settings-24.png"))); // NOI18N
        db_settings_icon.setToolTipText("");
        db_settings_icon.setPreferredSize(new java.awt.Dimension(100, 60));
        db_settings_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                db_settings_iconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout db_settings_icon_areaLayout = new javax.swing.GroupLayout(db_settings_icon_area);
        db_settings_icon_area.setLayout(db_settings_icon_areaLayout);
        db_settings_icon_areaLayout.setHorizontalGroup(
            db_settings_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, db_settings_icon_areaLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(db_settings_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        db_settings_icon_areaLayout.setVerticalGroup(
            db_settings_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, db_settings_icon_areaLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(db_settings_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        db_about_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        db_about_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        db_about_icon.setForeground(new java.awt.Color(28, 38, 61));
        db_about_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_about_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/about-icon31.png"))); // NOI18N
        db_about_icon.setToolTipText("null");
        db_about_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout db_about_icon_areaLayout = new javax.swing.GroupLayout(db_about_icon_area);
        db_about_icon_area.setLayout(db_about_icon_areaLayout);
        db_about_icon_areaLayout.setHorizontalGroup(
            db_about_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, db_about_icon_areaLayout.createSequentialGroup()
                .addComponent(db_about_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        db_about_icon_areaLayout.setVerticalGroup(
            db_about_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_about_icon_areaLayout.createSequentialGroup()
                .addComponent(db_about_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        db_refresh_icon_area.setBackground(new java.awt.Color(88, 104, 220));
        db_refresh_icon_area.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        db_refresh_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        db_refresh_icon.setBackground(new java.awt.Color(28, 38, 61));
        db_refresh_icon.setForeground(new java.awt.Color(88, 104, 220));
        db_refresh_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_refresh_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/baseline_refresh_white_18dp.png"))); // NOI18N
        db_refresh_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout db_refresh_icon_areaLayout = new javax.swing.GroupLayout(db_refresh_icon_area);
        db_refresh_icon_area.setLayout(db_refresh_icon_areaLayout);
        db_refresh_icon_areaLayout.setHorizontalGroup(
            db_refresh_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_refresh_icon_areaLayout.createSequentialGroup()
                .addComponent(db_refresh_icon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        db_refresh_icon_areaLayout.setVerticalGroup(
            db_refresh_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_refresh_icon_areaLayout.createSequentialGroup()
                .addComponent(db_refresh_icon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout db_side_panelLayout = new javax.swing.GroupLayout(db_side_panel);
        db_side_panel.setLayout(db_side_panelLayout);
        db_side_panelLayout.setHorizontalGroup(
            db_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_side_panelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(db_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(db_app_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db_dashboard_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db_database_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db_settings_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db_about_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db_refresh_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        db_side_panelLayout.setVerticalGroup(
            db_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_side_panelLayout.createSequentialGroup()
                .addComponent(db_app_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(db_dashboard_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(db_database_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(db_settings_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(db_about_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(db_refresh_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        db_free_panel.setBackground(new java.awt.Color(34, 41, 57));
        db_free_panel.setPreferredSize(new java.awt.Dimension(840, 70));

        db_operator_combobox.setFont(new java.awt.Font("Segoe UI Semibold", 1, 15)); // NOI18N
        db_operator_combobox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Lesser than", "Equals to", "Larger than" }));

        db_field_combobox.setFont(new java.awt.Font("Segoe UI Semibold", 1, 15)); // NOI18N
        db_field_combobox.setMaximumRowCount(3);
        db_field_combobox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "confirmed", "recovered", "death" }));

        db_filter_keyword.setFont(new java.awt.Font("Segoe UI Semibold", 1, 15)); // NOI18N
        db_filter_keyword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        db_filter_keyword.setText("0");
        db_filter_keyword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                db_filter_keywordActionPerformed(evt);
            }
        });

        db_filter_button.setFont(new java.awt.Font("Segoe UI Semibold", 1, 15)); // NOI18N
        db_filter_button.setText("Search");
        db_filter_button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        db_filter_button.setPreferredSize(new java.awt.Dimension(31, 38));
        db_filter_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                db_filter_buttonActionPerformed(evt);
            }
        });

        db_arrow_one.setFont(new java.awt.Font("Segoe UI Semibold", 1, 15)); // NOI18N
        db_arrow_one.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_arrow_one.setText("->");

        db_arrow_two.setFont(new java.awt.Font("Segoe UI Semibold", 1, 15)); // NOI18N
        db_arrow_two.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        db_arrow_two.setText("->");

        home_stat_clear_button1.setFont(new java.awt.Font("Dialog", 1, 5)); // NOI18N
        home_stat_clear_button1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/baseline_close_white_18dp.png"))); // NOI18N
        home_stat_clear_button1.setPreferredSize(new java.awt.Dimension(38, 38));
        home_stat_clear_button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                home_stat_clear_button1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout db_free_panelLayout = new javax.swing.GroupLayout(db_free_panel);
        db_free_panel.setLayout(db_free_panelLayout);
        db_free_panelLayout.setHorizontalGroup(
            db_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_free_panelLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addComponent(db_field_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(db_arrow_one, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(db_operator_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(db_arrow_two, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(db_filter_keyword, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(133, 133, 133)
                .addComponent(db_filter_button, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(home_stat_clear_button1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        db_free_panelLayout.setVerticalGroup(
            db_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, db_free_panelLayout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(db_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(db_free_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(db_operator_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(db_field_combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(db_filter_keyword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(db_arrow_one)
                        .addComponent(db_arrow_two)
                        .addComponent(db_filter_button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(home_stat_clear_button1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        db_information_panel.setBackground(new java.awt.Color(34, 41, 57));
        db_information_panel.setForeground(new java.awt.Color(255, 255, 255));
        db_information_panel.setPreferredSize(new java.awt.Dimension(840, 40));

        db_information_text_area.setBackground(new java.awt.Color(255, 255, 255));
        db_information_text_area.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        db_information_text_area.setForeground(new java.awt.Color(0, 0, 0));

        javax.swing.GroupLayout db_information_panelLayout = new javax.swing.GroupLayout(db_information_panel);
        db_information_panel.setLayout(db_information_panelLayout);
        db_information_panelLayout.setHorizontalGroup(
            db_information_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db_information_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(db_information_text_area, javax.swing.GroupLayout.PREFERRED_SIZE, 816, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        db_information_panelLayout.setVerticalGroup(
            db_information_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(db_information_text_area, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        db_table_scrollpane.setBackground(new java.awt.Color(28, 38, 61));
        db_table_scrollpane.setBorder(null);
        db_table_scrollpane.setToolTipText("Covid Country Based Chart");
        db_table_scrollpane.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        db_table_scrollpane.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        db_table_scrollpane.setHorizontalScrollBar(null);
        db_table_scrollpane.setPreferredSize(new java.awt.Dimension(840, 390));

        db_table_panel.setBackground(new java.awt.Color(34, 41, 57));
        db_table_panel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        db_table_panel.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        db_table_panel.setForeground(new java.awt.Color(255, 255, 255));
        db_table_panel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CountryName", "CountryCode", "Confirmed", "Recovered", "Deaths"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        db_table_panel.setFillsViewportHeight(true);
        db_table_panel.setGridColor(new java.awt.Color(255, 255, 255));
        db_table_panel.setRowHeight(27);
        db_table_panel.setShowVerticalLines(false);
        db_table_panel.getTableHeader().setResizingAllowed(false);
        db_table_scrollpane.setViewportView(db_table_panel);
        if (db_table_panel.getColumnModel().getColumnCount() > 0) {
            db_table_panel.getColumnModel().getColumn(0).setResizable(false);
            db_table_panel.getColumnModel().getColumn(1).setResizable(false);
            db_table_panel.getColumnModel().getColumn(2).setResizable(false);
            db_table_panel.getColumnModel().getColumn(3).setResizable(false);
            db_table_panel.getColumnModel().getColumn(4).setResizable(false);
        }

        javax.swing.GroupLayout database_panelLayout = new javax.swing.GroupLayout(database_panel);
        database_panel.setLayout(database_panelLayout);
        database_panelLayout.setHorizontalGroup(
            database_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(database_panelLayout.createSequentialGroup()
                .addComponent(db_side_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(database_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(db_table_scrollpane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 840, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(database_panelLayout.createSequentialGroup()
                        .addGroup(database_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(db_information_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(db_free_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        database_panelLayout.setVerticalGroup(
            database_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(database_panelLayout.createSequentialGroup()
                .addGroup(database_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(database_panelLayout.createSequentialGroup()
                        .addComponent(db_free_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(db_table_scrollpane, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(db_information_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(db_side_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        getContentPane().add(database_panel, "card3");

        settings_panel.setBackground(new java.awt.Color(27, 29, 36));
        settings_panel.setPreferredSize(new java.awt.Dimension(940, 500));

        stg_side_panel.setBackground(new java.awt.Color(28, 38, 61));
        stg_side_panel.setPreferredSize(new java.awt.Dimension(100, 500));

        stg_app_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        stg_app_icon_area.setMinimumSize(new java.awt.Dimension(100, 70));

        stg_app_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stg_app_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/covid-50x50.png"))); // NOI18N
        stg_app_icon.setToolTipText("");
        stg_app_icon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stg_app_icon.setPreferredSize(new java.awt.Dimension(100, 70));

        javax.swing.GroupLayout stg_app_icon_areaLayout = new javax.swing.GroupLayout(stg_app_icon_area);
        stg_app_icon_area.setLayout(stg_app_icon_areaLayout);
        stg_app_icon_areaLayout.setHorizontalGroup(
            stg_app_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_app_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_app_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        stg_app_icon_areaLayout.setVerticalGroup(
            stg_app_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_app_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_app_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        stg_dashboard_icon_area.setBackground(new java.awt.Color(28, 38, 61));

        stg_dashboard_icon.setForeground(new java.awt.Color(28, 38, 61));
        stg_dashboard_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stg_dashboard_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons8-home-24.png"))); // NOI18N
        stg_dashboard_icon.setToolTipText("");
        stg_dashboard_icon.setPreferredSize(new java.awt.Dimension(100, 60));
        stg_dashboard_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stg_dashboard_iconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout stg_dashboard_icon_areaLayout = new javax.swing.GroupLayout(stg_dashboard_icon_area);
        stg_dashboard_icon_area.setLayout(stg_dashboard_icon_areaLayout);
        stg_dashboard_icon_areaLayout.setHorizontalGroup(
            stg_dashboard_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_dashboard_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_dashboard_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        stg_dashboard_icon_areaLayout.setVerticalGroup(
            stg_dashboard_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_dashboard_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_dashboard_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        stg_database_icon_area.setBackground(new java.awt.Color(28, 38, 61));

        stg_database_icon.setForeground(new java.awt.Color(28, 38, 61));
        stg_database_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stg_database_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/piechart-30x30.png"))); // NOI18N
        stg_database_icon.setToolTipText("");
        stg_database_icon.setPreferredSize(new java.awt.Dimension(100, 60));
        stg_database_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stg_database_iconMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout stg_database_icon_areaLayout = new javax.swing.GroupLayout(stg_database_icon_area);
        stg_database_icon_area.setLayout(stg_database_icon_areaLayout);
        stg_database_icon_areaLayout.setHorizontalGroup(
            stg_database_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stg_database_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_database_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        stg_database_icon_areaLayout.setVerticalGroup(
            stg_database_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_database_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_database_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        stg_settings_icon_area.setBackground(new java.awt.Color(39, 49, 70));
        stg_settings_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        stg_settings_icon.setForeground(new java.awt.Color(39, 49, 70));
        stg_settings_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stg_settings_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons8-settings-24.png"))); // NOI18N
        stg_settings_icon.setToolTipText("");
        stg_settings_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout stg_settings_icon_areaLayout = new javax.swing.GroupLayout(stg_settings_icon_area);
        stg_settings_icon_area.setLayout(stg_settings_icon_areaLayout);
        stg_settings_icon_areaLayout.setHorizontalGroup(
            stg_settings_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stg_settings_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_settings_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        stg_settings_icon_areaLayout.setVerticalGroup(
            stg_settings_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_settings_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_settings_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        stg_about_icon_area.setBackground(new java.awt.Color(28, 38, 61));
        stg_about_icon_area.setPreferredSize(new java.awt.Dimension(100, 60));

        stg_about_icon.setForeground(new java.awt.Color(28, 38, 61));
        stg_about_icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        stg_about_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/about-icon31.png"))); // NOI18N
        stg_about_icon.setToolTipText("null");
        stg_about_icon.setPreferredSize(new java.awt.Dimension(100, 60));

        javax.swing.GroupLayout stg_about_icon_areaLayout = new javax.swing.GroupLayout(stg_about_icon_area);
        stg_about_icon_area.setLayout(stg_about_icon_areaLayout);
        stg_about_icon_areaLayout.setHorizontalGroup(
            stg_about_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stg_about_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_about_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        stg_about_icon_areaLayout.setVerticalGroup(
            stg_about_icon_areaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_about_icon_areaLayout.createSequentialGroup()
                .addComponent(stg_about_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout stg_side_panelLayout = new javax.swing.GroupLayout(stg_side_panel);
        stg_side_panel.setLayout(stg_side_panelLayout);
        stg_side_panelLayout.setHorizontalGroup(
            stg_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_side_panelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(stg_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stg_app_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stg_dashboard_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stg_database_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stg_settings_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stg_about_icon_area, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        stg_side_panelLayout.setVerticalGroup(
            stg_side_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_side_panelLayout.createSequentialGroup()
                .addComponent(stg_app_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stg_dashboard_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stg_database_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stg_settings_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stg_about_icon_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 166, Short.MAX_VALUE))
        );

        stg_options_panel.setBackground(new java.awt.Color(27, 29, 36));
        stg_options_panel.setPreferredSize(new java.awt.Dimension(352, 164));

        stg_stat_combo_box.setBackground(new java.awt.Color(255, 255, 255));
        stg_stat_combo_box.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        stg_stat_combo_box.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Global" }));
        stg_stat_combo_box.setToolTipText("Choose country to see its stats");
        stg_stat_combo_box.setPreferredSize(new java.awt.Dimension(216, 34));

        stg_combo_box_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        stg_combo_box_text.setForeground(new java.awt.Color(255, 255, 255));
        stg_combo_box_text.setText("> Default Dashboard Country : ");

        stg_stat_clear_button.setFont(new java.awt.Font("Dialog", 1, 5)); // NOI18N
        stg_stat_clear_button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/baseline_close_white_18dp.png"))); // NOI18N
        stg_stat_clear_button.setPreferredSize(new java.awt.Dimension(34, 34));

        stg_auto_refresh_text.setBackground(new java.awt.Color(255, 255, 255));
        stg_auto_refresh_text.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        stg_auto_refresh_text.setForeground(new java.awt.Color(255, 255, 255));
        stg_auto_refresh_text.setText("> Auto-Refresh API On Startup :");
        stg_auto_refresh_text.setPreferredSize(new java.awt.Dimension(259, 25));

        stg_auto_refresh_checkbox.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        stg_auto_refresh_checkbox.setText("yes");
        stg_auto_refresh_checkbox.setIconTextGap(10);
        stg_auto_refresh_checkbox.setPreferredSize(new java.awt.Dimension(75, 33));
        stg_auto_refresh_checkbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stg_auto_refresh_checkboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout stg_options_panelLayout = new javax.swing.GroupLayout(stg_options_panel);
        stg_options_panel.setLayout(stg_options_panelLayout);
        stg_options_panelLayout.setHorizontalGroup(
            stg_options_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stg_options_panelLayout.createSequentialGroup()
                .addContainerGap(63, Short.MAX_VALUE)
                .addGroup(stg_options_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stg_auto_refresh_checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(stg_options_panelLayout.createSequentialGroup()
                        .addComponent(stg_stat_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(stg_stat_clear_button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(stg_options_panelLayout.createSequentialGroup()
                .addGroup(stg_options_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stg_combo_box_text)
                    .addComponent(stg_auto_refresh_text, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        stg_options_panelLayout.setVerticalGroup(
            stg_options_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_options_panelLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(stg_combo_box_text)
                .addGap(23, 23, 23)
                .addGroup(stg_options_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(stg_stat_combo_box, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stg_stat_clear_button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addComponent(stg_auto_refresh_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addComponent(stg_auto_refresh_checkbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72))
        );

        stg_save_button.setText("Save");
        stg_save_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stg_save_buttonActionPerformed(evt);
            }
        });

        stg_cancel_button.setText("Cancel");

        stg_default_button.setText("Set to Default");

        stg_information_panel.setBackground(new java.awt.Color(27, 29, 36));
        stg_information_panel.setPreferredSize(new java.awt.Dimension(840, 40));

        stg_information_text_area.setBackground(new java.awt.Color(238, 238, 237));
        stg_information_text_area.setFont(new java.awt.Font("Segoe UI Semibold", 1, 18)); // NOI18N
        stg_information_text_area.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout stg_information_panelLayout = new javax.swing.GroupLayout(stg_information_panel);
        stg_information_panel.setLayout(stg_information_panelLayout);
        stg_information_panelLayout.setHorizontalGroup(
            stg_information_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stg_information_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(stg_information_text_area, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
                .addContainerGap())
        );
        stg_information_panelLayout.setVerticalGroup(
            stg_information_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(stg_information_text_area, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout settings_panelLayout = new javax.swing.GroupLayout(settings_panel);
        settings_panel.setLayout(settings_panelLayout);
        settings_panelLayout.setHorizontalGroup(
            settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settings_panelLayout.createSequentialGroup()
                .addComponent(stg_side_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settings_panelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(stg_information_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(settings_panelLayout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(stg_save_button)
                        .addGap(106, 106, 106)
                        .addComponent(stg_default_button)
                        .addGap(115, 115, 115)
                        .addComponent(stg_cancel_button)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(settings_panelLayout.createSequentialGroup()
                    .addGap(128, 128, 128)
                    .addComponent(stg_options_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(466, Short.MAX_VALUE)))
        );
        settings_panelLayout.setVerticalGroup(
            settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(stg_side_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(settings_panelLayout.createSequentialGroup()
                .addGroup(settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stg_save_button)
                    .addComponent(stg_cancel_button)
                    .addComponent(stg_default_button))
                .addGap(41, 41, 41)
                .addComponent(stg_information_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(settings_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(settings_panelLayout.createSequentialGroup()
                    .addGap(67, 67, 67)
                    .addComponent(stg_options_panel, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(133, Short.MAX_VALUE)))
        );

        getContentPane().add(settings_panel, "card4");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            this.addMouseEvent();
            Connection db = Database.getConnection();
            try {
                loadDashboard(db, true);
                loadTable(db);
                loadSettings();
            } finally {
                db.close();
            }
        } catch (java.sql.SQLSyntaxErrorException ex) {
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Opening for the first time? consider tapping the refresh icon",
                    ex.getMessage()
            );
        } catch (SQLException ex) {
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Could not connect into database",
                    ex.getMessage()
            );
        } catch (Exception ex) {
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    ex.getMessage(),
                    ex.getMessage()
            );
        }
    }//GEN-LAST:event_formWindowOpened

    private void db_settings_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_db_settings_iconMouseClicked
        database_panel.setVisible(false);
        settings_panel.setVisible(true);
    }//GEN-LAST:event_db_settings_iconMouseClicked

    private void db_dashboard_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_db_dashboard_iconMouseClicked
        database_panel.setVisible(false);
        dashboard_panel.setVisible(true);
    }//GEN-LAST:event_db_dashboard_iconMouseClicked

    private void home_database_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_home_database_iconMouseClicked
        dashboard_panel.setVisible(false);
        database_panel.setVisible(true);
    }//GEN-LAST:event_home_database_iconMouseClicked

    private void home_settings_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_home_settings_iconMouseClicked
        dashboard_panel.setVisible(false);
        settings_panel.setVisible(true);
    }//GEN-LAST:event_home_settings_iconMouseClicked

    private void stg_dashboard_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stg_dashboard_iconMouseClicked
        settings_panel.setVisible(false);
        dashboard_panel.setVisible(true);
    }//GEN-LAST:event_stg_dashboard_iconMouseClicked

    private void stg_database_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stg_database_iconMouseClicked
        settings_panel.setVisible(false);
        database_panel.setVisible(true);
    }//GEN-LAST:event_stg_database_iconMouseClicked

    private void stg_auto_refresh_checkboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stg_auto_refresh_checkboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_stg_auto_refresh_checkboxActionPerformed

    private void stg_save_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stg_save_buttonActionPerformed

    }//GEN-LAST:event_stg_save_buttonActionPerformed

    private void db_filter_keywordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_db_filter_keywordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_db_filter_keywordActionPerformed

    private void db_filter_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_db_filter_buttonActionPerformed
        try {
            Connection db = Database.getConnection();
            try {
                int filter_keyword = Integer.parseInt(db_filter_keyword.getText());
                ResultSet result = Database.filterModel(
                        db_field_combobox.getSelectedItem().toString(),
                        db_operator_combobox.getSelectedItem().toString(),
                        filter_keyword,
                        db);
                DefaultTableModel model = (DefaultTableModel) db_table_panel.getModel();
                // remove every row before adding new
                for (int i = model.getRowCount() - 1; i >= 0; i--) {
                    model.removeRow(i);
                }
                while (result.next()) {
                    model.addRow(new Object[]{
                        result.getString("countryname"),
                        result.getString("countrycode"),
                        result.getInt("confirmed"),
                        result.getInt("recovered"),
                        result.getInt("death")
                    });
                }
                db_table_panel.setModel(model);
            } finally {
                db.close();
            }
        } catch (SQLException ex) {
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Cound not connect into database",
                    ex.getMessage()
            );
        } catch (NumberFormatException ex) {
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Only number is allowed",
                    ex.getMessage()
            );
        }
    }//GEN-LAST:event_db_filter_buttonActionPerformed

    private void home_stat_clear_button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_home_stat_clear_button1ActionPerformed
        db_field_combobox.setSelectedIndex(0);
        db_operator_combobox.setSelectedIndex(0);
        db_filter_keyword.setText("0");
        try {
            Connection db = Database.getConnection();
            try {
                this.loadTable(db);
            } finally {
                db.close();
            }
        } catch (SQLException ex) {
            this.setInformation(
                    new java.awt.Color(254, 215, 215),
                    "Cound not connect into database",
                    ex.getMessage()
            );
        }
    }//GEN-LAST:event_home_stat_clear_button1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("GTK+".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel confirmed_text;
    private javax.swing.JPanel dashboard_panel;
    private javax.swing.JLabel database_index_text;
    private javax.swing.JSeparator database_index_underline;
    private javax.swing.JPanel database_panel;
    private javax.swing.JLabel db_about_icon;
    private javax.swing.JPanel db_about_icon_area;
    private javax.swing.JLabel db_app_icon;
    private javax.swing.JPanel db_app_icon_area;
    private javax.swing.JLabel db_arrow_one;
    private javax.swing.JLabel db_arrow_two;
    private javax.swing.JLabel db_dashboard_icon;
    private javax.swing.JPanel db_dashboard_icon_area;
    private javax.swing.JLabel db_database_icon;
    private javax.swing.JPanel db_database_icon_area;
    private javax.swing.JComboBox<String> db_field_combobox;
    private javax.swing.JButton db_filter_button;
    private javax.swing.JTextField db_filter_keyword;
    private javax.swing.JPanel db_free_panel;
    private javax.swing.JPanel db_information_panel;
    private javax.swing.JLabel db_information_text_area;
    private javax.swing.JComboBox<String> db_operator_combobox;
    private javax.swing.JLabel db_refresh_icon;
    private javax.swing.JPanel db_refresh_icon_area;
    private javax.swing.JLabel db_settings_icon;
    private javax.swing.JPanel db_settings_icon_area;
    private javax.swing.JPanel db_side_panel;
    private javax.swing.JTable db_table_panel;
    private javax.swing.JScrollPane db_table_scrollpane;
    private javax.swing.JLabel death_text;
    private javax.swing.JLabel home_about_icon;
    private javax.swing.JPanel home_about_icon_area;
    private javax.swing.JLabel home_app_icon;
    private javax.swing.JPanel home_app_icon_area;
    private javax.swing.JPanel home_confirmed_main;
    private javax.swing.JPanel home_confirmed_text_area;
    private javax.swing.JLabel home_dashboard_icon;
    private javax.swing.JPanel home_dashboard_icon_area;
    private javax.swing.JLabel home_database_icon;
    private javax.swing.JPanel home_database_icon_area;
    private javax.swing.JPanel home_database_index_area;
    private javax.swing.JPanel home_death_main;
    private javax.swing.JPanel home_death_text_area;
    private javax.swing.JPanel home_free_panel;
    private javax.swing.JPanel home_information_panel;
    private javax.swing.JLabel home_information_text_area;
    private javax.swing.JPanel home_recovered_main;
    private javax.swing.JPanel home_recovered_text_area;
    private javax.swing.JLabel home_refresh_icon;
    private javax.swing.JPanel home_refresh_icon_area;
    private javax.swing.JLabel home_settings_icon;
    private javax.swing.JPanel home_settings_icon_area;
    private javax.swing.JPanel home_side_panel;
    private javax.swing.JButton home_stat_clear_button;
    private javax.swing.JButton home_stat_clear_button1;
    private javax.swing.JComboBox<String> home_stat_combo_box;
    private javax.swing.JPanel home_stats_area;
    private javax.swing.JLabel home_stats_area_text;
    private javax.swing.JLabel home_text_confirmed;
    private javax.swing.JLabel home_text_death;
    private javax.swing.JLabel home_text_recovered;
    private javax.swing.JSeparator key_value_separator;
    private javax.swing.JLabel most_confirmed_placeholder;
    private javax.swing.JLabel most_confirmed_text;
    private javax.swing.JLabel most_death_placeholder;
    private javax.swing.JLabel most_deaths_text;
    private javax.swing.JLabel most_recovered_placeholder;
    private javax.swing.JLabel most_recovered_text;
    private javax.swing.JLabel recovered_text;
    private javax.swing.JPanel settings_panel;
    private javax.swing.JLabel stg_about_icon;
    private javax.swing.JPanel stg_about_icon_area;
    private javax.swing.JLabel stg_app_icon;
    private javax.swing.JPanel stg_app_icon_area;
    private javax.swing.JCheckBox stg_auto_refresh_checkbox;
    private javax.swing.JLabel stg_auto_refresh_text;
    private javax.swing.JButton stg_cancel_button;
    private javax.swing.JLabel stg_combo_box_text;
    private javax.swing.JLabel stg_dashboard_icon;
    private javax.swing.JPanel stg_dashboard_icon_area;
    private javax.swing.JLabel stg_database_icon;
    private javax.swing.JPanel stg_database_icon_area;
    private javax.swing.JButton stg_default_button;
    private javax.swing.JPanel stg_information_panel;
    private javax.swing.JLabel stg_information_text_area;
    private javax.swing.JPanel stg_options_panel;
    private javax.swing.JButton stg_save_button;
    private javax.swing.JLabel stg_settings_icon;
    private javax.swing.JPanel stg_settings_icon_area;
    private javax.swing.JPanel stg_side_panel;
    private javax.swing.JButton stg_stat_clear_button;
    private javax.swing.JComboBox<String> stg_stat_combo_box;
    private javax.swing.JLabel total_countries_text;
    private javax.swing.JLabel total_country_placeholder;
    // End of variables declaration//GEN-END:variables
}
