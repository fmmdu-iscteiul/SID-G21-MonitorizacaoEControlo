package SID.Conexoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class mongoToMySQL {
	
		//static String url = "jdbc:mysql://194.210.86.10:3306/aluno_g21";
		static String url = "jdbc:mysql://localhost/monitorizacao";
		static String user = "root";
		static String pass = "";
		static Connection con = null;
		
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			//Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			con = DriverManager.getConnection(url, user, pass);
			System.out.println("Conectado com o MySQL");
			getNomeTabelas();
			insertTabela();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Erro de Conexão");
			e.printStackTrace();
		}
	}
	
	public static void insertTabela() {
		try {
			Statement statement = con.createStatement();
			String insertquery = "INSERT INTO `zona`(`IDZona`) VALUES ('1900')";
	        statement.executeUpdate(insertquery);
	        System.out.print("Inserted");
			
		} catch (SQLException e) {
			System.out.print("Not Inserted");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void getNomeTabelas()
    {
        try {

            java.sql.DatabaseMetaData dbmd = con.getMetaData();
            String[] type = {"TABLE"};
            ResultSet rs = dbmd.getTables("aluno_g21", null, "%", type);
            while (rs.next()) {
                System.out.println(rs.getString("TABLE_NAME"));
            }
        } 
            catch (SQLException e) {
            e.printStackTrace();
        }
    }

}