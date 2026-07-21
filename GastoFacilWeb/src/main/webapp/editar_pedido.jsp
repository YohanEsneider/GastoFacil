<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>GastoFácil - Editar Pedido</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <%
        int idPedido = Integer.parseInt(request.getParameter("id"));
        int idProveedorActual = 0;
        String fechaPedido = "", descripcion = "", metodoPago = "";
        double valorTotal = 0.0;
        
        String dbUrl = "jdbc:mysql://localhost:3306/gastofacil";
        String dbUser = "root";
        String dbPass = "Admin123*";
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            String sql = "SELECT id_proveedor, fecha_pedido, descripcion, valor_total, metodo_pago FROM pedidos WHERE id_pedido = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                idProveedorActual = rs.getInt("id_proveedor");
                fechaPedido = rs.getString("fecha_pedido");
                descripcion = rs.getString("descripcion");
                valorTotal = rs.getDouble("valor_total");
                metodoPago = rs.getString("metodo_pago");
            }
            rs.close(); ps.close(); con.close();
        } catch(Exception e) { e.printStackTrace(); }
    %>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow-sm">
                    <div class="card-header bg-warning text-dark fw-bold">
                        ✏️ Editar Pedido (Factura ID: <%= idPedido %>)
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/PedidoServlet" method="POST">
                            <input type="hidden" name="accion" value="actualizar">
                            <input type="hidden" name="idPedido" value="<%= idPedido %>">
                            
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Proveedor:</label>
                                <select class="form-select" name="idProveedor" required>
                                    <%
                                        try {
                                            Connection conPr = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                                            String sqlPr = "SELECT idProveedor, nombre FROM proveedor ORDER BY nombre ASC";
                                            PreparedStatement psPr = conPr.prepareStatement(sqlPr);
                                            ResultSet rsPr = psPr.executeQuery();
                                            while(rsPr.next()) {
                                                int idPr = rsPr.getInt("idProveedor");
                                                String seleccionado = (idPr == idProveedorActual) ? "selected" : "";
                                    %>
                                                <option value="<%= idPr %>" <%= seleccionado %>><%= rsPr.getString("nombre") %></option>
                                    <%
                                            }
                                            rsPr.close(); psPr.close(); conPr.close();
                                        } catch(Exception e) { e.printStackTrace(); }
                                    %>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold">Fecha de Compra:</label>
                                <input type="date" class="form-control" name="fechaPedido" value="<%= fechaPedido %>" required>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold">Descripción:</label>
                                <textarea class="form-control" name="descripcion" rows="3" required><%= descripcion %></textarea>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold">Valor Total:</label>
                                <input type="number" class="form-control" name="valorTotal" step="0.01" min="0" value="<%= valorTotal %>" required>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold">Método de Pago:</label>
                                <select class="form-select" name="metodoPago">
                                    <option value="Efectivo" <%= "Efectivo".equals(metodoPago) ? "selected" : "" %>>Efectivo</option>
                                    <option value="Transferencia" <%= "Transferencia".equals(metodoPago) ? "selected" : "" %>>Transferencia</option>
                                    <option value="Tarjeta" <%= "Tarjeta".equals(metodoPago) ? "selected" : "" %>>Tarjeta</option>
                                </select>
                            </div>
                            
                            <div class="d-flex gap-2 justify-content-end mt-4">
                                <a href="pedidos.jsp" class="btn btn-secondary">Cancelar</a>
                                <button type="submit" class="btn btn-warning fw-bold">Actualizar Cambios</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>