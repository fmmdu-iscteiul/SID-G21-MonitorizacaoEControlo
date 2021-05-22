package SID.BrokerToMongo;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import SID.Conexoes.BrokerToMongo;
import SID.Conexoes.mongoToJava;

public class Broker {

	private Mqtt3AsyncClient client = MqttClient.builder().useMqttVersion3()
			.identifier(UUID.randomUUID().toString()).serverHost("broker.mqttdashboard.com").serverPort(1883)
			.buildAsync();

	private String message;
	private String topico;
	
	BrokerToMongo mongCon;
	
	mongoToJava mtj;

//	boolean isConnected = false;

	public Broker(String sensor, String topico) {
		this.topico = topico;
		BrokerToMongo mongCon = new BrokerToMongo(sensor, this);
		this.mongCon = mongCon;
		
		mongoToJava mtj = new mongoToJava(sensor);
		this.mtj = mtj;
		ClientConnection();
		
	}

	synchronized public void ClientConnection() {
		client.connectWith().simpleAuth().username("sid_g21").password("mtTL5BWd6Ff3".getBytes()).applySimpleAuth()
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

	synchronized private void subscribeMessage(String topico) {
		client.subscribeWith().topicFilter(topico).callback(publish -> {
			String message = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
			System.out.println(topico + " -> " + message);
			this.message = message;

		}).send().whenComplete((subAck, throwable) -> {
			if (throwable != null) { // Handle failure to subscribe
				System.out.println("Tópico não subscrito.");
			} 
			else { // Handle successful subscription, e.g. logging or incrementing a metric
				System.out.println("Topico " + topico + " subscrito com sucesso.");
				mongCon.start();
				mtj.start();
			}
		});

	}

	synchronized public String getMessage() {
		return message;
	}

}
