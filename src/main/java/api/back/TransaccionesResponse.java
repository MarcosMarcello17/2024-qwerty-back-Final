package api.back;

import java.util.List;

public class TransaccionesResponse {
    private List<Transacciones> transaccionesFiltradas;
    private List<Transacciones> transaccionesSinFiltrarCat;

    public TransaccionesResponse(List<Transacciones> transaccionesFiltradas, List<Transacciones> transaccionesSinFiltrarCat) {
        this.transaccionesFiltradas = transaccionesFiltradas;
        this.transaccionesSinFiltrarCat = transaccionesSinFiltrarCat;
    }

    public List<Transacciones> getTransaccionesFiltradas() {
        return transaccionesFiltradas;
    }

    public void setTransaccionesFiltradas(List<Transacciones> transaccionesFiltradas) {
        this.transaccionesFiltradas = transaccionesFiltradas;
    }

    public List<Transacciones> getTransaccionesSinFiltrarCat() {
        return transaccionesSinFiltrarCat;
    }

    public void setTransaccionesSinFiltrarCat(List<Transacciones> transaccionesSinFiltrarCat) {
        this.transaccionesSinFiltrarCat = transaccionesSinFiltrarCat;
    }
}
