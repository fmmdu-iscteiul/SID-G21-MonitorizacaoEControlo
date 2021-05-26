package SID.FileIni;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class FileIni {

	public static void createIni() throws IOException {
		File f = new File("config.ini");
		if(f.createNewFile()) {
			FileWriter myWriter = new FileWriter(f);
		    myWriter.write("** TRATAMENTO DE DADOS **\n\nTamanho_vetor_de_valores_suspeitos = 3"
		    		+ "\nMAX_VARIATION_TEMPERATURA = 10\nMAX_VARIATION_HUMIDADE = 6"
		    		+ "\nMAX_VARIATION_LUMINOSIDADE = 5");
		    myWriter.close();
		} 
    }
	
	public static int getVetorSize() {
		try {
			createIni();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try (FileReader reader = new FileReader(new File("config.ini"))) {
			Properties p = new Properties();
			p.load(reader);
			return Integer.valueOf(p.getProperty("Tamanho_vetor_de_valores_suspeitos").strip());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static double getMaxVariation(String tipoSensor) {
		try {
			createIni();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try (FileReader reader = new FileReader(new File("config.ini"))) {
			Properties p = new Properties();
			p.load(reader);
			return Double.valueOf(p.getProperty("MAX_VARIATION_"+tipoSensor).strip());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static void main(String[] args) {
		System.out.println(FileIni.getVetorSize() + " " + FileIni.getMaxVariation("TEMPERATURA") 
				+ " " + FileIni.getMaxVariation("HUMIDADE") + " " + FileIni.getMaxVariation("LUMINOSIDADE") );
	}
	
}
