/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cadenafarmacias_tbd1;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.sql.*;
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
    
    public DefaultTableModel buscarPorducto(String nombre_producto, int id_farmacia){
        String sql = "SELECT id_producto, nombre_producto, precio, cantidad FROM producto p, almacena a WHERE p.id_producto = a.id_producto AND id_farmacia = ? AND nombre_producto LIKE ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, id_farmacia + "");
            stmt.setString(2, "%" + nombre_producto + "%");
            
            ResultSet tabla = stmt.executeQuery();
            DefaultTableModel modelo = new DefaultTableModel();
              
            while(tabla.next()){
                modelo.addRow(new Object[]{tabla.getInt("id_producto"), tabla.getString("nombre_producto"), tabla.getDouble("precio"), tabla.getInt("cantidad")});
            }
            
            return modelo;
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
    
    public String idExiste(String id, String campo1, String campo2, String tabla){
        String sql = "SELECT * FROM " + tabla + " WHERE " + campo1 + " = ? LIMIT 1";
        PreparedStatement st;
        try {
            st = connection.prepareStatement(sql);
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
