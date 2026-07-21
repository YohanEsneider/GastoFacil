<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>GastoFácil - Módulo de Pedidos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <div class="container mt-4">
        <div class="card text-center p-4 shadow-sm mb-4" style="border: 1px solid #e3e6f0;">
            <h1 class="fw-bold text-primary display-4">GastoFácil</h1>
            <p class="text-muted fs-5 mb-3">Módulo de Administración de Pedidos</p>
            
            <div class="d-flex justify-content-center gap-2">
                <a href="index.jsp" class="btn btn-outline-secondary btn-sm">Proveedores</a>
                <a href="pedidos.jsp" class="btn btn-primary btn-sm fw-bold">Pedidos</a>
            </div>
        </div>
    </div>

    <div class="container">
        <div class="row">
            
            <div class="col-md-4 mb-4">
                <div class="card shadow-sm">
                    <div class="card-header bg-success text-white fw-bold">
                        ➕ Registrar Pedido
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/PedidoServlet" method="POST">
                            <input type="hidden" name="accion" value="guardar">
                            
                            <div class="mb-3">
                                <label for="idProveedor" class="form-label fw-semibold">Proveedor:</label>
                                <select class="form-select" id="idProveedor" name="idProveedor" required>
                                    <option selected disabled value="">Seleccionar proveedor...</option>
                                    <%
                                        String dbUrl = "jdbc:mysql://localhost:3306/gastofacil";
                                        String dbUser = "root";
                                        String dbPass = "Admin123*"; 
                                        
                                        try {
                                            Class.forName("com.mysql.cj.jdbc.Driver");
                                            Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                                            String sql = "SELECT idProveedor, nombre FROM proveedor ORDER BY nombre ASC";
                                            PreparedStatement ps = con.prepareStatement(sql);
                                            ResultSet rs = ps.executeQuery();
                                            while(rs.next()) {
                                    %>
                                                <option value="<%= rs.getInt("idProveedor") %>"><%= rs.getString("nombre") %></option>
                                    <%
                                            }
                                            rs.close(); ps.close(); con.close();
                                        } catch(Exception e) {
                                            out.println("<option disabled>Error al cargar proveedores</option>");
                                        }
                                    %>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label for="fechaPedido" class="form-label fw-semibold">Fecha de Compra:</label>
                                <input type="date" class="form-control" id="fechaPedido" name="fechaPedido" required>
                            </div>

                            <div class="mb-3">
                                <label for="descripcion" class="form-label fw-semibold">Descripción:</label>
                                <textarea class="form-control" id="descripcion" name="descripcion" rows="3" placeholder="Ej: Canastas, insumos..." required></textarea>
                            </div>

                            <div class="mb-3">
                                <label for="valorTotal" class="form-label fw-semibold">Valor Total:</label>
                                <input type="number" class="form-control" id="valorTotal" name="valorTotal" step="0.01" min="0" placeholder="0.00" required>
                            </div>

                            <div class="mb-3">
                                <label for="metodoPago" class="form-label fw-semibold">Método de Pago:</label>
                                <select class="form-select" id="metodoPago" name="metodoPago">
                                    <option value="Efectivo" selected>Efectivo</option>
                                    <option value="Transferencia">Transferencia</option>
                                    <option value="Tarjeta">Tarjeta</option>
                                </select>
                            </div>

                            <div class="d-grid mt-4">
                                <button type="submit" class="btn btn-success fw-bold">Guardar Pedido</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-md-8 mb-4">
                <div class="card shadow-sm">
                    <div class="card-header bg-light fw-bold text-secondary">
                        Pedidos Registrados
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-striped table-hover align-middle mb-0">
                                <thead class="table-dark">
                                    <tr>
                                        <th class="ps-3">Fecha</th>
                                        <th>Proveedor</th>
                                        <th>Descripción</th>
                                        <th>Total</th>
                                        <th>Pago</th>
                                        <th>Estado</th>
                                        <th class="pe-3 text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        try {
                                            Class.forName("com.mysql.cj.jdbc.Driver");
                                            Connection conTabla = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                                            String sqlTabla = "SELECT p.id_pedido, p.fecha_pedido, pr.nombre, p.descripcion, p.valor_total, p.metodo_pago, p.estado " +
                                                               "FROM pedidos p INNER JOIN proveedor pr ON p.id_proveedor = pr.idProveedor " +
                                                               "ORDER BY p.fecha_pedido DESC";
                                            PreparedStatement psTabla = conTabla.prepareStatement(sqlTabla);
                                            ResultSet rsTabla = psTabla.executeQuery();
                                            boolean tieneRegistros = false;
                                            
                                            while(rsTabla.next()) {
                                                tieneRegistros = true;
                                                int idPed = rsTabla.getInt("id_pedido");
                                    %>
                                                <tr>
                                                    <td class="ps-3"><%= rsTabla.getDate("fecha_pedido") %></td>
                                                    <td class="fw-bold"><%= rsTabla.getString("nombre") %></td>
                                                    <td class="text-muted"><%= rsTabla.getString("descripcion") %></td>
                                                    <td class="fw-bold text-success">$<%= String.format("%.2f", rsTabla.getDouble("valor_total")) %></td>
                                                    <td><%= rsTabla.getString("metodo_pago") %></td>
                                                    <td><span class="badge bg-success text-white px-2 py-1" style="border-radius: 5px;"><%= rsTabla.getString("estado") %></span></td>
                                                    
                                                    <td class="pe-3 text-center">
                                                        <div class="d-flex gap-1 justify-content-center">
                                                            <a href="editar_pedido.jsp?id=<%= idPed %>" class="btn btn-warning btn-sm fw-bold px-2 py-1" style="font-size: 0.8rem;">✏️ Editar</a>
                                                            <a href="${pageContext.request.contextPath}/PedidoServlet?accion=eliminar&id=<%= idPed %>" 
                                                               class="btn btn-danger btn-sm fw-bold px-2 py-1" style="font-size: 0.8rem;"
                                                               onclick="return confirm('¿Seguro que deseas eliminar este pedido?');">
                                                                🗑️ Eliminar
                                                            </a>
                                                        </div>
                                                    </td>
                                                </tr>
                                    <%
                                            }
                                            if(!tieneRegistros) {
                                    %>
                                                <tr><td colspan="7" class="text-center text-muted py-4">No hay pedidos registrados aún.</td></tr>
                                    <%
                                            }
                                            rsTabla.close(); psTabla.close(); conTabla.close();
                                        } catch(Exception e) {
                                    %>
                                            <tr><td colspan="7" class="text-center text-danger py-4">Error al cargar la tabla de pedidos.</td></tr>
                                    <%
                                        }
                                    %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>