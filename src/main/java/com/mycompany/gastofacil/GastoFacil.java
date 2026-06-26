package com.mycompany.gastofacil;

import com.mycompany.gastofacil.vista.FrmProveedor;
import javax.swing.JFrame;

public class GastoFacil {

    public static void main(String[] args) {
        // Ejecutamos la interfaz gráfica de forma segura
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // 1. Creamos una ventana transparente (JFrame)
                JFrame ventana = new JFrame("Módulo de Proveedores - GastoFácil");
                
                // 2. Creamos tu panel de proveedores (el que acabamos de reparar)
                FrmProveedor panelProveedores = new FrmProveedor();
                
                // 3. Metemos el panel dentro de la ventana
                ventana.add(panelProveedores);
                
                // 4. Configuraciones obligatorias de la ventana
                ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra el proceso al dar a la X
                ventana.pack(); // Ajusta la ventana al tamaño de tu diseño automáticamente
                ventana.setLocationRelativeTo(null); // Centra la ventana en el medio de la pantalla
                
                // 5. ¡La hacemos visible!
                ventana.setVisible(true);
            }
        });
    }
}