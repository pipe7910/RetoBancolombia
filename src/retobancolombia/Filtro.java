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
    private double valorNum;

    public Filtro(String mesa, String campo, String valorString, double valorNum) {
        this.mesa = mesa;
        this.campo = campo;
        this.valorString = valorString;
        this.valorNum = valorNum;
    }

    public String getMesa() {
        return mesa;
    }
    public void setMesa(String mesa) {
        this.mesa = mesa;
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
