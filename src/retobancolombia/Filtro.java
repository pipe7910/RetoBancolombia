package retobancolombia;

/**
 *
 * @author juan.montoya
 * Clase Filtro que tiene los atributos de los filtros que tendrán las mesas
 * Tiene de atributos campo, valorString, valorNum
 * Se crean getter y setters. Se crea constructor con todos los atributos
 */
public class Filtro {
    private String campo;
    private String valorString;
    private double valorNum;

    public Filtro(String campo, String valorString, double valorNum) {
        this.campo = campo;
        this.valorString = valorString;
        this.valorNum = valorNum;
    }

    public String getCampo() {
        return campo;
    }
    public void setCampo(String campo) {
        this.campo = campo;
    }
    public String getValorString() {
        return valorString;
    }
    public void setValorString(String valorString) {
        this.valorString = valorString;
    }
    public double getValorNum() {
        return valorNum;
    }
    public void setValorInt(double valorNum) {
        this.valorNum = valorNum;
    }
}
