package SID.Interfaces;

import javax.swing.JButton;
import javax.swing.JLabel;

public class ThreadTeste extends Thread {

	JLabel status;
	Process p;
	JButton but;

	public ThreadTeste(JLabel status, Process p, JButton but) {
		this.status = status;
		this.p = p;
		this.but = but;
		but.setEnabled(false);
	}

	public void run() {
		while(true) {
			if(p.isAlive())
				status.setText("Working");
			else {
				status.setText("Down");
				but.setEnabled(true);
				return;
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
