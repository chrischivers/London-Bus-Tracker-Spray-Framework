package com.PredictionAlgorithm.UI;

import com.PredictionAlgorithm.Commons.Commons;
import com.PredictionAlgorithm.ControlInterface.QueryController;
import com.PredictionAlgorithm.ControlInterface.StartStopControlInterface;
import com.PredictionAlgorithm.ControlInterface.StreamController;
import com.PredictionAlgorithm.Streaming.LiveStreamingCoordinator;
import com.PredictionAlgorithm.Streaming.StreamResult;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chrischivers on 14/07/15.
 */
public class MonitoringUI {
    private JLabel dataSourceLinesReadValue;
    private JPanel mainPanel;

    private int WINDOW_H_SIZE = 900;
    private int WINDOW_V_SIZE = 500;

    private int UI_REFRESH_INTERVAL;

    private JButton dataSourceReadStartStopButton;
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
    private JLabel liveStreamingMapSizeValue;
    private JButton enableLiveStreamingCollectionButton;


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

    public void setDataSourceProcess(StartStopControlInterface dsCI) {
        dataSourceReadStartStopButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            CounterUpdater cu = new CounterUpdater(dsCI, dataSourceLinesReadValue, sizeHoldingBufferValue, dBTransactionsRequestedValue, dBTransactionsExecutedValue, dBTransactionsOutstandingValue, dBPullTransactionsRequestedValue);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buttonStarted) {
                    dsCI.start();
                    new Thread(cu).start();
                    dataSourceReadStartStopButton.setText("Stop");
                    buttonStarted = true;
                } else {
                    dsCI.stop();
                    cu.terminate();
                    dataSourceReadStartStopButton.setText("Start");
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


    public void setStreamProcessing(StreamController streamController) {
        enableLiveStreamingCollectionButton.addActionListener(new ActionListener() {
            volatile boolean buttonStarted = false;
            StreamUpdater su = new StreamUpdater(streamController);
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buttonStarted) {
                    streamController.enableLiveStreamCollection(true);
                    new Thread(su).start();
                    enableLiveStreamingCollectionButton.setText("Stop Live Stream Collection");
                    buttonStarted = true;
                } else {
                    streamController.enableLiveStreamCollection(false);
                    su.terminate();
                    enableLiveStreamingCollectionButton.setText("Start Live Stream Collection");
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

    public class StreamUpdater implements Runnable {
        private volatile boolean running = true;
        private StreamController sc;
        public StreamUpdater(StreamController sc) {
            this.sc = sc;
        }
        public void terminate() {
            running = false;
        }


        @Override
        public void run() {
            running = true;
            System.out.println("Stream refresh running");
            while (running) {
                try {
                    Thread.sleep(UI_REFRESH_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                liveStreamingMapSizeValue.setText(Integer.toString(LiveStreamingCoordinator.getPositionMapSize()));
            }


        }
    }
}
