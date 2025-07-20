package api.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AutomationService {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private TransaccionesService transaccionesService;

    @Autowired
    private UserService userService;

    /**
     * Distribuye automáticamente un ingreso de dinero según los presupuestos activos del mes
     * @param montoIngreso Monto del ingreso a distribuir
     * @param fechaIngreso Fecha del ingreso
     * @param userEmail Email del usuario
     * @param motivoOriginal Motivo del ingreso original
     * @return Lista de transacciones creadas para la distribución
     */
    public List<Transacciones> distribuirIngresoAutomaticamente(Double montoIngreso, LocalDate fechaIngreso, 
                                                               String userEmail, String motivoOriginal) {
        List<Transacciones> transaccionesCreadas = new ArrayList<>();
        
        try {
            // Obtener el usuario
            User user = userService.findByEmail(userEmail);
            if (user == null) {
                throw new RuntimeException("Usuario no encontrado");
            }

            // Obtener presupuestos del mes actual
            List<Budget> presupuestosDelMes = obtenerPresupuestosDelMes(user, fechaIngreso);
            
            if (presupuestosDelMes.isEmpty()) {
                // No hay presupuestos activos, no se puede distribuir
                return transaccionesCreadas;
            }

            // Para cada presupuesto del mes, distribuir proporcionalmente
            for (Budget presupuesto : presupuestosDelMes) {
                List<Transacciones> transaccionesPresupuesto = distribuirSegunPresupuesto(
                    presupuesto, montoIngreso, fechaIngreso, userEmail, motivoOriginal
                );
                transaccionesCreadas.addAll(transaccionesPresupuesto);
            }

        } catch (Exception e) {
            System.err.println("Error en distribución automática: " + e.getMessage());
            e.printStackTrace();
        }

        return transaccionesCreadas;
    }

    /**
     * Obtiene los presupuestos activos para el mes de la fecha dada
     */
    private List<Budget> obtenerPresupuestosDelMes(User user, LocalDate fecha) {
        List<Budget> todosLosPresupuestos = budgetService.getPresupuestosByUserId(user);
        List<Budget> presupuestosDelMes = new ArrayList<>();
        
        // Formato del mes para comparar (YYYY-MM)
        String mesFormatoComparacion = fecha.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        for (Budget presupuesto : todosLosPresupuestos) {
            if (presupuesto.getBudgetMonth() != null) {
                // Extraer solo YYYY-MM del budget_month que puede tener formato completo
                String budgetMonthStr = presupuesto.getBudgetMonth();
                
                // Si el budget_month contiene una fecha completa, extraer solo YYYY-MM
                if (budgetMonthStr.length() >= 7) {
                    String budgetYearMonth = budgetMonthStr.substring(0, 7); // Obtener YYYY-MM
                    if (budgetYearMonth.equals(mesFormatoComparacion)) {
                        presupuestosDelMes.add(presupuesto);
                    }
                } else if (budgetMonthStr.equals(mesFormatoComparacion)) {
                    // Si ya está en formato YYYY-MM, comparar directamente
                    presupuestosDelMes.add(presupuesto);
                }
            }
        }
        
        return presupuestosDelMes;
    }

    /**
     * Distribuye el monto según las categorías de un presupuesto específico
     */
    private List<Transacciones> distribuirSegunPresupuesto(Budget presupuesto, Double montoTotal, 
                                                          LocalDate fecha, String userEmail, String motivoOriginal) {
        List<Transacciones> transacciones = new ArrayList<>();
        
        Map<String, Integer> categoryBudgets = presupuesto.getCategoryBudgets();
        if (categoryBudgets == null || categoryBudgets.isEmpty()) {
            return transacciones;
        }

        // Calcular total del presupuesto
        int totalPresupuesto = categoryBudgets.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalPresupuesto <= 0) {
            return transacciones;
        }

        // Distribuir proporcionalmente por cada categoría
        for (Map.Entry<String, Integer> entry : categoryBudgets.entrySet()) {
            String categoria = entry.getKey();
            Integer presupuestoCategoria = entry.getValue();
            
            if (presupuestoCategoria > 0) {
                // Calcular porcentaje de esta categoría
                double porcentaje = (double) presupuestoCategoria / totalPresupuesto;
                
                // Calcular monto proporcional
                double montoCategoria = montoTotal * porcentaje;
                
                // Redondear a 2 decimales
                montoCategoria = Math.round(montoCategoria * 100.0) / 100.0;
                
                if (montoCategoria > 0) {
                    // Crear transacción para esta categoría
                    Transacciones transaccion = new Transacciones();
                    transaccion.setValor(montoCategoria);
                    transaccion.setCategoria(categoria);
                    transaccion.setFecha(fecha);
                    transaccion.setTipoGasto("Distribución Automática");
                    transaccion.setMotivo("Distribución automática de: " + motivoOriginal + 
                                        " (" + String.format("%.1f", porcentaje * 100) + "% del presupuesto)");
                    
                    // Guardar la transacción
                    Transacciones transaccionGuardada = transaccionesService.createTransaccion(transaccion, userEmail);
                    transacciones.add(transaccionGuardada);
                }
            }
        }
        
        return transacciones;
    }

    /**
     * Verifica si es posible distribuir automáticamente para un usuario
     * @param userEmail Email del usuario
     * @param fecha Fecha para verificar presupuestos
     * @return true si hay presupuestos activos para distribuir
     */
    public boolean puedeDistribuirAutomaticamente(String userEmail, LocalDate fecha) {
        try {
            User user = userService.findByEmail(userEmail);
            if (user == null) {
                System.out.println("DEBUG: Usuario no encontrado: " + userEmail);
                return false;
            }
            
            List<Budget> presupuestosDelMes = obtenerPresupuestosDelMes(user, fecha);
            
            // Debug: mostrar información
            String mesComparacion = fecha.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            System.out.println("DEBUG: Buscando presupuestos para el mes: " + mesComparacion);
            System.out.println("DEBUG: Presupuestos encontrados: " + presupuestosDelMes.size());
            
            for (Budget budget : presupuestosDelMes) {
                System.out.println("DEBUG: Presupuesto encontrado - Nombre: " + budget.getNameBudget() + 
                                 ", Mes: " + budget.getBudgetMonth());
            }
            
            return !presupuestosDelMes.isEmpty();
        } catch (Exception e) {
            System.err.println("ERROR en puedeDistribuirAutomaticamente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene información sobre la distribución que se realizaría
     * @param montoIngreso Monto a distribuir
     * @param userEmail Email del usuario
     * @param fecha Fecha para los presupuestos
     * @return Información de la distribución previa
     */
    public DistributionPreview obtenerPrevisualizacionDistribucion(Double montoIngreso, String userEmail, LocalDate fecha) {
        DistributionPreview preview = new DistributionPreview();
        
        try {
            User user = userService.findByEmail(userEmail);
            if (user == null) {
                preview.setError("Usuario no encontrado");
                return preview;
            }

            List<Budget> presupuestosDelMes = obtenerPresupuestosDelMes(user, fecha);
            
            if (presupuestosDelMes.isEmpty()) {
                preview.setError("No hay presupuestos activos para este mes");
                return preview;
            }

            preview.setMontoTotal(montoIngreso);
            preview.setFecha(fecha);
            preview.setPuedeDistribuir(true);

            for (Budget presupuesto : presupuestosDelMes) {
                DistributionPreview.BudgetDistribution budgetDist = preview.new BudgetDistribution();
                budgetDist.setNombrePresupuesto(presupuesto.getNameBudget());
                
                Map<String, Integer> categoryBudgets = presupuesto.getCategoryBudgets();
                if (categoryBudgets != null && !categoryBudgets.isEmpty()) {
                    int totalPresupuesto = categoryBudgets.values().stream().mapToInt(Integer::intValue).sum();
                    
                    for (Map.Entry<String, Integer> entry : categoryBudgets.entrySet()) {
                        String categoria = entry.getKey();
                        Integer presupuestoCategoria = entry.getValue();
                        
                        if (presupuestoCategoria > 0) {
                            double porcentaje = (double) presupuestoCategoria / totalPresupuesto;
                            double montoCategoria = montoIngreso * porcentaje;
                            montoCategoria = Math.round(montoCategoria * 100.0) / 100.0;
                            
                            DistributionPreview.CategoryDistribution catDist = preview.new CategoryDistribution();
                            catDist.setCategoria(categoria);
                            catDist.setMonto(montoCategoria);
                            catDist.setPorcentaje(porcentaje * 100);
                            catDist.setPresupuestoCategoria(presupuestoCategoria);
                            
                            budgetDist.getCategorias().add(catDist);
                        }
                    }
                }
                
                preview.getPresupuestos().add(budgetDist);
            }

        } catch (Exception e) {
            preview.setError("Error al calcular distribución: " + e.getMessage());
        }
        
        return preview;
    }

    // Clase para la previsualización de distribución
    public class DistributionPreview {
        private Double montoTotal;
        private LocalDate fecha;
        private boolean puedeDistribuir;
        private String error;
        private List<BudgetDistribution> presupuestos = new ArrayList<>();

        // Getters y setters
        public Double getMontoTotal() { return montoTotal; }
        public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }
        
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        
        public boolean isPuedeDistribuir() { return puedeDistribuir; }
        public void setPuedeDistribuir(boolean puedeDistribuir) { this.puedeDistribuir = puedeDistribuir; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public List<BudgetDistribution> getPresupuestos() { return presupuestos; }
        public void setPresupuestos(List<BudgetDistribution> presupuestos) { this.presupuestos = presupuestos; }

        public class BudgetDistribution {
            private String nombrePresupuesto;
            private List<CategoryDistribution> categorias = new ArrayList<>();

            public String getNombrePresupuesto() { return nombrePresupuesto; }
            public void setNombrePresupuesto(String nombrePresupuesto) { this.nombrePresupuesto = nombrePresupuesto; }
            
            public List<CategoryDistribution> getCategorias() { return categorias; }
            public void setCategorias(List<CategoryDistribution> categorias) { this.categorias = categorias; }
        }

        public class CategoryDistribution {
            private String categoria;
            private Double monto;
            private Double porcentaje;
            private Integer presupuestoCategoria;

            public String getCategoria() { return categoria; }
            public void setCategoria(String categoria) { this.categoria = categoria; }
            
            public Double getMonto() { return monto; }
            public void setMonto(Double monto) { this.monto = monto; }
            
            public Double getPorcentaje() { return porcentaje; }
            public void setPorcentaje(Double porcentaje) { this.porcentaje = porcentaje; }
            
            public Integer getPresupuestoCategoria() { return presupuestoCategoria; }
            public void setPresupuestoCategoria(Integer presupuestoCategoria) { this.presupuestoCategoria = presupuestoCategoria; }
        }
    }
}
