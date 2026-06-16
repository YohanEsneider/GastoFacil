<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>GastoFácil - Registrar Proveedor</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; }
        form { background: #f9f9f9; padding: 20px; border-radius: 5px; width: 300px; border: 1px solid #ddd; }
        input { width: 100%; padding: 8px; margin-top: 5px; margin-bottom: 15px; box-sizing: border-box; }
        button { background: #28a745; color: white; border: none; padding: 10px; width: 100%; cursor: pointer; font-size: 16px; }
        button:hover { background: #218838; }
    </style>
</head>
<body>
    <h2>Registrar Nuevo Proveedor</h2>
    
    <form action="ProveedorServlet" method="post">
        <label>Nombre del Proveedor:</label>
        <input type="text" name="nombre" required>
        
        <label>Teléfono:</label>
        <input type="text" name="telefono">
        
        <label>Dirección:</label>
        <input type="text" name="direccion">
        
        <button type="submit">Guardar Proveedor</button>
    </form>
    
    <br>
    <a href="ProveedorServlet">📋 Ver Lista de Proveedores</a>
</body>
</html>