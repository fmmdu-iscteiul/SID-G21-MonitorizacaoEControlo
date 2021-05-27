package SID.Interfaces;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import SID.Conexoes.mongoToJava;

public class InterfaceMigracaoDireta {

	/* ola */
	public JFrame frame;

	private static final int n_sensores = 6;

	private final JPanel[] panels = new JPanel[n_sensores];
	private final JButton[] buttons = new JButton[n_sensores];
	private final JLabel[] labels = new JLabel[n_sensores];
	private Thread[] threads = new Thread[n_sensores];

	private int iterator = 0;

	public InterfaceMigracaoDireta() {
		frame = new JFrame("Inicializar migração do Mongo para o MySQL");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addFrameContent();
		frame.pack();
		init();
	}

	public void addFrameContent() {
		frame.setLayout(new GridLayout(1, (n_sensores + 1)));
		JButton activateAll = new JButton("Turn all sensors on");
		frame.add(activateAll);

		for (int i = 0; i < buttons.length; i++) {
			if (i == 0)
				buttons[i] = new JButton("H" + 1);

			else if (i == 1)
				buttons[i] = new JButton("H" + 2);

			else if (i == 2)
				buttons[i] = new JButton("T" + 1);

			else if (i == 3)
				buttons[i] = new JButton("T" + 2);

			else if (i == 4)
				buttons[i] = new JButton("L" + 1);

			else if (i == 5)
				buttons[i] = new JButton("L" + 2);

		}

		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel("Down");
		}

		for (iterator = 0; iterator < labels.length; iterator++) {
			buttons[iterator].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					((JButton)e.getSource()).getText();
					Initialize(((JButton)e.getSource()).getText());
				}
			});
		}


		activateAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < n_sensores; i++) {
					buttons[i].doClick();
				}
			}
		});

		for (int i = 0; i < panels.length; i++) {
			panels[i] = new JPanel(new GridLayout(2, 1));
			frame.add(panels[i]);
			panels[i].add(buttons[i]);
			panels[i].add(labels[i]);
		}
	}

	private void Initialize(String botao) {
		try {

			System.out.println("\nbotao do initialize: " + botao + "\n");



			String tipoSensor = "";
			String collection = "";
			int index = -1;

			switch (botao) {
			case "H1":
				tipoSensor = "HUMIDADE";
				collection = "Humidade1";
				index = 0;
				break;
			case "H2":
				tipoSensor = "HUMIDADE";
				collection = "Humidade2";
				index = 1;
				break;
			case "T1":
				tipoSensor = "TEMPERATURA";
				collection = "Temperatura1";
				index = 2;
				break;
			case "T2":
				tipoSensor = "TEMPERATURA";
				collection = "Temperatura2";
				index = 3;
				break;
			case "L1":
				tipoSensor = "LUMINOSIDADE";
				collection = "Luminosidade1";
				index = 4;
				break;
			case "L2":
				tipoSensor = "LUMINOSIDADE";
				collection = "Luminosidade2";
				index = 5;
				break;
			}

			final String[] args = {collection, tipoSensor};

			//			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "C:\\Users\\fred9\\OneDrive\\Documentos\\Faculdade\\SID\\MonitorizacaoDireta\\MigracaoDireta.jar", collection, tipoSensor);
			//			Process processo = pb.start();
			//			System.out.println(processo.pid());
			//			new mongoToJava(collection, tipoSensor).start();
			if(threads[index] == null) {
				labels[index].setText("Working");
				threads[index] = new Thread() {
					public void run() {
						mongoToJava.main(args);
					}
				};
				threads[index].start();
			}
			
			else {
				labels[index].setText("Down");
				threads[index].interrupt();
				threads[index] = null;
			}
				

		} catch (Exception e1) {
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
		new InterfaceMigracaoDireta();
	}

}
