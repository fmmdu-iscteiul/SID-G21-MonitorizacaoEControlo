package SID.Conexoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.DBObject;

public class javaToSQL {

	//static String url = "jdbc:mysql://194.210.86.10:3306/aluno_g21";
	static String url = "jdbc:mysql://localhost/monitorizacao";
	static String user = "root";
	static String pass = "";
	static Connection con = null;

	private static void connection() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			//Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(url, user, pass);
			System.out.println("Conectado com o MySQL");
			//getNomeTabelas();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro de Conexão");
			e.printStackTrace();
		}
	}

	public static void insertTabela(DBObject leitura, boolean medicao) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String[] content = stringSplitter(leitura);
		connection();
		Statement statement = con.createStatement();
		String insertquery;
		if(medicao) {
			insertquery = "INSERT INTO `medicao`(`Zona`, `Sensor`, `Hora`, `Leitura`, `IDleitura`) VALUES ('" + content[1] + "', '"
					+ content[2] + "', '" + Timestamp.valueOf(content[3])  + "', '" + Double.valueOf(content[4]) + "', '" + content[0] + "')";

			System.out.println("Inserted na tabela medicao");
		} else {
			insertquery = "INSERT INTO `medicao_erro`(`Zona`, `Sensor`, `Hora`, `Leitura`, `IDleitura`) VALUES ('" + content[1] + "', '"
					+ content[2] + "', '" + Timestamp.valueOf(content[3])  + "', '" + Double.valueOf(content[4]) + "', '" + content[0] + "')";

			System.out.println("Inserted na tabela erro.");		
		}
		
		statement.executeUpdate(insertquery);

	}

	private static String[] stringSplitter(DBObject leitura) {
		String id = leitura.get("_id").toString();
		String zona = leitura.get("Zona").toString();
		String sensor = leitura.get("Sensor").toString();
		String data = leitura.get("Data").toString();
		String medicao = leitura.get("Medicao").toString();
		//		StringUtils.substringsBetween(id, "ObjectID(\"", "\")");
		data = StringUtils.replace(data, "T", " ");
		data = StringUtils.replace(data, "Z", "");

		String[] r = {id, zona, sensor, data, medicao};
		for(int i = 0; i < 5; i++)
			System.out.println("Excerto " + i + " " + r[i]);
		return r;
	}


	public static void getNomeTabelas()
	{
		try {

			java.sql.DatabaseMetaData dbmd = con.getMetaData();
			String[] type = {"TABLE"};
			ResultSet rs = dbmd.getTables("NADA", null, "%", type);
			while (rs.next()) {
				System.out.println(rs.getString("TABLE_NAME"));
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

}