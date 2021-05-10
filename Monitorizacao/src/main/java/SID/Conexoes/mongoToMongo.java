package SID.Conexoes;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class mongoToMongo {

	@SuppressWarnings({ "unused", "deprecation" })
	public static void main (String[] args) {
		//Criar um mongo cliente
		MongoClient mongo = new MongoClient ("localhost", 27017);
		//Conecta a uma base de dados
		DB db = mongo.getDB("Monitorizacao");
		//Conecta a coleçao existente
		DBCollection collection = db.getCollection("Teste");


		//le na colecao
		BasicDBObject searchQuery = new BasicDBObject();
//		searchQuery.put("Nome", "Maria");
		DBCursor cursor = collection.find();
		while (cursor.hasNext()) {
			System.out.println(cursor.next());

		}
		mongo.close();



	}

}
