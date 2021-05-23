package SID.grupo29.mqtt;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSocketOpenException;

public class MongoToBroker extends Thread{

	private Mqtt3AsyncClient client = MqttClient.builder().useMqttVersion3().identifier(UUID.randomUUID().toString())
			.serverHost("broker.mqttdashboard.com").serverPort(1883).buildAsync();

	private String message;
	// conexao mongo
	private String user = "owner";
	private String database = "Monitorizacao29";
	private char[] password = {};
	private String collectionName;
	// colecoes mongo
	private DBCollection collection;
	private String topico;
	public static long init;

	public MongoToBroker(String topico, String collectionName) {
		this.topico = topico;
		this.collectionName = collectionName;
		ClientConnection();
	}
	
	public void run() {
		MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(user, database, password);
		
//		------------------------------tirar comentário no pc ana---------------------------------
		 
//		 MongoClient mongo = new MongoClient( new
//		 MongoClientURI("mongodb://localhost:27016,localhost:23016,localhost:25016/?replicaSet=replicaMonitorizacao"));
		 
//		-----------------------------------------------------------------------------------------
		 
		 
//		----------------------- FRED -------------------------------
		
		MongoClient mongo = new MongoClient("localhost", 27017);
		
//		------------------------------------------------------------

		// Conexao à base de dados do mongo (Monitorizacao)
		DB db = mongo.getDB(database);
//		System.out.println(db.getName());

		// Conexao à coleçao
		collection = db.getCollection(collectionName);
//		System.out.println(collection.getName());

		// Conexao à colecao backup
		
//		------------------------------tirar comentário no pc ana---------------------------------
		
//		collectionBackup = db.getCollection("Backup"); 
//		System.out.println(collectionBackup.getName());
		
//		-----------------------------------------------------------------------------------------
		
		DBCursor cursor;
		
		DBObject next = null;
		
		DBObject leitura = new BasicDBObject();
		
		init = System.currentTimeMillis();
		
		while (true) {
			try {
				cursor = collection.find().sort(new BasicDBObject("_id", -1)); // -1 for descending, 1 for
				// ascending
				if (cursor.hasNext()) {
					next = cursor.next();

					if (!leitura.equals(next)) {
						leitura = next;
						message = stringSplitter(leitura);
						System.out.println("VAI MANDAR PRO BROKER "+message);
						client.publishWith().topic(topico).payload(message.getBytes()).qos(MqttQos.EXACTLY_ONCE).send()
						.whenComplete((mqtt3Publish, throwable) -> {
							if (throwable != null) {
								// Handle failure to publish
								System.out.println("Not published");
							} else {
								// Handle successful publish, e.g. logging or incrementing a metric
								System.out.println("Published");
							}
						});
					}
					else
						System.out.println("Waiting for entries in collection (hasNext)");
				}

				else
					System.out.println("Waiting for entries in collection");

				sleep(1000);
				
			} catch (MongoSocketOpenException e) {
				System.out.println("Sensor interrompido");
			} catch (DuplicateKeyException e) {
				System.out.println("Sensor não forneceu novas leituras.");
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Interrompido");
				break;
			}

		}
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
//						PublishMessage();
						// Setup subscribes or start publishing
					}
				});
	}

//	public void PublishMessage() {
//		client.publishWith().topic("sid_g21_g29").payload(message.getBytes()).qos(MqttQos.EXACTLY_ONCE).send()
//				.whenComplete((mqtt3Publish, throwable) -> {
//					if (throwable != null) {
//						// Handle failure to publish
//						System.out.println("Not published");
//					} else {
//						// Handle successful publish, e.g. logging or incrementing a metric
//						System.out.println("Published");
//					}
//				});
//	}


	private static String stringSplitter(DBObject leitura) {
		String id = leitura.get("_id").toString();
		String zona = leitura.get("Zona").toString();
		String sensor = leitura.get("Sensor").toString();
		String data = leitura.get("Data").toString();
		String medicao = leitura.get("Medicao").toString();
		//		StringUtils.substringsBetween(id, "ObjectID(\"", "\")");
		data = StringUtils.replace(data, "T", " ");
		data = StringUtils.replace(data, "Z", "");

		String r = id+","+zona+","+sensor+","+data+","+medicao;
		/*for(int i = 0; i < 5; i++)
			System.out.println("Excerto " + i + " " + r[i]);*/
		System.out.println(r);
		return r;
	}
	
}
