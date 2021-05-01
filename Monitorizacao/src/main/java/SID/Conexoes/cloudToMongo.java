package SID.Conexoes;

import java.util.Arrays;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class cloudToMongo extends Thread {

	public String sensor;
	String userCloud = "aluno"; // the userCloud name
	String databaseCloud = "admin"; // the name of the databaseCloud in which the userCloud is defined
	char[] passwordCloud = { 'a', 'l', 'u', 'n', 'o' }; // the passwordCloud as a character array

	public cloudToMongo(String sensor) {
		this.sensor = sensor;
	}

	public static void main(String[] args) {
		cloudToMongo c = new cloudToMongo("sensort1");
		c.start();
	}

	@SuppressWarnings({ "deprecation", "static-access" })
	public void run() {
		MongoCredential credential = MongoCredential.createScramSha1Credential(userCloud, databaseCloud, passwordCloud);
		MongoClient mongoClientCloud = new MongoClient(new ServerAddress("194.210.86.10", 27017),
				Arrays.asList(credential));
		
		/** ---------------------------------------- */
		// Criar um mongo cliente

		MongoClient mongo = new MongoClient("localhost", 27017);
		// Conecta a uma base de dados
		DB db = mongo.getDB("Monitorizacao");
		// Conecta a coleçao existente
		DBCollection collection = db.getCollection(sensor);

		/** ---------------------------------------- */

		DB dbCloud = mongoClientCloud.getDB("sid2021");
		DBCollection collectionCloud = dbCloud.getCollection(sensor);
//		MongoCollection<Document> collectionCloud = (MongoCollection<Document>) db.getCollection(sensor);
		int i = 0; // aux

//		FindIterable<Document> cursor = collectionCloud.find().sort(new BasicDBObject("_id", -1));
//		DBCursor cursor = collectionCloud.find().sort(new BasicDBObject("_id", -1));
		
		DBCursor cursor = collectionCloud.find();
		
//		DBCursor cursor = collectionCloud.find().sort((DBObject) new Document("_id", -1)).limit(50);
		
		DBObject[] leitura = new DBObject[5];
		
		while (cursor.hasNext() && i < 5) {
			leitura[i] = cursor.next();
			System.out.println("Leitura sensor cloud: " + leitura[i]);
			collection.insert(leitura[i]);
			i++;
			try {
				this.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		
		DBCursor cursorTeste = collection.find();
		
		while (cursorTeste.hasNext()) {
			System.out.println("Leitura Teste: " + cursorTeste.next());
		}

		System.out.println(cursor);
		System.out.println("Fim sensor " + sensor);
	}
}
