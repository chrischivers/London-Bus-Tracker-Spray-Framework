package com.PredictionAlgorithm.UI;

import com.PredictionAlgorithm.Commons.Commons;
import com.PredictionAlgorithm.ControlInterface.QueryController;
import com.PredictionAlgorithm.ControlInterface.StartStopControlInterface;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;


public class MonitoringUI {
    private JLabel dataSourceLinesReadValue;
    private JPanel mainPanel;

    private int WINDOW_H_SIZE = 1200;
    private int WINDOW_V_SIZE = 500;

    private int UI_REFRESH_INTERVAL;

    private JButton startStopStreamProcessingButton;
    private JPanel DataSourcePanel;
    private JPanel DataProcessingPanel;
    private JLabel sizeHoldingBufferValue;
    private JLabel dBTransactionsRequestedValue;
    private JLabel dBTransactionsExecutedValue;
    private JLabel dBTransactionsOutstandingValue;
    private JPanel queryingPanel;
    private JTextField routeInput;
    private JTextField directionInput;
    private JTextField fromStopIDInput;
    private JTextField toStopIDInput;
    private JTextField dayCodeInput;
    private JButton runQueryButton;
    private JLabel queryResultValue;
    private JLabel dBPullTransactionsRequestedValue;
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


    public MonitoringUI(int refreshIntervalMS) {
        this.UI_REFRESH_INTERVAL = refreshIntervalMS;
    }

    public void createAndDisplayGUI() {

        JFrame frame = new JFrame("MonitoringUI");
        frame.setPreferredSize(new Dimension(WINDOW_H_SIZE, WINDOW_V_SIZE));
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(500, 500);
        frame.pack();
        frame.setVisible(true);

    }

    public void setCleanUpPointToPoint(StartStopControlInterface sSCI) {
        cleanUpPointToPointButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            CounterUpdater cu = new CounterUpdater(sSCI, numberPointToPointRecordsCheckedForCleanUpValue, numberPointToPointRecordsDeletedValue);
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
            CounterUpdater cu = new CounterUpdater(sSCI, routeUpdatePercentageCompleteValue, numberRoutesInsertedDBValue, numberRoutesUpdatedDBValue);

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
            CounterUpdater cu = new CounterUpdater(sSCI, numberAddPolyLinesLinesReadValue, numberPolyLinesAddedFromWebValue,numberPolyLinesAddedFromCacheValue);
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
            CounterUpdater cu = new CounterUpdater(sSCI, stopUpdatePercentageCompleteValue, numberStopsInsertedDBValue,numberStopsUpdatedDBValue);
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
            CounterUpdater cu = new CounterUpdater(sSCI, dataSourceLinesReadValue);

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
            CounterUpdater cu = new CounterUpdater(sSCI,sizeHoldingBufferValue, nonMatchesCountValue, dBTransactionsRequestedValue, dBTransactionsExecutedValue, dBTransactionsOutstandingValue, dBPullTransactionsRequestedValue);

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

    public void setQueryProcessing(QueryController queryController) {
        runQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = queryController.makePrediction(routeInput.getText(), Integer.parseInt(directionInput.getText()), fromStopIDInput.getText(), toStopIDInput.getText(), dayCodeInput.getText(), Commons.getTimeOffset(System.currentTimeMillis()));
                queryResultValue.setText(result);
            }
        });
    }


    public void setLiveStreaming(StartStopControlInterface sSCI) {
        startStopLiveStreamingButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            CounterUpdater cu = new CounterUpdater(sSCI, numberLiveActorsValue);
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




    public class CounterUpdater implements Runnable {
        private StartStopControlInterface cI;
        private List<JLabel> valueLabelList = new LinkedList<JLabel>();
        private volatile boolean running = true;


        public CounterUpdater(StartStopControlInterface cI, JLabel... valueLabels) {
            this.cI = cI;
            for (int i = 0; i < valueLabels.length; i++) {
                valueLabelList.add(valueLabels[i]);
            }
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
