/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package retobancolombia;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 *
 * @author juan.montoya
 */
public class RetoBancolombia {
	private static final String rutaArchivo = 
				"..\\RetoBancolombia\\src\\resources\\entrada.txt" ;
	private static final String urlWebService = 
				"https://test.evalartapp.com/extapiquest/code_decrypt/" ;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ArrayList<ArrayList<Filtro>> filtrosMesas = leerArchivoFiltros();
        MySqlConnector mySqlCon = new MySqlConnector();
        Connection con = mySqlCon.getConexion();
        //mySqlCon.consulta(con);
        for(int i=0; i< filtrosMesas.size();i++){
            ArrayList<Filtro> filtros = filtrosMesas.get(i);
            StringBuilder whereStatement = new StringBuilder();
            for(int j=0; j < filtros.size(); j++){
                whereStatement.append(filtros.get(j).getCampo());
                if(j+1 == filtros.size())
                    whereStatement.append(" (?)");
                else
                    whereStatement.append(" (?),");
            }
        }
    }

    public static ArrayList<ArrayList<Filtro>> leerArchivoFiltros(){
        File archivo = new File (rutaArchivo);
        ArrayList<ArrayList<Filtro>> filtrosMesas = new ArrayList<>();
        int i = 0;
        if (archivo.exists()) {
            try( FileReader fr = new FileReader(archivo);
            BufferedReader br = new BufferedReader(fr);) {
                String linea;
                String mesa = "";
                while((linea=br.readLine())!=null){
                    if(linea.charAt(0) == '<') {
                        filtrosMesas.add(new ArrayList<Filtro>());
                        mesa = linea;
                        if(filtrosMesas.size() != 1)
                            i++;
                    }else{
                        String nombreColumna = nombreColumnaTabla(linea.split(":")[0]);
                        if(isNumeric(linea.split(":")[1])){
                            String valorString = "";
                            int valorNum = Integer.parseInt(linea.split(":")[1]);
                        }else{
                            String valorString = linea.split(":")[1];
                            int valorNum = -99;
                        }
                        Filtro filtro = new Filtro(mesa, nombreColumnaTabla(linea.split(":")[0]), valorString, valorNum);
                        ArrayList<Filtro> filtros = filtrosMesas.get(i);
                        filtros.add(filtro);
                        filtrosMesas.set(i,filtros);
                    }
                }
            } catch (IOException e) {
            	e.printStackTrace();
            } 
        }
        return filtrosMesas;
    }

    public static String peticionHttpGet(String codigoEncriptado) {
        StringBuilder codigoDesencriptado = new StringBuilder();
        try{
            URL url = new URL(urlWebService + codigoEncriptado);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String linea;
            while ((linea = rd.readLine()) != null) {
              linea = linea.replace("\"","");
              codigoDesencriptado.append(linea);
            }
            rd.close();
        
        }catch(Exception e){
            System.out.println("Error web service " + e);
        }
        return codigoDesencriptado.toString();
    }

    public static String nombreColumnaTabla(String filtro) {
        String columnaTabla = "";
        switch(filtro){
            case "TC":
                columnaTabla = "type =";
                break;
            case "UG":
                columnaTabla = "location =";
                break;
            case "RI":
                columnaTabla = "balance >";
                break;
            case "RF":
                columnaTabla = "balance <";
                break;
            default
                break;
        }
        return columnaTabla;
    }

    public static boolean isNumeric(String cadena) {

        boolean resultado;

        try {
            Integer.parseInt(cadena);
            resultado = true;
        } catch (NumberFormatException excepcion) {
            resultado = false;
        }

        return resultado;
    }
}
