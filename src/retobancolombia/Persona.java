package retobancolombia;

/**
 *
 * @author juan.montoya
 * Clase Persona que tiene los atributos de los clientes
 * Tiene de atributos code, male, company, encrypt, balance
 * Se crean getter y setters. Se crea constructor con todos los atributos.
 * Se crea método toString.
 */
public class Persona {
    private String code;
    private int male;
    private String company;
    private int encrypt;
    private double balance;

    public Persona(String code, int male, String company, int encrypt, double balance) {
        this.code = code;
        this.male = male;
        this.company = company;
        this.encrypt = encrypt;
        this.balance = balance;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public int getMale() {
        return male;
    }
    public void setMale(int male) {
        this.male = male;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public int getEncrypt() {
        return encrypt;
    }
    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Persona [code=" + code + ", male=" + male + ", company=" + company + ", encrypt=" + encrypt
                        + ", balance=" + balance + "]";
    }
}
