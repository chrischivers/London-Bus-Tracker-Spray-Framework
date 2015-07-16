package com.PredictionAlgorithm.UI;

import com.PredictionAlgorithm.ControlInterface.ControlInterface;

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

    private int WINDOW_H_SIZE = 500;
    private int WINDOW_V_SIZE = 500;

    private int UI_REFRESH_INTERVAL;

    private JButton dataSourceReadStartStopButton;
    private JPanel DataSourcePanel;


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

    public void setDataSourceProcess(ControlInterface dsCI) {


        dataSourceReadStartStopButton.addActionListener(new ActionListener() {

            volatile boolean buttonStarted = false;
            CounterUpdater cu = new CounterUpdater(dsCI, dataSourceLinesReadValue);

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

    public class CounterUpdater implements Runnable {
        private ControlInterface cI;
        private List<JLabel> valueLabelList = new LinkedList<JLabel>();
        private volatile boolean running = true;


        public CounterUpdater(ControlInterface cI, JLabel... valueLabels) {
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
            System.out.println("started");
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
