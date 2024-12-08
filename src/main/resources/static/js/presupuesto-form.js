import {clearErrors,setErrorMessage} from './utils.js';

// Función para obtener el JWT de la cookie
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

function toggleLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    overlay.style.display = show ? 'flex' : 'none';
}

// Evento de envío del formulario
document.getElementById('procesar-solicitud').addEventListener('submit',  (event) => {
    event.preventDefault();

    toggleLoading(true);

    // Obtener el JWT de la cookie
    const jwt = getCookie('jwt');
    if (!jwt) {
        alert("No se encontró el token JWT.");
        toggleLoading(false);
        return;
    }

    // Obtener los datos del formulario
    const nombre = document.getElementById('nombre').value;
    const email = document.getElementById('email').value;
    const telefono = document.getElementById('telefono').value;
    const fechaEvento = document.getElementById('fechaEvento').value;
    const cantidadInvitados = parseInt(document.getElementById('invitados').value);
    const comentarios = document.getElementById('comentarios').value;
    const localId = parseInt(document.getElementById('localId').value);

    // Recoger los servicios seleccionados
    const serviciosSeleccionados = Array.from(document.querySelectorAll('input[name="serviciosSeleccionados"]:checked'))
        .map(cb => parseInt(cb.value));

    // Crear el objeto con los datos
    const solicitudFormDTO = {
        nombre,
        email,
        telefono,
        fechaEvento,
        cantidadInvitados,
        serviciosSeleccionados,
        comentarios,
        localId
    };

        // Enviar la solicitud usando fetch
        fetch('/matrimonio/procesar-solicitud-presupuesto', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwt}`
            },
            body: JSON.stringify(solicitudFormDTO)
        }).then(response =>{
            return response.json().then(data =>{
                // Ocultar pantalla de carga al finalizar la solicitud
                toggleLoading(false);
                clearErrors();
                if (response.ok) {
                    // Cargar los datos en el modal
                    document.getElementById('modalNombre').textContent = data.nombre;
                    document.getElementById('modalCorreo').textContent = data.correo;
                    document.getElementById('modalFechaEvento').textContent = data.fechaEvento;
                    document.getElementById('modalPresupuesto').textContent = data.presupuestoEstimado;
                    document.getElementById('modalMensajeInformativo').textContent = data.mensajeInformativo;

                    // Mostrar el modal
                    $('#resultadoSolicitudModal').modal('show');
                }else if(response.status == 400){
                    const errorType = data.errorType;

                    Swal.fire({
                        title: "Error al crear el presupuesto",
                        text: data.message,
                        icon: "error"
                    });

                    if(errorType == "VALIDATION_ERROR"){
                        for (const [field, message] of Object.entries(data.errors)) {
                            setErrorMessage(field, message);
                        }
                    }
                } else {
                    alert(`Error al enviar la solicitud: ${data.message || 'Error desconocido'}`);
                }

            });
        }).catch(error => {
            toggleLoading(false);
            console.error('Error en la solicitud:', error);
            Swal.fire({
                title: "Error al crear el presupuesto",
                text: "Verifica si tus datos son correctos",
                icon: "error"
            });
        });
});

