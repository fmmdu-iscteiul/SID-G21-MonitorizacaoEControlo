package SID.Conexoes;

import java.sql.SQLException;
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

import SID.FileIni.FileIni;

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

public class mongoToJava{

	// conexao mongo
	private String user = "owner";
	private String database = "Monitorizacao";
	private char[] password = { 'm', 't', 'T', 'L', '5', 'B', 'W', 'd', '6', 'F', 'f', '3' };
	private String collectionName;
	private String IPMongo;
	// colecoes mongo
	private DBCollection collection;
	private DBCollection collectionBackup;
	// tratamento de dados
	private int vetorErros; 
	private DBObject[] errorVet;
	private int errorInt = 0;
	private double refMedicao = 10000; // 10000?
	private double MAX_VARIATION;

	// construtor
	public mongoToJava(String collectionName, String tipoSensor) {
		//String aux = collectionName.substring(0, collectionName.length()-2).toUpperCase();
		this.collectionName = collectionName;
		MAX_VARIATION = FileIni.getMaxVariation(tipoSensor);
		IPMongo = FileIni.getIPMongo();
		vetorErros = FileIni.getVetorSize();
		errorVet = new DBObject[vetorErros];
		
	}
	
	public static long init;


	// run
	@SuppressWarnings({ "deprecation" })
	public void run() {

		// conexao mongo
		MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(user, database, password);

//		------------------------------tirar comentário no pc ana---------------------------------
		 
//		 MongoClient mongo = new MongoClient( new
//		 MongoClientURI("mongodb://" + IPMongo + ":27016," + IPMongo + ":23016," + IPMongo + ":25016/?replicaSet=replicaMonitorizacao"));
		 
//		-----------------------------------------------------------------------------------------
		 
		 
//		----------------------- FRED -------------------------------
		MongoClient mongo = new MongoClient(IPMongo, 27017);
//		------------------------------------------------------------

		// Conexao à base de dados do mongo (Monitorizacao)
		DB db = mongo.getDB("Monitorizacao");
//		System.out.println(db.getName());

		// Conexao à coleçao
		collection = db.getCollection(collectionName);
//		System.out.println(collection.getName());

		// Conexao à colecao backup		
		collectionBackup = db.getCollection("Backup"); 
		System.out.println(collectionBackup.getName());
		
		
		DBCursor cursor;
		
		DBObject next = null;
		
		DBObject leitura = new BasicDBObject();
		
		init = System.currentTimeMillis();

		while (true) {
			try {
//				init = System.currentTimeMillis();
				cursor = collection.find().sort(new BasicDBObject("_id", -1)); // -1 for descending, 1 for
//				// ascending

				if (cursor.hasNext()) {
					next = cursor.next();

					if (!leitura.equals(next)) {
						leitura = next;
						System.out.println("Leitura da colecao: " + leitura);

						// Tratamento de dados
						double d = Double.valueOf(leitura.get("Medicao").toString());
						boolean isSuspect = isSuspect(d);
						boolean isCoerent = isCoerent(d);
						// -suspeitos no vetor
						if (isSuspect) {
							System.out.println("suspeita: " + errorInt);
							errorVet[errorInt] = leitura;
							if (errorInt >= 0 && errorInt <= vetorErros - 1) // vetor nao cheio
								errorInt++;
						}
						// -inserir no SQL
						if (!(errorInt >= 0 && errorInt < vetorErros && isSuspect && isCoerent)) { // !(suspeita coerente antes
																							// de encher o vetor de
																							// analise)
							insertSQL(leitura, isSuspect, isCoerent, d);
							errorInt = 0; // reset do vetor
						}
						// mover para backup
								
						collectionToBackup(leitura);
						
						while(cursor.hasNext()) {
							collectionToBackup(cursor.next());
						}
						
						
						
					}
					else
						System.out.println("Waiting for entries in collection (hasNext)");
				}

				else
					System.out.println("Waiting for entries in collection");
				
				Thread.sleep(1000);

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
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				break;
			}

		}

		System.out.println("Fim sensor " + collectionName);
		mongo.close();
	}

	// auxiliares

	public boolean isSuspect(double d) {
		// compara com o valor de refência
		if (refMedicao != 10000 && Math.abs(d - refMedicao) > MAX_VARIATION)
			return true;
		return false;
	}

	public boolean isCoerent(double d) {
		// compara com a ultima medicao suspeita (caso exista)
		if (errorInt != 0
				&& (Math.abs(d - Double.valueOf(errorVet[errorInt - 1].get("Medicao").toString())) > MAX_VARIATION))
			return false;
		return true;
	}

	public void insertSQLMedicao(DBObject leitura, double medicao) {
		try {
			// insert in SQL
			javaToSQL.insertTabela(leitura, true);
			// Passa a leitura para a colecao backup e apaga
			// collectionToBackup(leitura);
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
			System.out.println("O valor não foi inserido na base de dados");
		}
	}

	public void insertSQLMedicaoErro(DBObject leitura) {
		try {
			// insert in SQL
			javaToSQL.insertTabela(leitura, false);
			// Passa a leitura para a colecao backup e apaga
			// collectionToBackup(leitura);
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
			// collectionToBackup(leitura);
			e.printStackTrace();
		}
	}

	public void insertSQL(DBObject leitura, boolean isSuspect, boolean isCoerent, double medicao)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// todas certas - isSuspect, isCoerent, ultima
		// todas erradas - isSuspect, !isCoerent, ultima ou nao
		// so ultima correta - !isSuspect, !isCoerent, ultima ou nao

		if (errorInt == 0) { // fora da analise - correta
			insertSQLMedicao(leitura, medicao);
			System.out.println("Leitura inserida naturalmente");
		} else { // dentro da analise
			if (errorInt == vetorErros && isSuspect && isCoerent) { // ultima, suspeita, coerente - todas corretas
				for (int i = 0; i < errorInt; i++)
					insertSQLMedicao(errorVet[i], medicao);
				System.out.println("Leituras inseridas todas corretas");

			} else {
				if (isSuspect) { // suspeita, incoerente - todas erradas
					for (int i = 0; i < errorInt; i++)
						insertSQLMedicaoErro(errorVet[i]);
					System.out.println("Leituras inseridas todas erradas");

				} else { // nao suspeita, incoerente - so a ultima esta correta
					for (int i = 0; i < errorInt; i++)
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

	public static void main(String[] args) {
		mongoToJava p = new mongoToJava(args[0], args[1]); //collectionName
		p.run();
	}


}
