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
    //variable que contiene la ruta relativa de los propeties
    private static final String rutaConfig = 
                            "..\\RetoBancolombia\\src\\resources\\props.properties" ;
    
    //creo método constructor vacío
    public MySqlConnector() {
    }

    //Método para realizar la conexión contra la base de datos
    public Connection getConexion(){
        //creo variable de tipo Connection
        Connection con = null;
        try{
           //creo la variable de propiedades que leerá el archivo de props
           Properties propiedades = new Properties();
           //cargo las propiedades asignadas en el archivo de props
           propiedades.load(new FileReader(rutaConfig));
           //Obtengo el driver JDBC de mysql
           Class.forName("com.mysql.cj.jdbc.Driver");
           //creo la conexión a la base de datos con los parámetros de conexión
           //instanciados en el archivo de props
           con = (Connection) DriverManager.getConnection(propiedades.getProperty("URL"), propiedades.getProperty("USER"), propiedades.getProperty("password"));
        }catch(Exception e){
            //capturo si existió algún error de conexión
            System.out.println("Error: " + e.getMessage());
        }
        //retorno la conexión creada
        return con;
    }

    //método que realiza la consulta con los filtros de cada mesa y filtros generales
    public void consulta(Connection con, ArrayList<Filtro> filtros, String mesa){
        //creo la variable que va a contener los filtros del where de la consulta
        StringBuilder whereStatement = new StringBuilder();
        //creo la variable que va a contener los filtros del having de la consulta
        StringBuilder havingStatement = new StringBuilder();
        //recorro al array de filtros
        for(int i=0; i < filtros.size(); i++){
            //valido si el filtro corresponde a un having
            if(filtros.get(i).getCampo().equals("balance >") || filtros.get(i).getCampo().equals("balance <")){
                //valido si el stringbuilder está vacío
                if(havingStatement.length() == 0){
                    //agrego la palabra having al string builder porque comenzaré la sentencia having
                    havingStatement.append("HAVING ");
                }else{
                    //agrego la palabra and al string builder por que ya existe una condición previa
                    havingStatement.append(" AND ");
                }
                //agrego el nombre por el cual se hará el having de la consulta
                havingStatement.append("SUM(a.balance) ");
                //agrego el signo de comparación
                havingStatement.append(filtros.get(i).getCampo().split(" ")[1]);
                //agrego signo para que se le asigne valor en el prepareStatement
                havingStatement.append(" ?");
            }else{
                //valido si el stringbuilder está vacío
                if(whereStatement.length() == 0){
                    //agrego la palabra where al string builder porque comenzaré la sentencia where
                    whereStatement.append("WHERE ");
                }else{
                    //agrego la palabra and al string builder por que ya existe una condición previa
                    whereStatement.append(" AND ");
                }
                //agrego el campo a filtrar
                whereStatement.append(filtros.get(i).getCampo());
                //agrego signo para que se le asigne valor en el prepareStatement
                whereStatement.append(" ?");
            }
        }
        //creo stringbuilder que tendrá la salida de la mesa con el formato
        StringBuilder salidaMesa = new StringBuilder();
        //creo el prepareStatement con la consulta que se debe realizar con los filtros where y having creados
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT c.code, c.male, c.company, c.encrypt, SUM(a.balance) AS balance"
                + " FROM evalart_reto.client c join evalart_reto.account a ON c.id = a.client_id " 
                + whereStatement.toString() 
                + " GROUP BY c.code, c.male, c.company, c.encrypt "
                + havingStatement.toString()
                + " ORDER BY SUM(a.balance) DESC, c.code ASC")) {
                
            //recorro al array de filtros
            for(int j=0; j < filtros.size(); j++){
            //valido si el filtro corresponde a un having para asignar el double
                if(filtros.get(j).getCampo().equals("balance >") || filtros.get(j).getCampo().equals("balance <")){
                    //asigno valor double a la posición correspondiente
                    stmt.setDouble(j+1, filtros.get(j).getValorNum());
                //valido si el valor string del filtro es diferente a vacío
                }else if(!filtros.get(j).getValorString().equals("")){
                    //asigno valor string a la posición correspondiente
                    stmt.setString(j+1, filtros.get(j).getValorString());
                }else{
                    //asigno valor int a la posición correspondiente
                    stmt.setInt(j+1, (int) filtros.get(j).getValorNum());
                }
            }
            //Ejecuto el statement creado y obtengo el resultado
            ResultSet rs = stmt.executeQuery();
            //creo arraylist de personas
            ArrayList<Persona> personas = new ArrayList<>();
            //recorro el resultset obtenido de la consulta
            while (rs.next()){
                //creo una variable de tipo persona
                Persona persona;
                //valido si la persona tiene el código encriptado o no
                if(rs.getInt("encrypt") == 1){
                    //creo la nueva persona con los atributos obtenidos de la consulta
                    //como tiene el código encriptado, realizo la petición http get al
                    //web service para obtener el código desencriptado
                    persona = new Persona(peticionHttpGet(rs.getString("code")), rs.getInt("male"), 
                            rs.getString("company"), rs.getInt("encrypt"), rs.getDouble("balance"));
                }else{
                    //creo la nueva persona con los atributos obtenidos de la consulta
                    persona = new Persona(rs.getString("code"), rs.getInt("male"), 
                            rs.getString("company"), rs.getInt("encrypt"), rs.getDouble("balance"));
                }
                personas.add(persona);
            }
            //validación si el arraylist de personas creado tiene tamaño menor a 4
            if(personas.size() < 4){
                //como no se alcanzó el cupo mínimo de la mesa, se cancela la mesa
                salidaMesa.append(mesa);
                salidaMesa.append("\nCANCELADA");
            }else{
                // recorro el array de personas
                for(int i = 0; i < personas.size(); i++ ){
                    //recorro array de personas una posición más adelante
                    for(int j = i+1; j < personas.size(); j++){
                        //valido si la persona de la posición i es de la misma compañía
                        //que la persona de la posición j
                        if(personas.get(i).getCompany().equals(personas.get(j).getCompany())){
                            //elimino persona de la posición j del array
                            //no puede existir 2 personas de la misma empresa en la misma mesa
                            personas.remove(j);
                        }
                    }
                }
                //validación si el arraylist de personas creado tiene tamaño menor a 4 luego
                //de eliminar a las personas de la misma compañía
                if(personas.size() < 4){
                    //como no se alcanzó el cupo mínimo de la mesa, se cancela la mesa
                    salidaMesa.append(mesa);
                    salidaMesa.append("\nCANCELADA");
                }else{
                    //declaro bandera que define si en la mesa hay igual cantidad
                    //de hombres y mujeres
                    boolean band_hom_muj = false;
                    //recorro mientras la bandera sea falsa
                    while(band_hom_muj == false){
                        //declaro variable de cantidad de hombres
                        int cantHombres = 0;
                        //declaro variable de cantidad de mujeres
                        int cantMujeres = 0;
                        //declaro variable de posición del último hombre en la lista
                        int posUltHombre = 0;
                        //declaro variable de posición de la última mujer en la lista
                        int posUltMujer = 0;
                        //recorro array de personas hasta 8
                        for(int i = 0; i < personas.size() && i < 8; i++){
                            //valido si la persona es hombre
                            if(personas.get(i).getMale() == 1){
                                //agrego un hombre al contador
                                cantHombres++;
                                //asigno la posición donde se encontró al último hombre
                                posUltHombre = i;
                            }else{
                                //agrego una mujer al contador
                                cantMujeres++;
                                //asigno la posición donde se encontró a la última mujer
                                posUltMujer = i;
                            }
                        }
                        //valido si la cantidad de hombres es igual a la de mujeres
                        if(cantHombres == cantMujeres){
                            //asigna bandera en true para dejar de hacer el ciclo de validación
                            band_hom_muj = true;
                        //valido si la cantidad de hombres es mayor a la de mujeres
                        }else if(cantHombres > cantMujeres){
                            //elimino el hombre de la última posición encontrada
                            personas.remove(posUltHombre);
                        }else{
                            //elimino la mujer de la última posición encontrada
                            personas.remove(posUltMujer);
                        }
                    }
                    //valido si después de igualar el número de hombres y mujeres en
                    //la mesa el tamaño no cumple con el cupo mínimo
                    if(personas.size() < 4){
                        //como no se alcanzó el cupo mínimo de la mesa, se cancela la mesa
                        salidaMesa.append(mesa);
                        salidaMesa.append("\nCANCELADA");
                    }else{
                        //añado el nombre de la mesa
                        salidaMesa.append(mesa);
                        //añado salto de linea
                        salidaMesa.append("\n");
                        //recorro array de personas
                        for(int i=0; i < personas.size() && i < 8; i++){
                            //valido si la siguiente posición es la última
                            if(i+1 == personas.size() || i+1 == 8){
                                //agrego el código de la persona
                                salidaMesa.append(personas.get(i).getCode());
                            }else{
                                //agrego el código de la persona
                                salidaMesa.append(personas.get(i).getCode());
                                //separador para añadir el siguiente código
                                salidaMesa.append(",");
                            }
                        }
                    }
                }
            }
        } catch (SQLException sqle) { 
            //si existió algún error en la consulta a la BBDD. se cancela la mesa por default.
            //puede ser por el nombre del filtro u otro motivo
            salidaMesa.append(mesa);
            salidaMesa.append("\nCANCELADA");
        }
        //imprimo la salida correspondiente
        System.out.println (salidaMesa.toString());
    }

    //método que realiza la petición http get al web service de desencripción
    public String peticionHttpGet(String codigoEncriptado) {
        //creo el stringbuilder que tendrá el código desencriptado
        StringBuilder codigoDesencriptado = new StringBuilder();
        try{
            //creo la variable de propiedades que leerá el archivo de props
            Properties propiedades = new Properties();
            //cargo las propiedades asignadas en el archivo de props
            propiedades.load(new FileReader(rutaConfig));
            //creo la variable de URL del web service y el código encriptado
            URL url = new URL(propiedades.getProperty("URL_WEB_SERVICE") + codigoEncriptado);
            //abro la conexión HTTP a la url definida
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            //ejecuto el método get
            conexion.setRequestMethod("GET");
            //leo el resultado devuelto de la petición HTTP GET realizada
            BufferedReader rd = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            //declaro variable que recibirá el valor de las lineas del iterador
            String linea;
            //recorro la respuesta del web service
            while ((linea = rd.readLine()) != null) {
                //reemplazo el caracter " de la cadena
                linea = linea.replace("\"","");
                //agrego la linea al stringbuilder
                codigoDesencriptado.append(linea);
            }
            //cierro el bufferedReader
            rd.close();
        
        }catch(Exception e){
            //si hubo un error, devuelvo el mismo código del parámetro de entrada
            codigoDesencriptado.append(codigoEncriptado);
        }
        //devuelvo el código desencriptado
        return codigoDesencriptado.toString();
    }

}
