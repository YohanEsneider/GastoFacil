// URL Base del servidor Tomcat en NetBeans
const BASE_URL = 'http://localhost:8080/GastoFacilWeb';

// Función auxiliar para construir encabezados con autenticación e id_tienda
const getHeaders = (extraHeaders = {}) => {
    let idTienda = localStorage.getItem('idTienda');

    // Recupera idTienda del objeto de sesión si no existe la clave individual
    if (!idTienda || idTienda === 'null' || idTienda === 'undefined') {
        const idUsuario = localStorage.getItem('idUsuario');
        if (idUsuario && idUsuario !== 'null') {
            idTienda = idUsuario;
        } else {
            const sesionStr = localStorage.getItem('usuarioSesion');
            if (sesionStr) {
                try {
                    const sesion = JSON.parse(sesionStr);
                    idTienda = sesion.idTienda || sesion.id_tienda || sesion.idUsuario || sesion.id_usuario;
                } catch (e) {
                    idTienda = null;
                }
            }
        }
    }

    return {
        'Content-Type': 'application/json',
        'X-Usuario-Id': String(idTienda || ''),
        ...extraHeaders
    };
};

const API = {
    // 0. Comprobar si el servidor Tomcat está encendido
    verificarConexion: async () => {
        try {
            const res = await fetch(`${BASE_URL}/api/dashboard`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            return res.ok;
        } catch (error) {
            return false;
        }
    },

    // 1. Autenticación / Login
    login: async (usuario, password) => {
        try {
            const res = await fetch(`${BASE_URL}/api/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ usuario, password })
            });

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.mensaje || "Credenciales inválidas");
            }

            // Almacenar el contexto de sesión único para el usuario logueado
            const idUsuarioFinal = data.idUsuario || data.id_usuario || data.id;
            const idTiendaFinal = data.idTienda || data.id_tienda || idUsuarioFinal;
            const nombreFinal = data.usuario || data.nombreUsuario || data.nombre || 'Usuario';

            localStorage.setItem('idTienda', String(idTiendaFinal));
            localStorage.setItem('idUsuario', String(idUsuarioFinal));
            localStorage.setItem('nombreUsuario', nombreFinal);
            localStorage.setItem('usuarioSesion', JSON.stringify(data));

            return data;
        } catch (error) {
            console.error("Error en login:", error);
            throw error;
        }
    },

    // 2. Registro de Usuario
    registro: async (datosUsuario) => {
        try {
            const res = await fetch(`${BASE_URL}/api/registro`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(datosUsuario)
            });
            return await res.json();
        } catch (error) {
            console.error("Error en registro:", error);
            throw error;
        }
    },

    // 3. Obtener Proveedores
    obtenerProveedores: async () => {
        try {
            const res = await fetch(`${BASE_URL}/api/proveedores`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) return [];
            const data = await res.json();
            return Array.isArray(data) ? data : [];
        } catch (error) {
            console.error("Error al obtener proveedores:", error);
            return [];
        }
    },

    // 4. Guardar Pedido / Factura Desglosada
    guardarPedido: async (datosFactura) => {
        try {
            const res = await fetch(`${BASE_URL}/api/pedidos`, {
                method: 'POST',
                headers: getHeaders(),
                credentials: 'include',
                body: JSON.stringify(datosFactura)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.mensaje || "No se pudo registrar el pedido");
            }
            return await res.json();
        } catch (error) {
            console.error("Error al guardar pedido:", error);
            throw error;
        }
    },

    // 5. Obtener Historial de Pedidos
    obtenerPedidos: async () => {
        try {
            const res = await fetch(`${BASE_URL}/api/pedidos`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) return [];
            const data = await res.json();
            return Array.isArray(data) ? data : [];
        } catch (error) {
            console.error("Error al obtener pedidos:", error);
            return [];
        }
    },

    // 6. Actualizar Pedido Existente
    actualizarPedido: async (datosPedido) => {
        try {
            const res = await fetch(`${BASE_URL}/api/pedidos`, {
                method: 'POST',
                headers: getHeaders(),
                credentials: 'include',
                body: JSON.stringify(datosPedido)
            });
            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.mensaje || "No se pudo actualizar el pedido");
            }
            return await res.json();
        } catch (error) {
            console.error("Error al actualizar pedido:", error);
            throw error;
        }
    },

    // 7. Eliminar Pedido
    eliminarPedido: async (idPedido) => {
        try {
            const res = await fetch(`${BASE_URL}/api/pedidos?id=${idPedido}`, {
                method: 'DELETE',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.mensaje || "No se pudo eliminar el pedido");
            }
            return await res.json();
        } catch (error) {
            console.error("Error al eliminar pedido:", error);
            throw error;
        }
    },

    // 8. Obtener datos y métricas para el Dashboard
    obtenerDashboard: async () => {
        try {
            const res = await fetch(`${BASE_URL}/api/dashboard`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) return null;
            return await res.json();
        } catch (error) {
            console.error("Error al obtener métricas del dashboard:", error);
            return null;
        }
    }
};