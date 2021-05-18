package SID.Conexoes;

import java.sql.SQLException;
import java.util.Arrays;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.ServerAddress;

// Monitorizacao:
//  -Temperatura1
//  -Temperatura2
//  -Luminosidade1
//  -Luminosidade2
//  -Humidade1
//  -Humidade2
//  -Backup
//
// replicaMonitorizacao:
//  -27016 db1
//  -23016 db2
//  -25016 db3

public class mongoToJava extends Thread {

	//conexao mongo
	private String user = "owner";
	private String database = "Monitorizacao";
	private char[] password = {'m', 't', 'T', 'L', '5', 'B', 'W', 'd', '6', 'F', 'f', '3'};
	private String collectionName;
	//colecoes mongo 
	private DBCollection collection;
	private DBCollection collectionBackup;
	//tratamento de dados
	private DBObject[] errorVet = new DBObject[3];
	private int errorInt = 0;
	private double refMedicao = 10000; //10000?
	private double MAX_VARIATION=5; 

	//construtor
	public mongoToJava(String collectionName) {
		this.collectionName = collectionName; 
	}

	//run
	@SuppressWarnings({ "deprecation", "static-access" })
	public void run() {

		//conexao mongo
		MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(user, database, password);
		MongoClient mongo = new MongoClient(
				new MongoClientURI("mongodb://localhost:27016,localhost:23016,localhost:25016/?replicaSet=replicaMonitorizacao"));

		// Conexao à base de dados do mongo (Monitorizacao)
		DB db = mongo.getDB("Monitorizacao");
		System.out.println(db.getName());

		// Conexao à coleçao 
		collection = db.getCollection(collectionName);
		System.out.println(collection.getName());

		// Conexao à colecao backup
		collectionBackup = db.getCollection("Backup");
		System.out.println(collectionBackup.getName());

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
				cursor = collection.find().sort(new BasicDBObject("_id", -1)); // -1 for descending, 1 for
				// ascending
				while (cursor.hasNext()) {
					leitura = cursor.next();
					System.out.println("Leitura sensor: " + leitura);

					//Tratamento de dados
					double d = Double.valueOf(leitura.get("Medicao").toString());
					boolean isSuspect = isSuspect(d);
					boolean isCoerent = isCoerent(d);
					//	-suspeitos no vetor
					if(isSuspect) {
						System.out.println("suspeita: " + errorInt);
						errorVet[errorInt] = leitura;
						if(errorInt >= 0 && errorInt <= 2) //vetor nao cheio
							errorInt++;
					}
					//	-inserir no SQL
					if(!(errorInt >= 0 && errorInt < 3 && isSuspect && isCoerent)) { // !(suspeita coerante antes de encher o vetor de analise)
						insertSQL(leitura, isSuspect, isCoerent, d);
						errorInt = 0; //reset do vetor
					}
					//mover para backup
					collectionToBackup(leitura);

				} 
				System.out.println("Waiting for the sensor");
				
			} catch (MongoSocketOpenException e) {
				System.out.println("Sensor interrompido");
			} catch (DuplicateKeyException e) {
				System.out.println("Sensor não forneceu novas leituras.");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				errorInt = 0;
				e.printStackTrace();
			}

			try {
				this.sleep(4000);
			} catch(InterruptedException e1) {
				break;
			}


		}

		System.out.println("Fim sensor " + collectionName);
		mongo.close();
	}



	//auxiliares

	public boolean isSuspect(double d) {		
		//compara com o valor de refência
		if(refMedicao != 10000 && Math.abs(d-refMedicao) > MAX_VARIATION)  
			return true;
		return false;
	}

	public boolean isCoerent(double d) {
		//compara com a ultima medicao suspeita (caso exista)
		if(errorInt != 0 && (Math.abs(d-Double.valueOf(errorVet[errorInt-1].get("Medicao").toString())) > MAX_VARIATION))
			return false;
		return true;
	}

	public void insertSQLMedicao(DBObject leitura, double medicao)  {
		try {
			//insert in SQL
			javaToSQL.insertTabela(leitura, true);
			// Passa a leitura para a colecao backup e apaga
			//collectionToBackup(leitura);
			refMedicao = medicao;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertSQLMedicaoErro(DBObject leitura) {
		try {
			//insert in SQL
			javaToSQL.insertTabela(leitura, false);
			// Passa a leitura para a colecao backup e apaga
			//collectionToBackup(leitura);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// Passa a leitura para a colecao backup e apaga
			//collectionToBackup(leitura);
			e.printStackTrace();
		}
	}

	public void insertSQL(DBObject leitura, boolean  isSuspect, boolean isCoerent, double medicao) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		//todas certas - isSuspect, isCoerent, ultima
		//todas erradas - isSuspect, !isCoerent, ultima ou nao
		//so ultima correta - !isSuspect, !isCoerent, ultima ou nao
		
		if(errorInt == 0) { //fora da analise - correta
			insertSQLMedicao(leitura, medicao);
			System.out.println("Leitura inserida naturalmente");
		} else { //dentro da analise
			if(errorInt == 3 && isSuspect && isCoerent) { //ultima, suspeita, coerente - todas corretas
				for(int i = 0; i < errorInt; i++)
					insertSQLMedicao(errorVet[i], medicao);
				System.out.println("Leituras inseridas todas corretas");

			} else { 
				if(isSuspect) { //suspeita, incoerente - todas erradas
					for(int i = 0; i < errorInt; i++)
						insertSQLMedicaoErro(errorVet[i]);
					System.out.println("Leituras inseridas todas erradas");

				}
				else { //nao suspeita, incoerente - so a ultima esta correta
					for(int i = 0; i < errorInt; i++)
						insertSQLMedicaoErro(errorVet[i]);
					insertSQLMedicao(leitura, medicao);
					System.out.println("Leituras inseridas so a ultima esta correta");
				}
			}
		}

	}
	

	public void collectionToBackup(DBObject leitura) {
		// Passa a leitura para a colecao backup e apaga 
		collectionBackup.insert(leitura);
		collection.remove(leitura);
	}


	//main
	public static void main(String[] args) {
		mongoToJava c = new mongoToJava("Temperatura1");
		c.start();
	}

}
