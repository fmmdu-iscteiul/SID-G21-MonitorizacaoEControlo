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

import SID.BrokerToMongo.Broker;

public class InterfaceSensores {

	/* ola */
	public JFrame frame;
//	ProcessBuilder pb = new ProcessBuilder("java", "-jar", "C:\\Users\\Yotsuba\\Desktop\\Teste.jar"); // Torre
//	Process processo;
	ThreadTeste t;
	private Broker broker;

	private static final int n_sensores = 6;

	private final JPanel[] panels = new JPanel[n_sensores];
	private final JButton[] buttons = new JButton[n_sensores];
	
	private int iterator = 0;

	public InterfaceSensores() {
		frame = new JFrame("Inicializar migração dos dados para o mongo");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addFrameContent();
		frame.pack();
		init();
	}

	public void addFrameContent() {
		frame.setLayout(new GridLayout(1, (n_sensores + 1)));
		JButton activateAll = new JButton("Turn all sensors on");
//		JButton deactivateAll = new JButton("Turn all sensors off");
		frame.add(activateAll);
//		frame.add(deactivateAll);

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

		for (iterator = 0; iterator < n_sensores; iterator++) {
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
		}
	}

	private void Initialize(String botao) {
		try {
			
			System.out.println("\nbotao do initialize: " + botao + "\n");
//			processo = pb.start();
			
//			t = new ThreadTeste(labels[botao], processo, buttons[botao]);
//			t.start();
						
			switch (botao) {
			case "H1":
				broker = new Broker("Humidade1", "sid_g21_h1");
				break;
			case "H2":
				broker = new Broker("Humidade2", "sid_g21_h2");
				break;
			case "T1":
				broker = new Broker("Temperatura1", "sid_g21_t1");
				break;
			case "T2":
				broker = new Broker("Temperatura2", "sid_g21_t2");
				break;
			case "L1":
				broker = new Broker("Luminosidade1", "sid_g21_l1");
				break;
			case "L2":
				broker = new Broker("Luminosidade2", "sid_g21_l2");
				break;

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
		new InterfaceSensores();
	}

}
