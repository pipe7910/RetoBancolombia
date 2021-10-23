/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package retobancolombia;

/**
 *
 * @author juan.montoya
 */
public class Filtro {
    private String mesa;
    private String campo;
    private String valorString;
    private int valorNum;

    public Filtro(String mesa, String campo, String valorString, int valorNum) {
        this.mesa = mesa;
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
    public int getValorNum() {
        return valorNum;
    }
    public void setValorInt(int valorNum) {
        this.valorNum = valorNum;
    }

    @Override
    public String toString() {
        return "Filtro [campo=" + campo + ", valor=" + valor + "]";
    }
}
