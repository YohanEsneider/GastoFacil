// URL Base del servidor Tomcat en NetBeans
const BASE_URL = 'http://localhost:8080/GastoFacilWeb';

// Recupera el ID de tienda activo
const getActiveStoreId = () => {
    let idT = localStorage.getItem('idTienda');
    if (!idT || idT === 'null' || idT === 'undefined' || idT === '0') {
        idT = localStorage.getItem('idUsuario');
    }
    if (!idT || idT === 'null' || idT === 'undefined' || idT === '0') {
        try {
            const sesion = JSON.parse(localStorage.getItem('usuarioSesion') || '{}');
            idT = sesion.idTienda || sesion.id_tienda || sesion.idUsuario || sesion.id_usuario;
        } catch (e) {}
    }
    return String(idT || '0');
};

const getHeaders = (extraHeaders = {}) => {
    const storeId = getActiveStoreId();
    return {
        'Content-Type': 'application/json',
        'X-Usuario-Id': storeId,
        'idTienda': storeId,
        ...extraHeaders
    };
};

const API = {
    verificarConexion: async () => {
        try {
            const storeId = getActiveStoreId();
            const res = await fetch(`${BASE_URL}/api/dashboard?idTienda=${storeId}`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            return res.ok;
        } catch (error) { return false; }
    },

    login: async (usuario, password) => {
        try {
            const res = await fetch(`${BASE_URL}/api/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ usuario, password })
            });

            const data = await res.json();
            if (!res.ok) throw new Error(data.mensaje || "Credenciales inválidas");

            const idUsuarioFinal = data.idUsuario || data.id_usuario || data.id || 0;
            const idTiendaFinal = data.idTienda || data.id_tienda || idUsuarioFinal;
            const nombreFinal = data.nombreUsuario || data.usuario || data.nombre || 'Usuario';

            localStorage.clear();
            sessionStorage.clear();

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

    obtenerProveedores: async () => {
        try {
            const storeId = getActiveStoreId();
            const res = await fetch(`${BASE_URL}/api/proveedores?idTienda=${storeId}`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) return [];
            const data = await res.json();
            return Array.isArray(data) ? data : [];
        } catch (error) { return []; }
    },

    guardarProveedor: async (datosProveedor) => {
        try {
            const storeId = getActiveStoreId();
            const payload = { ...datosProveedor, idTienda: storeId, id_tienda: storeId };
            const res = await fetch(`${BASE_URL}/api/proveedores`, {
                method: 'POST',
                headers: getHeaders(),
                credentials: 'include',
                body: JSON.stringify(payload)
            });
            return await res.json();
        } catch (error) { throw error; }
    },

    eliminarProveedor: async (idProveedor) => {
        try {
            const storeId = getActiveStoreId();
            const res = await fetch(`${BASE_URL}/api/proveedores?id=${idProveedor}&idTienda=${storeId}`, {
                method: 'DELETE',
                headers: getHeaders(),
                credentials: 'include'
            });
            return await res.json();
        } catch (error) { throw error; }
    },

    obtenerPedidos: async () => {
        try {
            const storeId = getActiveStoreId();
            const res = await fetch(`${BASE_URL}/api/pedidos?idTienda=${storeId}`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) return [];
            const data = await res.json();
            return Array.isArray(data) ? data : [];
        } catch (error) { return []; }
    },

    guardarPedido: async (datosFactura) => {
        try {
            const storeId = getActiveStoreId();
            const payload = { ...datosFactura, idTienda: storeId, id_tienda: storeId };
            const res = await fetch(`${BASE_URL}/api/pedidos`, {
                method: 'POST',
                headers: getHeaders(),
                credentials: 'include',
                body: JSON.stringify(payload)
            });
            return await res.json();
        } catch (error) { throw error; }
    },

    actualizarPedido: async (datosPedido) => {
        try {
            const storeId = getActiveStoreId();
            const payload = { ...datosPedido, idTienda: storeId, id_tienda: storeId };
            const res = await fetch(`${BASE_URL}/api/pedidos`, {
                method: 'POST',
                headers: getHeaders(),
                credentials: 'include',
                body: JSON.stringify(payload)
            });
            return await res.json();
        } catch (error) { throw error; }
    },

    eliminarPedido: async (idPedido) => {
        try {
            const storeId = getActiveStoreId();
            const res = await fetch(`${BASE_URL}/api/pedidos?id=${idPedido}&idTienda=${storeId}`, {
                method: 'DELETE',
                headers: getHeaders(),
                credentials: 'include'
            });
            return await res.json();
        } catch (error) { throw error; }
    },

    obtenerDashboard: async () => {
        try {
            const storeId = getActiveStoreId();
            const res = await fetch(`${BASE_URL}/api/dashboard?idTienda=${storeId}`, { 
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) return null;
            return await res.json();
        } catch (error) { return null; }
    },

    obtenerHistorialAuditoria: async (buscar = '', modulo = '') => {
        try {
            const storeId = getActiveStoreId();
            const queryParams = new URLSearchParams({ buscar, modulo, idTienda: storeId }).toString();
            const res = await fetch(`${BASE_URL}/api/auditoria?${queryParams}`, {
                method: 'GET',
                headers: getHeaders(),
                credentials: 'include'
            });
            if (!res.ok) return [];
            const data = await res.json();
            return Array.isArray(data) ? data : [];
        } catch (error) { return []; }
    }
};