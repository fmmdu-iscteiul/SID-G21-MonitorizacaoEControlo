package SID.Interfaces;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import SID.Conexoes.cloudToMongo;


public class ProcessBuilderTester {

	/*ola*/
	public JFrame frame;
	ProcessBuilder pb = new ProcessBuilder("java", "-jar", "C:\\Users\\Yotsuba\\Desktop\\Teste.jar"); //Torre
	Process processo;	
	ThreadTeste t;
	cloudToMongo t1;
	private final JPanel[] panels = new JPanel[6];
	private final JButton[] buttons = new JButton[6];
	private final JLabel [] labels = new JLabel[6];


	public ProcessBuilderTester() {
		frame = new JFrame("Inicializar migração dos sensores");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addFrameContent();
		frame.pack();
		init();
	}

	public void addFrameContent() {
		frame.setLayout(new GridLayout(1,7));	
		JButton activateAll = new JButton("Turn all sensors on");
		frame.add(activateAll);

		for(int i = 1; i <= buttons.length; i++) {
			if(i == 1) 
				buttons[i-1] = new JButton("H" + 1);

			if(i == 2)
				buttons[i-1] = new JButton("H" + 2);

			if(i == 3)
				buttons[i-1] = new JButton("T" + 1);

			if(i == 4)
				buttons[i-1] = new JButton("T" + 2);

			if(i == 5)
				buttons[i-1] = new JButton("L" + 1);

			if(i == 6)
				buttons[i-1] = new JButton("L" + 2);

		}

		for(int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel("Down");
		}

		buttons[0].addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Initialize(0);
			}
		});

		buttons[1].addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Initialize(1);
			}
		});

		buttons[2].addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Initialize(2);
			}
		});

		buttons[3].addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Initialize(3);
			}
		});

		buttons[4].addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Initialize(4);
			}
		});

		buttons[5].addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Initialize(5);
			}
		});
		
		activateAll.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < 6; i++) {
					buttons[i].doClick();
				}
			}
		});
		


		for(int i = 0; i < panels.length; i++) {
			panels[i] = new JPanel(new GridLayout(2,1));
			frame.add(panels[i]);
			panels[i].add(buttons[i]);
			panels[i].add(labels[i]);
		}
	}


	private void Initialize(int num) {
		try {
			processo = pb.start();
			t = new ThreadTeste(labels[num], processo, buttons[num]);
			t.start();
			switch (num) {
			case 0:
				t1 = new cloudToMongo("sensorh1", "Humidade1");
				break;
			case 1:
				t1 = new cloudToMongo("sensorh2", "Humidade2");
				break;
			case 2:
				t1 = new cloudToMongo("sensort1", "Temperatura1");
				break;
			case 3:
				t1 = new cloudToMongo("sensort2", "Temperatura2");
				break;
			case 4:
				t1 = new cloudToMongo("sensorl1", "Luminosidade1");
				break;
			case 5:
				t1 = new cloudToMongo("sensorl2", "Luminosidade2");
				break;
				
			}
			t1.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void init() {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2 - (300 / 2), dimension.height / 2 - (150 / 2));
		frame.setSize(500, 100);
		frame.setVisible(true);
	}


	public static void main(String[] args) {
		new ProcessBuilderTester();
	}


}
