console.log("Is OK");
// URL del recurso al que deseas hacer la petición
const url = 'test/users';

// Realizar la petición GET
fetch(url)
    .then(response => {
        if (!response.ok) {
            throw new Error('Error en la respuesta');
        }
        return response.json();
    })
    .then(data => {
        console.log('Datos recibidos:', data);
    })
    .catch(error => {
        console.error('Error al realizar la petición:', error);
    });
