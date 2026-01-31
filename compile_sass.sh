#!/bin/bash

# Detectar si el script se ejecuta dentro de un terminal o no
if [[ -t 1 ]]; then
    # Estamos en una terminal, ejecutar normalmente
    echo "Ejecutando en terminal..."
else
    # No estamos en una terminal, abrir Konsole y ejecutar el script dentro
    konsole -e bash -c "$0; exit"
    exit 0
fi

# Hacer que el script se detenga en caso de error
set -e

# Ir al directorio donde está el script
cd "$(dirname "$0")"

# Ejecutar node-sass para compilar SCSS a CSS
sass scss:static/css --style=compressed

