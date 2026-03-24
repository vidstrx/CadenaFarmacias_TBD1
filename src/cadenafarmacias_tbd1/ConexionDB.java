/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cadenafarmacias_tbd1;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David Samuel
 */
public class ConexionDB {
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Properties properties = new Properties();
                // Cargamos el archivo de configuración
                FileInputStream fis = new FileInputStream("db.properties");
                properties.load(fis);

                String url = properties.getProperty("db.url");
                String user = properties.getProperty("db.user");
                String pass = properties.getProperty("db.password");

                connection = DriverManager.getConnection(url, user, pass);
                System.out.println("Conexion exitosa a la base de datos en la nube.");
                
                
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
    
    public DefaultTableModel buscarProducto(String nombre_producto, int id_farmacia, DefaultTableModel modelo){
        String comando = "CALL buscar_producto(?,?)";
        try {
            CallableStatement callStmt = connection.prepareCall(comando);
            callStmt.setString(1, nombre_producto);
            callStmt.setInt(2, id_farmacia);
            
            ResultSet rs = callStmt.executeQuery();
            while(rs.next()){
                modelo.addRow(new Object[] {
                    rs.getInt("codigo_lote"), 
                    rs.getInt("id_producto"), 
                    rs.getString("nombre_producto"), 
                    rs.getString("tipo_presentacion"), 
                    rs.getString("volumen_cantidad"), 
                    rs.getDouble("precio"), 
                    rs.getInt("cantidad"),
                    rs.getDate("fecha_vencimiento")
                });
            }    
            return modelo;
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
    
    public String idExiste(String id, String campo1, String campo2, String tabla){
        String comando = "SELECT * FROM " + tabla + " WHERE " + campo1 + " = ? LIMIT 1";
        PreparedStatement st;
        try {
            st = connection.prepareStatement(comando);
            st.setString(1, id);
            
            ResultSet nueva_tabla = st.executeQuery();
            
            if(nueva_tabla.next())
                return nueva_tabla.getString(campo2);
            else
                return "";
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return "";
        }
    }
    
    public void iniciarRecepcion(int id_farmacia, int id_proveedor, int id_empleado){
        String comando = "{call insertar_recepcion_suministra(?, ?, ?)}";
        try(CallableStatement cs = connection.prepareCall(comando)){
            cs.setInt(1, id_farmacia);
            cs.setInt(2, id_proveedor);
            cs.setInt(3, id_empleado);
            cs.execute();
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public void iniciarTurno(){
        String comando = "{call insertar_turno()}";
        try(CallableStatement cs = connection.prepareCall(comando)){
            cs.execute();
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public void insertarDetalleRecepcion(int id_farmacia, int id_producto, int cantidad, double precio, LocalDate fecha_vencimiento) throws SQLException{
        String comando = "{Call insertar_detalle_recepcion_lote_almacena(?,?,?,?,?)}";
        try(CallableStatement cs = connection.prepareCall(comando)){
            cs.setInt(1, id_farmacia);
            cs.setInt(2, id_producto);
            cs.setInt(3, cantidad);
            cs.setDouble(4, precio);
            java.sql.Date fecha = java.sql.Date.valueOf(fecha_vencimiento);
            cs.setDate(5, fecha);
            cs.execute();
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public void finalizarTurno(){
        String comando = "UPDATE turno SET hora_final = CURRENT_TIME() WHERE id_turno = (SELECT * FROM (SELECT MAX(id_turno) FROM turno) AS t)";
        PreparedStatement st;
        try{
            st = connection.prepareStatement(comando);
            int cambio = st.executeUpdate();
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public String verificarStock(int id_farmacia, String nombre_producto, int codigo_lote, int cantidad) {
        String comando = "CALL verificar_stock(?,?,?,?,?)";
        try {
            CallableStatement callStmt = connection.prepareCall(comando);
            callStmt.setInt(1, id_farmacia);
            callStmt.setString(2, nombre_producto);
            callStmt.setInt(3, codigo_lote);
            callStmt.setInt(4, cantidad);
            callStmt.registerOutParameter(5, Types.VARCHAR);
            callStmt.execute();
            
            return callStmt.getString(5);
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
    
    public void insertar_venta(int id_empleado, int id_farmacia, int id_cliente, String metodo_pago){
        String comando = "call insertar_venta(?,?,?,?)";
        try {
            CallableStatement callStmt = connection.prepareCall(comando);
            callStmt.setInt(1, id_empleado);
            callStmt.setInt(2, id_farmacia);
            callStmt.setInt(3, id_cliente);
            callStmt.setString(4, metodo_pago);
            
            callStmt.execute();
        } catch (SQLException e) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
        }
    }
    
    public double insertar_detalle_venta(int codigo_lote, int id_producto, int cantidad){
        String comando = "call insertar_detalle_venta(?,?,?,?)";
        try {
            CallableStatement callStmt = connection.prepareCall(comando);
            callStmt.setInt(1, codigo_lote);
            callStmt.setInt(2, id_producto);
            callStmt.setInt(3, cantidad);
            callStmt.registerOutParameter(4, Types.DECIMAL);
            callStmt.execute();
            
            return callStmt.getDouble(4);
        } catch (SQLException e) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            return 0;
        }
    }
    
    public void insertar_conciliacion(int id_farmacia){
        String comando = "call insertar_conciliacion(?)";
        try {
            CallableStatement callStmt = connection.prepareCall(comando);
            callStmt.setInt(1, id_farmacia);
            callStmt.execute();
            
        } catch (SQLException e) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
        }
    }
    
    public ResultSet enumerarProductos(int id_farmacia){
        String comando = "SELECT p.nombre_producto, p.id_producto FROM producto p, almacena a WHERE p.id_producto = a.id_producto AND a.id_farmacia = ?;";
        PreparedStatement st;
        try {
            st = connection.prepareStatement(comando);
            st.setInt(1, id_farmacia);
            
            ResultSet nueva_tabla = st.executeQuery();
            return nueva_tabla;
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return null;
        }
    }
    
    public void insertar_detalle_conciliacion(int id_farmacia, int id_producto, int cantidad){
        String comando = "call insertar_detalle_conciliacion(?, ?, ?)";
        try {
            CallableStatement callStmt = connection.prepareCall(comando);
            callStmt.setInt(1, id_farmacia);
            callStmt.setInt(2, id_producto);
            callStmt.setInt(3, cantidad);
            String mensaje = callStmt.getString(4);
            callStmt.execute();
            
            if(!mensaje.equals(" "))
                JOptionPane.showConfirmDialog(null, mensaje, "CUIDADO", JOptionPane.WARNING_MESSAGE);
            
        } catch (SQLException e) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
        }
    }
    
//    public void insertar(String nombre, String email, String telefono) {
//        String query = "insert into clientes(nombre,email,telefono) values (?,?,?)";
//        try {
//            PreparedStatement ps = connection.prepareStatement(query);
//            ps.setString(1, nombre);
//            ps.setString(2, email);
//            ps.setString(3, telefono);
//            ps.executeUpdate();
//            
//            System.out.println("\nDato insertado correctamente");
//        } catch (SQLException e) {
//            System.out.println("Error al insertar");
//            e.printStackTrace();
//        }
//    }
}
