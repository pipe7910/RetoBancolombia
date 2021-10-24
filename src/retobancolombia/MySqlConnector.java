/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package retobancolombia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
/**
 *
 * @author juan.montoya
 */
public class MySqlConnector {
	private static final String rutaConfig = 
				"..\\RetoBancolombia\\src\\resources\\props.properties" ;
	private static final String urlWebService = 
				"https://test.evalartapp.com/extapiquest/code_decrypt/" ;
    
    public MySqlConnector() {
    }

    public Connection getConexion(){
        Connection con = null;
        try{
           Properties propiedades = new Properties();
           propiedades.load(new FileReader(rutaConfig));
           Class.forName("com.mysql.cj.jdbc.Driver");
           con = (Connection) DriverManager.getConnection(propiedades.getProperty("URL"), propiedades.getProperty("USER"), propiedades.getProperty("password"));
        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return con;
    }

    public void consulta(Connection con, ArrayList<Filtro> filtros){
        StringBuilder whereStatement = new StringBuilder();
        StringBuilder havingStatement = new StringBuilder();
        for(int i=0; i < filtros.size(); i++){
            if(filtros.get(i).getCampo().equals("balance >") || filtros.get(i).getCampo().equals("balance <")){
                if(havingStatement.length() == 0)
                    havingStatement.append("HAVING ");
                else
                    havingStatement.append(" AND ");
                havingStatement.append("SUM(a.balance) " + filtros.get(i).getCampo().split(" ")[1]);
                if(i+1 == filtros.size())
                    havingStatement.append(" ?");
                else
                    havingStatement.append(" ? AND ");
            }else{
                if(whereStatement.length() == 0)
                    whereStatement.append("WHERE ");
                else
                    whereStatement.append(" AND ");
                whereStatement.append(filtros.get(i).getCampo());
                    whereStatement.append(" ?");
            }
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT c.code, c.male, c.company, c.encrypt, SUM(a.balance) AS balance"
                + " FROM evalart_reto.client c join evalart_reto.account a ON c.id = a.client_id " 
                + whereStatement.toString() 
                + " GROUP BY c.code, c.male, c.company, c.encrypt "
                + havingStatement.toString()
                + " ORDER BY SUM(a.balance) DESC, c.code ASC")) {
                
            for(int j=0; j < filtros.size(); j++){
                
                if(filtros.get(j).getCampo().equals("balance >") || filtros.get(j).getCampo().equals("balance <"))
                    stmt.setDouble(j+1, new Double(filtros.get(j).getValorNum()));
                else if(!filtros.get(j).getValorString().equals(""))
                    stmt.setString(j+1, filtros.get(j).getValorString());
                else
                    stmt.setInt(j+1, (int) filtros.get(j).getValorNum());
            }
            
            System.out.println (filtros.get(0).getMesa());

            ResultSet rs = stmt.executeQuery();
            ArrayList<Persona> personas = new ArrayList<>();
            while (rs.next()){
                Persona persona;
                if(rs.getInt("encrypt") == 1)
                    persona = new Persona(peticionHttpGet(rs.getString("code")), rs.getInt("male"), 
                            rs.getString("company"), rs.getInt("encrypt"), rs.getDouble("balance"));
                else
                    persona = new Persona(rs.getString("code"), rs.getInt("male"), 
                            rs.getString("company"), rs.getInt("encrypt"), rs.getDouble("balance"));
                personas.add(persona);
            }
            if(personas.size() < 4){
                System.out.println ("CANCELADA");
            }else{
                for(int i = 0; i < personas.size(); i++ ){
                    for(int j = i+1; j < personas.size(); j++){
                        if(personas.get(i).getCompany().equals(personas.get(j).getCompany()))
                            personas.remove(j);
                    }
                }
                boolean band_hom_muj = false;
                while(band_hom_muj == false){
                    int hombres = 0;
                    int mujeres = 0;
                    int posUltHombre = 0;
                    int posUltMujer = 0;
                    for(int i = 0; i < personas.size() && i < 8; i++){
                        if(personas.get(i).getMale() == 1){
                            hombres++;
                            posUltHombre = i;
                        }else{
                            mujeres++;
                            posUltMujer = i;
                        }
                    }
                    if(hombres == mujeres){
                        band_hom_muj = true;
                    }else if(hombres > mujeres){
                        personas.remove(posUltHombre);
                    }else{
                        personas.remove(posUltMujer);
                    }
                }
                if(personas.size() < 4){
                    System.out.println ("CANCELADA");
                }else{
                    StringBuilder salidaMesa = new StringBuilder();
                    for(int i=0; i < personas.size() && i < 8; i++){
                        if(i+1 == personas.size() || i+1 == 8)
                            salidaMesa.append(personas.get(i).getCode());
                        else{
                            salidaMesa.append(personas.get(i).getCode());
                            salidaMesa.append(",");
                        }
                    }
                    System.out.println (salidaMesa.toString());

                }
                
            }
        } catch (SQLException sqle) { 
          System.out.println("Error en la ejecución:" 
            + sqle.getErrorCode() + " " + sqle.getMessage());    
        }
    }

    public String peticionHttpGet(String codigoEncriptado) {
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

}
