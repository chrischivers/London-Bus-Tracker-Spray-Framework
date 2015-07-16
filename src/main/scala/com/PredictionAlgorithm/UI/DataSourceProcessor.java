package com.PredictionAlgorithm.UI;

import com.PredictionAlgorithm.Processes.TFL.TFLIterateOverArrivalStream;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by chrischivers on 14/07/15.
 */
public class DataSourceProcessor {
    private JLabel linesReadValue;
    private JPanel panel1;
    private JButton startButton;

    public void createAndDisplayGUI() {

        JFrame frame = new JFrame("DataSourceProcessor");
        frame.setContentPane(new DataSourceProcessor().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(650, 400);
        frame.pack();
        frame.setVisible(true);
        new Thread(new counterUpdater()).start();

    }

    public DataSourceProcessor () {

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new counterUpdater()).start();

            }
        });
    }

    public class counterUpdater implements Runnable {
        private int REFRESH_INTERVAL = 1000;


        @Override
        public void run() {
            System.out.println("started");
            while (true) {
                try {
                    Thread.sleep(REFRESH_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                linesReadValue.setText(Integer.toString(TFLIterateOverArrivalStream.getNumberProcessed()));
            }

        }
    }
}
