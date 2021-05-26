package SID.grupo29.mqtt;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.mongodb.DBObject;

public class BrokerToSQL extends Thread {

	private Mqtt3AsyncClient client = MqttClient.builder().useMqttVersion3().identifier(UUID.randomUUID().toString())
			.serverHost("broker.mqttdashboard.com").serverPort(1883).buildAsync();

	private String message;
	private String topico;

	static String url = "jdbc:mysql://localhost/monitorizacao";
	static String user = "root";
	static String pass = "";
	static Connection con = null;
	
	public static long end;
	public static long count;

	public BrokerToSQL(String topico) {
		this.topico = topico;
	}

	public void run() {
		ClientConnection();
	}

	public void ClientConnection() {
		client.connectWith().simpleAuth().username("sid_g21_29").password("mtTL5BWd6Ff3".getBytes()).applySimpleAuth()
				.send().whenComplete((connAck, throwable) -> {
					if (throwable != null) {
						System.out.println("Erro de conexão");
						client.disconnect();
						return;
					} else {
						System.out.println("Client connected");
						subscribeMessage(topico);
						// Setup subscribes or start publishing
					}
				});
	}

	private void subscribeMessage(String topico) {
		client.subscribeWith().topicFilter(topico).callback(publish -> {
			if (!publish.isRetain()) {
				String message = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
				System.out.println("RECEBE " + message + " PARA MANDAR PRO SQL");
				System.out.println("BrokerToSQL " + topico + " -> " + message);
				this.message = message;
				if (!message.equals("Mensagem"))
					try {
						insertTabela(message);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			}

		}).send().whenComplete((subAck, throwable) -> {
			if (throwable != null) { // Handle failure to subscribe
				System.out.println("Tópico não subscrito.");
			} else { // Handle successful subscription, e.g. logging or incrementing a metric
				System.out.println("Topico " + topico + " subscrito com sucesso.");

			}
		});

	}

	private static String[] stringSplitter(String leitura) {
		String[] separacao = leitura.strip().split(",");
		System.out.println("Split lindo id: " + separacao[0]);
		/*
		 * String id; String zona; String sensor; String data; String medicao;
		 * System.out.println("Leitura: " + leitura); for (int i = 0; i <
		 * separacao.length; i++) { if (i == 0) { id =
		 * separacao[i].strip().split("\"")[1]; separacao[i] = id; } else if (i == 1) {
		 * zona = separacao[i].strip().split("\"")[1]; separacao[i] = zona; } else if (i
		 * == 2) { sensor = separacao[i].strip().split("\"")[1]; separacao[i] = sensor;
		 * } else if (i == 3) { data = separacao[i].strip().split("\"")[1]; separacao[i]
		 * = data; } else { medicao = separacao[i].strip().split("\"")[1]; separacao[i]
		 * = medicao; } }
		 */
		return separacao;
	}

	private static void connection() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			// Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(url, user, pass);
			System.out.println("Conectado com o MySQL");
			// getNomeTabelas();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro de Conexão");
			e.printStackTrace();
		}
	}

	public static void insertTabela(String mensagem)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String[] content = stringSplitter(mensagem);
		System.out.println("Mensagem: " + mensagem + " Data: " + content[0]);
		connection();
		Statement statement = con.createStatement();
		String insertquery;
		content[3] = StringUtils.replace(content[3], "T", " ");
		content[3] = StringUtils.replace(content[3], "Z", "");

		System.out.println(" Data: " + content[3]);

		insertquery = "INSERT INTO `medicao`(`Zona`, `Sensor`, `Hora`, `Leitura`, `IDleitura`) VALUES ('" + content[1]
				+ "', '" + content[2] + "', '" + Timestamp.valueOf(content[3]) + "', '" + Double.valueOf(content[4])
				+ "', '" + content[0] + "')";

		System.out.println("Inserted na tabela medicao");

		statement.executeUpdate(insertquery);
		count++;
		System.out.println("\nContador: " + count + "\n");

		if (count == 299) {
			end = System.currentTimeMillis() - MongoToBroker.init;
			System.out.println("\n------------------------------------------------------------\n\n"
					+ "Tempo médio de migração grupo 21: " + end / 300
					+ " milisegundos\n\n------------------------------------------------------------\n");
		}

	}

}
