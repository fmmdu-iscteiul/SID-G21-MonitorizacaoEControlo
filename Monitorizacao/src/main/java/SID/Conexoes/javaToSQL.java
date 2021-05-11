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

	public static void insertTabela(DBObject leitura, boolean medicao) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String[] content = stringSplitter(leitura);
		connection();
		try {
			Statement statement = con.createStatement();
			String insertquery;
			if(medicao) {
				insertquery = "INSERT INTO `medicao`(`IDMedicao`, `Zona`, `Sensor`, `Hora`, `Leitura`) VALUES ('" + content[0] + "', '" + content[1] + "', '"
						+ content[2] + "', '" + content[3]  + "', '" + content[4] + "')";
			}
			else
				insertquery = "INSERT INTO `medicao_erro`(`IDMedicao`, `Zona`, `Sensor`, `Hora`, `Leitura`) VALUES ('" + content[0] + "', '" + content[1] + "', '"
						+ content[2] + "', '" + Timestamp.valueOf(content[3])  + "', '" + Double.valueOf(content[4]) + "')";

			statement.executeUpdate(insertquery);
			System.out.print("Inserted");

		} catch (SQLException e) {
			System.out.print("Not Inserted");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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