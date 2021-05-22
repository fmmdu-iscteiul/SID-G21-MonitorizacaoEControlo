package SID.Conexoes;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import SID.BrokerToMongo.Broker;

public class BrokerToMongo extends Thread {

	private String sensor;
	MongoClient mongo = new MongoClient("localhost", 27017);
	Broker broker;

	public BrokerToMongo(String sensor, Broker broker) {
		this.sensor = sensor;
		this.broker = broker;
	}

	public void run() {
		insertToMongo();
	}

	public void insertToMongo() {
		try {
			sleep(3000);
			System.out.println("3s sleep");
		} catch (InterruptedException e1) {
			System.out.println("Sleep disturbed");
			e1.printStackTrace();
			return;
		}

		// Conecta a uma base de dados
		MongoDatabase db = mongo.getDatabase("Monitorizacao");
		
		/**
		 * Verifica se coleção existe
		 * Caso não exista, cria uma coleção para o sensor em causa
		 */
		
//		boolean exists = db.("students");
		
		// Conecta a coleçao existente
		MongoCollection<Document> collection = db.getCollection(sensor);
		
		String leitura = "Mensagem";
		Document doc;
		while (true) {
			try {
				// analyse new entries
				sleep(1500);
				String aux = broker.getMessage();
				if (!leitura.equals(aux)) {
					System.out.println("Inserting in mongo");
					leitura = broker.getMessage();
					System.out.println("Leitura Broker: " + leitura);
					String[] s = stringSplitter(leitura);
					doc = new Document("_id", new ObjectId());
					doc.append("Zona", s[0]);
					doc.append("Sensor", s[1]);
					doc.append("Data", s[2]);
					doc.append("Medicao", s[3]);

					collection.insertOne(doc);
				}
				
				else
					System.out.println("Waiting for Sensor");

			} catch (MongoSocketOpenException e) {
				System.out.println("Sensor interrompido");
			} catch (InterruptedException e) {
				System.out.println("Problema - Interrupção Sensor");
				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static String[] stringSplitter(String leitura) {
		String[] separacao = leitura.strip().split(",");
		String zona;
		String sensor;
		String data;
		String medicao;
		for (int i = 0; i <= 3; i++) {
			if (i == 0) {
				zona = separacao[i].strip().split("\"")[1];
				separacao[i] = zona;
			} else if (i == 1) {
				sensor = separacao[i].strip().split("\"")[1];
				separacao[i] = sensor;
			} else if (i == 2) {
				data = separacao[i].strip().split("\"")[1];
				separacao[i] = data;
			} else {
				medicao = separacao[i].strip().split("\"")[1];
				separacao[i] = medicao;
			}
		}
		return separacao;
	}

	// {Zona: "Z1", Sensor: "L1", Data: "2021-05-22T14:18:19Z", Medicao:
	// "10.900000000000002" }

}
