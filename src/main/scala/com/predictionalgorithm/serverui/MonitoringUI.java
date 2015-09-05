package com.predictionalgorithm.serverui;

import com.predictionalgorithm.controlinterface.StartStopControlInterface;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class MonitoringUI {
    private JLabel dataSourceLinesReadValue;
    private JPanel mainPanel;

    private final int UI_REFRESH_INTERVAL;

    private JButton startStopStreamProcessingButton;
    private JPanel DataSourcePanel;
    private JPanel DataProcessingPanel;
    private JLabel sizeHoldingBufferValue;
    private JLabel dBTransactionsRequestedValue;
    private JLabel dBTransactionsExecutedValue;
    private JLabel dBTransactionsOutstandingValue;
    private JTextField routeInput;
    private JTextField directionInput;
    private JTextField fromStopIDInput;
    private JTextField toStopIDInput;
    private JTextField dayCodeInput;
    private JButton runQueryButton;
    private JLabel queryResultValue;
    private JLabel dBPullTransactionsRequested;
    private JLabel numberLiveActorsValue;
    private JButton startStopLiveStreamingButton;
    private JButton startStopHistoricalDataCollectionButton;
    private JButton updateRouteDefinitionsFromButton;
    private JPanel liveStreamingPanel;
    private JPanel updateRouteDefinitionsPanel;
    private JLabel numberRoutesInsertedDBValue;
    private JLabel numberRoutesUpdatedDBValue;
    private JLabel routeUpdatePercentageCompleteValue;
    private JPanel updateStopDefinitionsPanel;
    private JButton updateStopDefinitionsFromButton;
    private JLabel stopUpdatePercentageCompleteValue;
    private JLabel numberStopsInsertedDBValue;
    private JLabel numberStopsUpdatedDBValue;
    private JLabel numberPolyLinesAddedFromWebValue;
    private JButton addPolyLinesButton;
    private JLabel numberAddPolyLinesLinesReadValue;
    private JLabel nonMatchesCountValue;
    private JLabel numberPolyLinesAddedFromCacheValue;
    private JButton cleanUpPointToPointButton;
    private JLabel numberPointToPointRecordsCheckedForCleanUpValue;
    private JLabel numberPointToPointRecordsDeletedValue;
    private JLabel currentRainfallValue;
    private JLabel usedMemoryValue;
    private JLabel freeMemoryValue;
    private JLabel totalMemoryValue;
    private JLabel maxMemoryValue;
    private JLabel numberLiveChildrenValue;
    private JButton emailAlertsEnabledButton;
    private JLabel dBPullTransactionsExecuted;


    public MonitoringUI(int refreshIntervalMS) {
        this.UI_REFRESH_INTERVAL = refreshIntervalMS;
    }

    public void createAndDisplayGUI() {

        JFrame frame = new JFrame("MonitoringUI");
        int WINDOW_V_SIZE = 500;
        int WINDOW_H_SIZE = 1200;
        frame.setPreferredSize(new Dimension(WINDOW_H_SIZE, WINDOW_V_SIZE));
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocation(500, 500);
        frame.pack();
        frame.setVisible(true);

    }

    public void setUpEmailAlerts(StartStopControlInterface sSCI) {
        emailAlertsEnabledButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buttonStarted) {
                    sSCI.start();
                    emailAlertsEnabledButton.setText("Disable Email Alerts");
                    buttonStarted = true;
                } else {
                    sSCI.stop();
                    emailAlertsEnabledButton.setText("Enable Email Alerts");
                    buttonStarted = false;
                }
            }
        });
    }

    public void setCleanUpPointToPoint(StartStopControlInterface sSCI) {
        cleanUpPointToPointButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            final CounterUpdater cu = new CounterUpdater(sSCI, numberPointToPointRecordsCheckedForCleanUpValue, numberPointToPointRecordsDeletedValue);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buttonStarted) {
                    sSCI.start();
                    new Thread(cu).start();
                    cleanUpPointToPointButton.setText("Stop PointToPoint Clean up");
                    buttonStarted = true;
                } else {
                    sSCI.stop();
                    cu.terminate();
                    cleanUpPointToPointButton.setText("Start PointToPoint Clean up");
                    buttonStarted = false;
                }

            }
        });
    }

    public void setUpdateRouteDefinitions(StartStopControlInterface sSCI) {
        updateRouteDefinitionsFromButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            final CounterUpdater cu = new CounterUpdater(sSCI, routeUpdatePercentageCompleteValue, numberRoutesInsertedDBValue, numberRoutesUpdatedDBValue);

            @Override
            public void actionPerformed(ActionEvent e) {
                sSCI.start();
                new Thread(cu).start();
                updateRouteDefinitionsFromButton.setEnabled(false);
                buttonStarted = true;
            }
        });
    }

    public void setAddPolyLines(StartStopControlInterface sSCI) {
        addPolyLinesButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            final CounterUpdater cu = new CounterUpdater(sSCI, numberAddPolyLinesLinesReadValue, numberPolyLinesAddedFromWebValue, numberPolyLinesAddedFromCacheValue);

            @Override
            public void actionPerformed(ActionEvent e) {
                sSCI.start();
                new Thread(cu).start();
                addPolyLinesButton.setEnabled(false);
                buttonStarted = true;
            }
        });
    }

    public void setUpdateStopDefinitions(StartStopControlInterface sSCI) {
        updateStopDefinitionsFromButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            final CounterUpdater cu = new CounterUpdater(sSCI, stopUpdatePercentageCompleteValue, numberStopsInsertedDBValue, numberStopsUpdatedDBValue);

            @Override
            public void actionPerformed(ActionEvent e) {
                sSCI.start();
                new Thread(cu).start();
                updateStopDefinitionsFromButton.setEnabled(false);
                buttonStarted = true;
            }
        });
    }

    public void setStreamProcessing(StartStopControlInterface sSCI) {
        startStopStreamProcessingButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            final CounterUpdater cu = new CounterUpdater(sSCI, dataSourceLinesReadValue, currentRainfallValue, usedMemoryValue, freeMemoryValue, totalMemoryValue, maxMemoryValue);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buttonStarted) {
                    sSCI.start();
                    new Thread(cu).start();
                    startStopStreamProcessingButton.setText("Stop Stream Processing");
                    buttonStarted = true;
                } else {
                    sSCI.stop();
                    cu.terminate();
                    startStopStreamProcessingButton.setText("Start Stream Processing");
                    buttonStarted = false;
                }
            }
        });
    }

    public void setHistoricalDataCollection(StartStopControlInterface sSCI) {
        startStopHistoricalDataCollectionButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            final CounterUpdater cu = new CounterUpdater(sSCI, sizeHoldingBufferValue, nonMatchesCountValue, dBTransactionsRequestedValue, dBTransactionsExecutedValue, dBTransactionsOutstandingValue, dBPullTransactionsRequested, dBPullTransactionsExecuted);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buttonStarted) {
                    sSCI.start();
                    new Thread(cu).start();
                    startStopHistoricalDataCollectionButton.setText("Disable Historical Data Collection");
                    buttonStarted = true;
                } else {
                    sSCI.stop();
                    cu.terminate();
                    startStopHistoricalDataCollectionButton.setText("Enable Historical Data Collection");
                    buttonStarted = false;
                }
            }
        });
    }


    public void setLiveStreaming(StartStopControlInterface sSCI) {
        startStopLiveStreamingButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            final CounterUpdater cu = new CounterUpdater(sSCI, numberLiveActorsValue, numberLiveChildrenValue);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buttonStarted) {
                    sSCI.start();
                    new Thread(cu).start();
                    startStopLiveStreamingButton.setText("Disable Live Streaming");
                    buttonStarted = true;
                } else {
                    sSCI.stop();
                    cu.terminate();
                    startStopLiveStreamingButton.setText("Enable Live Streaming");
                    buttonStarted = false;
                }


            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(5, 7, new Insets(0, 0, 0, 0), -1, -1));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(4, 4, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        DataSourcePanel = new JPanel();
        DataSourcePanel.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.add(DataSourcePanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        DataSourcePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Data Source Reading"));
        dataSourceLinesReadValue = new JLabel();
        dataSourceLinesReadValue.setText("0");
        DataSourcePanel.add(dataSourceLinesReadValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Number of lines read:");
        DataSourcePanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startStopStreamProcessingButton = new JButton();
        startStopStreamProcessingButton.setText("Start Stream Processing");
        DataSourcePanel.add(startStopStreamProcessingButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(200, -1), 0, false));
        currentRainfallValue = new JLabel();
        currentRainfallValue.setText("0");
        DataSourcePanel.add(currentRainfallValue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Current Rainfall (mm)");
        DataSourcePanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        liveStreamingPanel = new JPanel();
        liveStreamingPanel.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(liveStreamingPanel, new GridConstraints(1, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        liveStreamingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Live Streaming"));
        final Spacer spacer2 = new Spacer();
        liveStreamingPanel.add(spacer2, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Number of Live Actors");
        liveStreamingPanel.add(label3, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberLiveActorsValue = new JLabel();
        numberLiveActorsValue.setHorizontalAlignment(2);
        numberLiveActorsValue.setText("");
        liveStreamingPanel.add(numberLiveActorsValue, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startStopLiveStreamingButton = new JButton();
        startStopLiveStreamingButton.setText("Enable Live Streaming");
        liveStreamingPanel.add(startStopLiveStreamingButton, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Number of Live Children");
        liveStreamingPanel.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberLiveChildrenValue = new JLabel();
        numberLiveChildrenValue.setHorizontalAlignment(2);
        numberLiveChildrenValue.setText("");
        liveStreamingPanel.add(numberLiveChildrenValue, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DataProcessingPanel = new JPanel();
        DataProcessingPanel.setLayout(new GridLayoutManager(11, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.add(DataProcessingPanel, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        DataProcessingPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Data Processing"));
        sizeHoldingBufferValue = new JLabel();
        sizeHoldingBufferValue.setText("0");
        DataProcessingPanel.add(sizeHoldingBufferValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Size of Holding Buffer");
        DataProcessingPanel.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dBTransactionsRequestedValue = new JLabel();
        dBTransactionsRequestedValue.setText("0");
        DataProcessingPanel.add(dBTransactionsRequestedValue, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Database Insert Transactions Requested");
        DataProcessingPanel.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dBTransactionsExecutedValue = new JLabel();
        dBTransactionsExecutedValue.setText("0");
        DataProcessingPanel.add(dBTransactionsExecutedValue, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Database Insert Transactions Executed");
        DataProcessingPanel.add(label7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dBTransactionsOutstandingValue = new JLabel();
        dBTransactionsOutstandingValue.setText("0");
        DataProcessingPanel.add(dBTransactionsOutstandingValue, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Database Insert Transactions Outstanding");
        DataProcessingPanel.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dBPullTransactionsRequested = new JLabel();
        dBPullTransactionsRequested.setText("0");
        DataProcessingPanel.add(dBPullTransactionsRequested, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Database Pull Transactions Requested");
        DataProcessingPanel.add(label9, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startStopHistoricalDataCollectionButton = new JButton();
        startStopHistoricalDataCollectionButton.setText("Enable Historical Data Collection");
        DataProcessingPanel.add(startStopHistoricalDataCollectionButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nonMatchesCountValue = new JLabel();
        nonMatchesCountValue.setText("0");
        DataProcessingPanel.add(nonMatchesCountValue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Number route definition non-matches");
        DataProcessingPanel.add(label10, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cleanUpPointToPointButton = new JButton();
        cleanUpPointToPointButton.setText("Start PointToPoint Clean up");
        DataProcessingPanel.add(cleanUpPointToPointButton, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberPointToPointRecordsCheckedForCleanUpValue = new JLabel();
        numberPointToPointRecordsCheckedForCleanUpValue.setText("0");
        DataProcessingPanel.add(numberPointToPointRecordsCheckedForCleanUpValue, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Number PointToPoint Records checked");
        DataProcessingPanel.add(label11, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberPointToPointRecordsDeletedValue = new JLabel();
        numberPointToPointRecordsDeletedValue.setText("0");
        DataProcessingPanel.add(numberPointToPointRecordsDeletedValue, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Number PointToPoint Records deleted");
        DataProcessingPanel.add(label12, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dBPullTransactionsExecuted = new JLabel();
        dBPullTransactionsExecuted.setText("0");
        DataProcessingPanel.add(dBPullTransactionsExecuted, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Database Pull Transactions Executed");
        DataProcessingPanel.add(label13, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateRouteDefinitionsPanel = new JPanel();
        updateRouteDefinitionsPanel.setLayout(new GridLayoutManager(9, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(updateRouteDefinitionsPanel, new GridConstraints(1, 3, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        updateRouteDefinitionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Update Route Definitions"));
        final Spacer spacer3 = new Spacer();
        updateRouteDefinitionsPanel.add(spacer3, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Number Inserted into DB");
        updateRouteDefinitionsPanel.add(label14, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberRoutesInsertedDBValue = new JLabel();
        numberRoutesInsertedDBValue.setHorizontalAlignment(2);
        numberRoutesInsertedDBValue.setText("");
        updateRouteDefinitionsPanel.add(numberRoutesInsertedDBValue, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Number updated into DB");
        updateRouteDefinitionsPanel.add(label15, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberRoutesUpdatedDBValue = new JLabel();
        numberRoutesUpdatedDBValue.setHorizontalAlignment(2);
        numberRoutesUpdatedDBValue.setText("");
        updateRouteDefinitionsPanel.add(numberRoutesUpdatedDBValue, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("% complete");
        updateRouteDefinitionsPanel.add(label16, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        routeUpdatePercentageCompleteValue = new JLabel();
        routeUpdatePercentageCompleteValue.setHorizontalAlignment(2);
        routeUpdatePercentageCompleteValue.setText("");
        updateRouteDefinitionsPanel.add(routeUpdatePercentageCompleteValue, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateRouteDefinitionsFromButton = new JButton();
        updateRouteDefinitionsFromButton.setText("Update Route Definitions From Web");
        updateRouteDefinitionsPanel.add(updateRouteDefinitionsFromButton, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Number polylines added from Web");
        updateRouteDefinitionsPanel.add(label17, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberPolyLinesAddedFromWebValue = new JLabel();
        numberPolyLinesAddedFromWebValue.setHorizontalAlignment(2);
        numberPolyLinesAddedFromWebValue.setText("");
        updateRouteDefinitionsPanel.add(numberPolyLinesAddedFromWebValue, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addPolyLinesButton = new JButton();
        addPolyLinesButton.setText("Add polylines where none recorded");
        updateRouteDefinitionsPanel.add(addPolyLinesButton, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Number lines read");
        updateRouteDefinitionsPanel.add(label18, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberAddPolyLinesLinesReadValue = new JLabel();
        numberAddPolyLinesLinesReadValue.setHorizontalAlignment(2);
        numberAddPolyLinesLinesReadValue.setText("");
        updateRouteDefinitionsPanel.add(numberAddPolyLinesLinesReadValue, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("Number polylines added from Cache");
        updateRouteDefinitionsPanel.add(label19, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberPolyLinesAddedFromCacheValue = new JLabel();
        numberPolyLinesAddedFromCacheValue.setHorizontalAlignment(2);
        numberPolyLinesAddedFromCacheValue.setText("");
        updateRouteDefinitionsPanel.add(numberPolyLinesAddedFromCacheValue, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateStopDefinitionsPanel = new JPanel();
        updateStopDefinitionsPanel.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(updateStopDefinitionsPanel, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        updateStopDefinitionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Update Stop Definitions"));
        final Spacer spacer4 = new Spacer();
        updateStopDefinitionsPanel.add(spacer4, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("Number Inserted into DB");
        updateStopDefinitionsPanel.add(label20, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberStopsInsertedDBValue = new JLabel();
        numberStopsInsertedDBValue.setHorizontalAlignment(2);
        numberStopsInsertedDBValue.setText("");
        updateStopDefinitionsPanel.add(numberStopsInsertedDBValue, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("Number updated into DB");
        updateStopDefinitionsPanel.add(label21, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberStopsUpdatedDBValue = new JLabel();
        numberStopsUpdatedDBValue.setHorizontalAlignment(2);
        numberStopsUpdatedDBValue.setText("");
        updateStopDefinitionsPanel.add(numberStopsUpdatedDBValue, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("% complete");
        updateStopDefinitionsPanel.add(label22, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stopUpdatePercentageCompleteValue = new JLabel();
        stopUpdatePercentageCompleteValue.setHorizontalAlignment(2);
        stopUpdatePercentageCompleteValue.setText("");
        updateStopDefinitionsPanel.add(stopUpdatePercentageCompleteValue, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        updateStopDefinitionsFromButton = new JButton();
        updateStopDefinitionsFromButton.setText("Update Stop Definitions From Web");
        updateStopDefinitionsPanel.add(updateStopDefinitionsFromButton, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 2, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "System Stats"));
        usedMemoryValue = new JLabel();
        usedMemoryValue.setText("0");
        panel1.add(usedMemoryValue, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label23 = new JLabel();
        label23.setText("Used Memory");
        panel1.add(label23, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        freeMemoryValue = new JLabel();
        freeMemoryValue.setText("0");
        panel1.add(freeMemoryValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label24 = new JLabel();
        label24.setText("Free Memory");
        panel1.add(label24, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        totalMemoryValue = new JLabel();
        totalMemoryValue.setText("0");
        panel1.add(totalMemoryValue, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label25 = new JLabel();
        label25.setText("Total Memory");
        panel1.add(label25, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxMemoryValue = new JLabel();
        maxMemoryValue.setText("0");
        panel1.add(maxMemoryValue, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label26 = new JLabel();
        label26.setText("Max Memory");
        panel1.add(label26, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label27 = new JLabel();
        label27.setFont(new Font(label27.getFont().getName(), Font.BOLD, 18));
        label27.setHorizontalAlignment(0);
        label27.setHorizontalTextPosition(0);
        label27.setText("TfL Bus Prediction Framework - Server UI");
        mainPanel.add(label27, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emailAlertsEnabledButton = new JButton();
        emailAlertsEnabledButton.setText("Enable Email Alerts");
        mainPanel.add(emailAlertsEnabledButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }


    public class CounterUpdater implements Runnable {
        private final StartStopControlInterface cI;
        private final List<JLabel> valueLabelList = new LinkedList<>();
        private volatile boolean running = true;


        public CounterUpdater(StartStopControlInterface cI, JLabel... valueLabels) {
            this.cI = cI;
            Collections.addAll(valueLabelList, valueLabels);
        }

        public void terminate() {
            running = false;
        }


        @Override
        public void run() {
            running = true;
            System.out.println("UI refresh running");
            while (running) {
                try {
                    Thread.sleep(UI_REFRESH_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String[] variableArray = cI.getVariableArray();
                assert (variableArray.length == valueLabelList.size()); //Check the right number of variables are being passed from the control interface

                // Update each of the labels with the values from the valueListArray
                for (int i = 0; i < valueLabelList.size(); i++) {
                    valueLabelList.get(i).setText(variableArray[i]);
                }

            }


        }
    }

}
