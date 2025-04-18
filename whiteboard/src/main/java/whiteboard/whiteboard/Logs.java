package whiteboard.whiteboard;

public class Logs {
    private String nomeDelComando;
    private String parametro1;

    public Logs(){}
    public Logs(String msg, String parametro1){
        this.nomeDelComando=msg;
        this.parametro1=parametro1;
    }

    public String getNomeDelComando() {
        return nomeDelComando;
    }
    public String getParametro1() {
        return parametro1;
    }
    public void setNomeDelComando(String nomeDelComando) {
        this.nomeDelComando = nomeDelComando;
    }
    public void setParametro1(String parametro1) {
        this.parametro1 = parametro1;
    }
}
