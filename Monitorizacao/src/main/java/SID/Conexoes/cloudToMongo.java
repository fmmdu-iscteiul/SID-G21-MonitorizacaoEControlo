package SID.Conexoes;

import java.util.Arrays;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.ServerAddress;

public class cloudToMongo extends Thread {

	private String sensorCloud;
	private String sensor;
	String userCloud = "aluno"; // the userCloud name
	String databaseCloud = "admin"; // the name of the databaseCloud in which the userCloud is defined
	char[] passwordCloud = { 'a', 'l', 'u', 'n', 'o' }; // the passwordCloud as a character array
	
	String user = "admin";
	String database = "Monitorizacao";
	char[] password = {'m', 't', 'T', 'L', '5', 'B', 'W', 'd', '6', 'F', 'f', '3'};
	
	public cloudToMongo(String sensorCloud, String sensor) {
		this.sensorCloud = sensorCloud;
		this.sensor = sensor;
	}

	public static void main(String[] args) {
		cloudToMongo c = new cloudToMongo("sensort1", "Temperatura1");
		c.start();
	}

	@SuppressWarnings({ "deprecation", "static-access" })
	public void run() {
		MongoCredential credential = MongoCredential.createScramSha1Credential(userCloud, databaseCloud, passwordCloud);
		MongoClient mongoClientCloud = new MongoClient(new ServerAddress("194.210.86.10", 27017),
				Arrays.asList(credential));

		/** ---------------------------------------- */
		// Criar um mongo cliente

		MongoClient mongo = new MongoClient("localhost", 27017);// usar no pc do fred
		
		/** usar no pc da ana */
//		MongoCredential ourCredentials = MongoCredential.createScramSha1Credential(user, database, password);
//		MongoClient mongo = new MongoClient(Arrays.asList(new ServerAddress("10.101.212.123", 27016), new ServerAddress("10.101.212.123", 23016), new ServerAddress("10.101.212.123", 25016)), Arrays.asList(ourCredentials));
		
		// Conecta a uma base de dados
		DB db = mongo.getDB("Monitorizacao");
		// Conecta a coleçao existente
		DBCollection collection = db.getCollection(sensor);

		/** ---------------------------------------- */

		DB dbCloud = mongoClientCloud.getDB("sid2021");
		DBCollection collectionCloud = dbCloud.getCollection(sensorCloud);

		DBCursor cursor;

		/**
		 * use only if needed // MongoCollection<Document> collectionCloud =
		 * (MongoCollection<Document>) db.getCollection(sensor); //
		 * FindIterable<Document> cursor = collectionCloud.find().sort(new
		 * BasicDBObject("_id", -1)); // DBCursor cursor = collectionCloud.find();
		 * working!!! // DBCursor cursor = collectionCloud.find().sort((DBObject) new
		 * Document("_id", -1)).limit(50);
		 */

		DBObject leitura;

		while (true) {
			try {
				cursor = collectionCloud.find().sort(new BasicDBObject("_id", -1)); // -1 for descending, 1 for
																					// ascending
				
				while (cursor.hasNext()) { //podemos usar while para testes MAS o "if" é que apanha o último valor que é inserido
					try {
					leitura = cursor.next();
					// analyse new entries
					System.out.println("Leitura sensor cloud: " + leitura);
					collection.insert(leitura);
					
//					javaToSQL.insertTabela(leitura, true);
					
					} catch (DuplicateKeyException e) {
						System.out.println("Sensor não forneceu novas leituras.");
					}
					
					
					
					this.sleep(2000);

				} 
				
				//Sysout dentro de um else (quando se usar o if na statement acima)
				System.out.println("Waiting for the sensor");
	
			} catch (MongoSocketOpenException e) {
				System.out.println("Sensor interrompido");
			} catch (InterruptedException e) {
				System.out.println("Problema - Interrupção Sensor");
				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				this.sleep(2000);
			} catch(InterruptedException e1) {
				break;
			}
			

		}

		System.out.println("Fim sensor " + sensor);
		mongo.close();
		mongoClientCloud.close();
	}
}
