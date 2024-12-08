
import {clearErrors,setErrorMessage} from './utils.js';
document.getElementById('registerForm').addEventListener('submit', function (e) {
    e.preventDefault();
    const $name = document.getElementById('name').value;
    const $lastname = document.getElementById('lastname').value;
    const $email = document.getElementById('email').value;
    const $password = document.getElementById('password').value;
    const $address = document.getElementById('address').value;
    const $phone = document.getElementById('phone').value;

    const registerData = {
        name: $name,
        lastname: $lastname,
        email: $email,
        password: $password,
        address: $address,
        phone: $phone,
        isEnabled: true,
        isCredentialNoExpired: true,
        isAccountNoLocked: true,
        isAccountNoExpired: true
    };

    fetch('/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(registerData)
    })
        .then(response => {
            return response.json().then(data => {
                clearErrors();
                if (response.ok) {
                    // Manejo del éxito
                    console.log('Respuesta del servidor:', data);
                    Swal.fire({
                        title: "Usuario Creado exitosamente",
                        text: "Haga click en el boton de abajo para redirigirse al login",
                        icon: "success",
                        confirmButtonText: 'Ir a Login'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/login';
                        }
                    });
                } else {
                    if(response.status == 400 || response.status == 409 ){
                        const errorType = data.errorType;
                        Swal.fire({
                            title: "Error al crear el usuario",
                            text: data.message,
                            icon: "error"
                        });

                        if(errorType == "VALIDATION_ERROR"){

                            for (const [field, message] of Object.entries(data.errors)) {
                                setErrorMessage(field, message);
                            }
                        }else if(errorType == "EMAIL_DUPLICATE"){
                            setErrorMessage("email", data.message);
                        }
                    } else{
                        console.error('Error en la autenticación:', data);
                        alert('Error en la autenticación: ' + (data.message || 'Error desconocido'));
                        Swal.fire({
                            title: "Error al crear el usuario",
                            text: "Verifica si tus datos son correctos",
                            icon: "error"
                        });
                    }
                }
            });
        }).catch(error => {
        console.error('Error en la solicitud:', error);
        Swal.fire({
            title: "Error al crear el usuario",
            text: "Verifica si tus datos son correctos",
            icon: "error"
        });
    });


});