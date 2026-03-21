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
    
    public DefaultTableModel buscarPorducto(String nombre_producto){
        String sql = "SELECT id_producto, nombre_producto, precio FROM productos WHERE nombre_producto LIKE ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, "%" + nombre_producto + "%");
            
            ResultSet tabla = stmt.executeQuery();
            DefaultTableModel modelo = new DefaultTableModel();
            
            while(tabla.next()){
                modelo.addRow(new Object[3]);
            }
            
            return modelo;
        } catch (SQLException ex) {
            System.getLogger(ConexionDB.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
    
    public void insertar(String nombre, String email, String telefono) {
        String query = "insert into clientes(nombre,email,telefono) values (?,?,?)";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, telefono);
            ps.executeUpdate();
            
            System.out.println("\nDato insertado correctamente");
        } catch (SQLException e) {
            System.out.println("Error al insertar");
            e.printStackTrace();
        }
    }
}
