package SID.grupo29.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;


public class ClientBroker29 {

	private Mqtt3AsyncClient client = MqttClient.builder().useMqttVersion3()
			.identifier(UUID.randomUUID().toString()).serverHost("broker.mqttdashboard.com").serverPort(1883)
			.buildAsync();

	private String message;
	private String topico;
	private String middleTopic;
	
	BrokerToMongo29 mongCon;
	MongoToBroker mongoToBroker;
	BrokerToSQL brokerToSQL;
	
//	boolean isConnected = false;

	public ClientBroker29(String collection, String topico) {
		middleTopic = topico+"_migracao";
		this.topico = topico;
		
		BrokerToMongo29 mongCon = new BrokerToMongo29(collection, this);
		this.mongCon = mongCon;
		
		mongoToBroker = new MongoToBroker(middleTopic, collection);
		
		brokerToSQL = new BrokerToSQL(middleTopic);
		
		ClientConnection();
		
	}

	synchronized public void ClientConnection() {
		client.connectWith().simpleAuth().username("sid_g21_29").password("mtTL5BWd6Ff3".getBytes()).applySimpleAuth()
				.send().whenComplete((connAck, throwable) -> {
					if (throwable != null) {
						System.out.println("Erro de conexão");
						client.disconnect();
						return;
					} else {
						System.out.println("Client connected in ClientBroker29");
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
				mongoToBroker.start();
				brokerToSQL.start();
			}
		});

	}

	synchronized public String getMessage() {
		return message;
	}

}
