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
/**
 *
 * @author juan.montoya
 */
public class MySqlConnector {
	private static final String rutaConfig = 
				"..\\RetoBancolombia\\src\\resources\\props.properties" ;
    
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
        for(int i=0; i < filtros.size(); i++){
            whereStatement.append(filtros.get(i).getCampo());
            if(i+1 == filtros.size())
                whereStatement.append(" ?");
            else
                whereStatement.append(" ?,");
        }
        try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM client" + whereStatement)) {
            for(int j=0; j < filtros.size(); j++){
                if(filtros.get(j).getValorNum() == -99)
                    sentencia.setString(j+1, filtros.get(j).getValorString());
                else
                    sentencia.setDouble(j+1, new Double(filtros.get(j).getValorNum()));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
                System.out.println (rs.getString("code"));

        } catch (SQLException sqle) { 
          System.out.println("Error en la ejecución:" 
            + sqle.getErrorCode() + " " + sqle.getMessage());    
        }
    }

}
