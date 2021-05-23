package SID.grupo29.mqtt;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import SID.BrokerToMongo.Broker;
import SID.grupo29.mqtt.ClientBroker29;

public class BrokerToMongo29 extends Thread {

	private String colecao;
	MongoClient mongo = new MongoClient("localhost", 27017);
	ClientBroker29 ClientBroker;

	public BrokerToMongo29(String colecao, ClientBroker29 ClientBroker) {
		this.colecao = colecao;
		this.ClientBroker = ClientBroker;
	}

	public void run() {
		insertToMongo();
	}

	public void insertToMongo() {

		MongoDatabase db;

		// Conecta a uma base de dados

		db = mongo.getDatabase("Monitorizacao29");

		// Conecta a coleçao existente
		MongoCollection<Document> collection = db.getCollection(colecao);

		String leitura = "Mensagem";
		Document doc;
		while (true) {
			try {
				// analyse new entries
				sleep(1000);
				String aux;
				
				aux = ClientBroker.getMessage();
				System.out.println("Mensagem do Broker é: " + aux);
				
				if (!leitura.equals(aux) && aux != null) {
					System.out.println("Inserting in mongo");

					leitura = aux;
					System.out.println("Leitura Broker Sensor: " + leitura);
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
		for (int i = 0; i < separacao.length; i++) {
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

}
