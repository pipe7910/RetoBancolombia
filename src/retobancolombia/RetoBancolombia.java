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
import java.sql.Connection;
/**
 *
 * @author juan.montoya
 */
public class RetoBancolombia {
    //variable que contiene la ruta relativa del archivo de entrada
	private static final String rutaArchivo = 
				"..\\RetoBancolombia\\src\\resources\\entrada.txt" ;
	private static ArrayList<String> mesas = new ArrayList<String>();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //se llama al metodo para leer el archivo de filtros suministrado
        ArrayList<ArrayList<Filtro>> filtrosMesas = leerArchivoFiltros();
        //creo objeto para obtener la conexión con la base de datos
        MySqlConnector mySqlCon = new MySqlConnector();
        //obtengo la conexión creada con la base de datos
        Connection con = mySqlCon.getConexion();
        //valido si la conexión fue creada exitosamente
        if(con != null){
            //recorro la lista de mesas devueltas del archivo de filtros
            for(int i=0; i< filtrosMesas.size();i++){
                //obtengo los filtros por mesa
                ArrayList<Filtro> filtros = filtrosMesas.get(i);
                //Llamo método que hace la consulta en la base de datos
                //Y realiza los filtros por mesa y filtros generales
                mySqlCon.consulta(con, filtros, mesas.get(i));
            }
        }
    }

    //método que lee el archivo de filtros y devuelve un array de arrays de filtros
    public static ArrayList<ArrayList<Filtro>> leerArchivoFiltros(){
        //Creo la variable que tiene la ruta del archivo a leer
        File archivo = new File (rutaArchivo);
        //Creo la variable que se retornará al finalizar el método
        ArrayList<ArrayList<Filtro>> filtrosMesas = new ArrayList<>();
        //creo variable i para saber en qué número de mesa voy
        int i = 0;
        //Valido si el archivo existe
        if (archivo.exists()) {
            //Abro las variables para leer el contenido del archivo
            try( FileReader fr = new FileReader(archivo);
            BufferedReader br = new BufferedReader(fr);) {
                //declaro variable linea que servirá como iterador del contenido del archivo
                String linea;
                //declaro variable mesa que contendrá el nombre de cada una de las mesas
                String mesa = "";
                //recorro las lineas del archivo plano hasta que se acabe
                while((linea=br.readLine())!=null){
                    //valido si el primer caracter de la linea es <. 
                    //lo que significa que es el nombre de una mesa
                    if(linea.charAt(0) == '<') {
                        //creo una nueva posición en el array para guardar los filtros que tendrá la mesa
                        filtrosMesas.add(new ArrayList<Filtro>());
                        //añado el nombre de la mesa al array de mesas
                        mesas.add(linea);
                        //valido si es la primera iteración para saber si sumo al contador o no
                        if(filtrosMesas.size() != 1)
                            i++;
                    }else{
                        //Comienzo a crear los filtros de la mesa
                        //Creo el nombre de la columna correspondiente al filtro que está en el archivo plano
                        String nombreColumna = nombreColumnaTabla(linea.split(":")[0].trim());
                        //Creo la variable de valor si el filtro corresponde a un string
                        String valorString = "";
                        //Creo la variable de valor si el filtro corresponde a un número
                        double valorNum = 0;
                        //valido si el valor del filtro es númerico o string
                        if(isNumeric(linea.split(":")[1].trim())){
                            //seteo la variable del valor en vacío porque el filtro corresponde a un numero
                            valorString = "";
                            //seteo la variable del valor con lo que viene en el archivo convertido a un número
                            valorNum = new Double(linea.split(":")[1].trim());
                        }else{
                            //seteo la variable del valor con lo que viene en el archivo
                            valorString = linea.split(":")[1];
                            //seteo la variable del valor en vacío porque el filtro corresponde a un string
                            valorNum = new Double(-99);
                        }
                        //creo el filtro de la mesa con los datos obtenidos anteriormente
                        Filtro filtro = new Filtro(nombreColumna, valorString, valorNum);
                        //obtengo el array actual de filtros de la mesa correspondiente
                        ArrayList<Filtro> filtros = filtrosMesas.get(i);
                        //añado el nuevo filtro al array de filtros de la mesa
                        filtros.add(filtro);
                        //actualizo el array de filtros en el array de mesas
                        filtrosMesas.set(i,filtros);
                    }
                }
            } catch (IOException e) {
                //capturo si existe algún error en la lectura del archivo plano
            	e.printStackTrace();
            } 
        }else{
            //imprimo cuando el archivo plano no existe en la ruta especificada
            System.out.println ("Asegurese que el archivo de la ruta " + rutaArchivo + " existe");
        }
        //retorno el array de arrays de filtros
        return filtrosMesas;
    }

    //método que me ayuda a pasar del nombre del filtro del archivo
    //al nombre correspondiente de la columna en la tabla de la base de datos
    public static String nombreColumnaTabla(String filtro) {
        //creo variable de columnaTabla para contener el nombre de la columna mapeada
        String columnaTabla = "";
        //abro un switch para validar los valores del filtro enviado en el parámetro de entrada
        switch(filtro){
            //valido si el filtro tiene el valor TC
            case "TC":
                //asigno el nombre de la columna y el signo del filtro
                columnaTabla = "type =";
                break;
            //valido si el filtro tiene el valor UG
            case "UG":
                //asigno el nombre de la columna y el signo del filtro
                columnaTabla = "location =";
                break;
            //valido si el filtro tiene el valor RI
            case "RI":
                //asigno el nombre de la columna y el signo del filtro
                columnaTabla = "balance >";
                break;
            //valido si el filtro tiene el valor RF
            case "RF":
                //asigno el nombre de la columna y el signo del filtro
                columnaTabla = "balance <";
                break;
            //caso default cuando no se encuentra correspondencia
            default:
                break;
        }
        //retorno el nombre de la columna
        return columnaTabla;
    }
    
    //método para validar si una cadena es númerica o no
    public static boolean isNumeric(String cadena) {
        //declaro variable booleana a retornar
        boolean resultado;

        try {
            //parseo la cadena del parámetro de entrada a un entero
            Integer.parseInt(cadena);
            //no se generó error, entonces significa que la variable si es numérica
            resultado = true;
        } catch (NumberFormatException excepcion) {
            //se generó error de converción a número, lo que significa que la variable es string
            resultado = false;
        }
        //retorno la variable booleana
        return resultado;
    }
}
