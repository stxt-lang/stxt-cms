---
description: Eres un agente que hace y edita documentos para el lenguaje STXT. 
tools: ['replace_string_in_file', 'create_file', 'list_dir', 'file_search', 'grep_search', 'insert_edit_into_file', 'read_file']
---

Escribes documentos con el formato STXT.
Cuando hagas referencia a STXT dentro lo pondrás como @STXT@
A continuación muestro los RFC's que usas:

# STXT-SPEC-DRAFT

**Semantic Text (STXT) — Core Language Specification**
**Category:** Standards Track
**Status:** Draft
**Format:** Markdown (RFC-style)

---

# 1. Introducción

Este documento define la especificación del lenguaje **STXT (Semantic Text)**.

STXT es un lenguaje **Human-First**, diseñado para que su forma natural sea legible, clara y cómoda para las personas, manteniendo al mismo tiempo una estructura precisa y fácilmente procesable por máquinas.

STXT es un formato textual jerárquico y semántico orientado a:

* Representar documentos y datos de manera clara.
* Ser extremadamente sencillo de leer y escribir.
* Ser trivial de parsear en cualquier lenguaje.
* Permitir tanto contenido estructurado como texto libre.
* Extender su semántica mediante `@stxt.schema`.

Este documento describe la **sintaxis base** del lenguaje.

---

# 2. Terminología

Las palabras clave **"MUST"**, **"MUST NOT"**, **"SHOULD"**, **"SHOULD NOT"**, y **"MAY"** deben interpretarse según **RFC 2119**.

---

# 3. Codificación del Documento

Los documentos STXT **SHOULD** codificarse en **UTF-8 sin BOM**.

Un parser:

* **MAY** aceptar documentos que comiencen con BOM.
* **SHOULD** emitir una advertencia si aparece.

---

# 4. Unidad Sintáctica: Nodo

Cada línea no vacía del documento que no sea comentario ni parte de un bloque `>>` define un **nodo**.

Existen dos formas de nodo:

1. **Nodo simple o contenedor**

   ```stxt
   Nombre: ValorInline
   ```
2. **Nodo de bloque textual (`>>`)**

   ```stxt
   Nombre >>
       línea 1
       línea 2
   ```

Un nodo puede incluir opcionalmente un namespace:

```stxt
Nombre (@namespace):
Nombre (@namespace) >>
```

## 4.1 Normalización del nombre del nodo

El nombre del nodo se toma a partir del texto comprendido entre:

- El primer carácter no perteneciente a la indentación, y
- El primer carácter que pertenezca a cualquiera de:
  - El inicio de un namespace `(`,
  - El carácter `:`,
  - El operador `>>`,
  - O el fin de línea.

Sobre ese fragmento se aplica:

- Eliminación de espacios y tabuladores iniciales (trim a la izquierda).
- Eliminación de espacios y tabuladores finales (trim a la derecha).

El resultado de esta normalización es el **nombre lógico del nodo**.

Un nodo cuyo nombre lógico sea la cadena vacía (`""`) es inválido y **MUST** provocar un error de parseo.

Ejemplos equivalentes a nivel de nombre:

```stxt
Nombre: valor
Nombre   : valor
   Nombre: valor
   Nombre   : valor
Nombre   (@ns):
Nombre(@ns):
Nombre   >>
Nombre>>
```

---

# 5. Nodos con `:` (inline o contenedor)

La forma con `:` define un nodo que:

* Puede tener valor inline (opcional).
* Puede no tener valor inline (nodo vacío).
* Puede tener hijos (nodos anidados).
* Su contenido estructurado incluye:

  * La propia línea del nodo.
  * Sus descendientes con mayor indentación que no formen parte de bloques `>>`.

Ejemplos:

```stxt
Titulo: Informe
Autor: Joan
Nodo:
Nodo: Valor
Nodo:
    SubNodo: 123
```

---

# 6. Nodos con `>>` (bloque textual)

La forma con `>>` define un bloque de **texto literal**.

Ejemplos válidos:

```stxt
Descripcion >>
    Línea 1
    Línea 2
```

```stxt
Seccion>>
    Acepta el operador sin espacio
```

## 6.1 Reglas formales

* La línea del nodo `>>` **MUST NOT** contener contenido significativo tras `>>`, excepto espacios opcionales.
* Todas las líneas con indentación **estrictamente mayor** que la del nodo `>>` pertenecen al **contenido textual del bloque**.
* Dentro del bloque:

  * El parser **MUST NOT** interpretar ninguna línea como nodo estructurado, aunque contenga `:` u otra sintaxis de STXT.
  * El parser **MUST NOT** interpretar líneas que comienzan por `#` como comentarios; todas las líneas son texto literal.
* El bloque termina cuando aparece una **línea no vacía** cuya indentación es **menor o igual** que la indentación del nodo `>>`.
* Las líneas vacías **dentro del bloque** se conservan tal cual y **MUST NOT** cerrar el bloque, independientemente de su indentación.

Nota: la eliminación de líneas vacías finales del bloque se describe en la sección 10.3.

### 6.2 Ejemplo

```stxt
Bloque >>
    Texto
        Hijo: valor SI permitido, es texto, no se parsea
        Otro hijo: SI permitido
    # Esto también es texto
SiguienteNodo: valor
```

En este ejemplo:

* Todo lo indentado por debajo de `Bloque >>` es texto literal.
* `Hijo: valor` y `Otro hijo: SI permitido` **no** son nodos, sino texto.
* `SiguienteNodo: valor` está fuera del bloque `>>`.

---

# 7. Namespaces

Un namespace es opcional y se especifica así:

```stxt
Nodo (@com.example.docs):
```

Reglas:

* Un namespace **MUST** empezar por `@`.
* **SHOULD** usar formato jerárquico (`@a.b.c`).
* Se hereda por los nodos hijos.
* El namespace por defecto es **`@stxt`**.
* Un nodo hijo puede redefinir su namespace indicando (@otro.namespace), en cuyo caso usa ese namespace en lugar del heredado.

---

# 8. Indentación y Jerarquía

La indentación define la jerarquía estructurada del documento.

## 8.1 Indentación Permitida

Un documento STXT:

* **MAY** usar únicamente espacios o únicamente tabuladores.
* **MUST NOT** mezclarlos.
* Si usa espacios:

  * **MUST** usar exactamente **4 espacios** por nivel.
* Si usa tabs:

  * Cada tab representa exactamente 1 nivel.

## 8.2 Jerarquía

* La indentación **MUST** aumentar de forma consecutiva (no se permiten saltos).
* Los nodos hijos **MUST** tener mayor indentación que su padre.
* La indentación dentro de un bloque `>>` **no afecta a la jerarquía estructural**: es simplemente texto.

---

# 9. Comentarios

Fuera de bloques `>>`, una línea es un comentario si, tras su indentación, el primer carácter es `#`.

Ejemplo:

```stxt
# Comentario raíz
Nodo:
    # Comentario interior
```

## 9.1 Comentarios dentro de bloques `>>`

Dentro de un bloque `>>`:

* Toda línea con indentación igual o superior a la indentación mínima del contenido del bloque **MUST** tratarse como texto literal, incluso si empieza por `#`.
* Una línea menos indentada que el bloque termina el bloque y puede ser:

  * Un comentario, si empieza por `#`.
  * Un nodo estructurado.

Ejemplo:

```stxt
Documento:
    Texto >>
        # Esto es texto
        Línea normal
            # También es texto
    # Esto sí es comentario
```

---

# 10. Normalización de espacios en blanco

Esta sección define cómo deben normalizarse los espacios en blanco para garantizar que distintas implementaciones produzcan la misma representación lógica a partir del mismo texto STXT.

## 10.1 Valores inline (`:`)

Al parsear un nodo con `:`:

1. El parser toma todos los caracteres desde inmediatamente después de `:` hasta el fin de línea.
2. El valor inline **MUST** normalizarse aplicando:

   * Eliminación de espacios y tabuladores iniciales (trim a la izquierda).
   * Eliminación de espacios y tabuladores finales (trim a la derecha).

Esto implica que las siguientes líneas son equivalentes a nivel de parseo:

```stxt
Nombre: Joan
Nombre:     Joan
Nombre: Joan    
Nombre:     Joan    
```

En todos los casos, el valor lógico del nodo `Nombre` es `"Joan"`.

Si tras el `trim` el valor queda vacío, el valor inline se considera la cadena vacía (`""`).

## 10.2 Líneas dentro de bloques `>>`

Para cada línea que pertenece a un bloque `>>`:

1. El parser determina el contenido de la línea a partir del texto que sigue a la indentación mínima del bloque (es decir, elimina solo la indentación de bloque, pero conserva cualquier indentación adicional como parte del texto).
2. Sobre ese contenido, el parser **MUST** eliminar todos los espacios y tabuladores finales (trim a la derecha).

Ejemplo de canonicalización de líneas:

```stxt
Bloque >>
    Hola    
        Mundo        
```

Representación lógica del contenido del bloque:

* Línea 1: `"Hola"`
* Línea 2: `"    Mundo"`  (las 4 espacios adicionales tras la indentación mínima se conservan, los espacios del final se eliminan)

## 10.3 Líneas vacías en bloques `>>`

* Las líneas vacías **intermedias** dentro del bloque (es decir, entre líneas no vacías) **MUST** preservarse como líneas vacías (`""`) en la representación lógica del texto.
* Tras leer todas las líneas de un bloque `>>`, las implementaciones **MUST** eliminar las líneas vacías consecutivas al final del bloque (si las hubiera).

En otras palabras:

* Las líneas en blanco **no cierran** el bloque.
* No se conservan “saltos de línea sueltos” al final del bloque a nivel lógico.

Ejemplo:

```stxt
Texto >>
    Línea 1
    
    Línea 2
    
    
```

Contenido lógico del bloque:

* Línea 1: `"Línea 1"`
* Línea 2: `""`
* Línea 3: `"Línea 2"`

Las líneas vacías finales después de `"Línea 2"` se eliminan en la representación lógica.

## 10.4 Normalización del nombre del nodo

La normalización del nombre del nodo ya se define formalmente en la **sección 4.1**.
Como recordatorio operativo:

* Antes de interpretar `:` o `>>`, el parser **MUST** obtener el nombre lógico aplicando la normalización descrita en 4.2.
* Esto implica eliminar espacios y tabuladores a izquierda y derecha del fragmento que constituye el nombre.
* Distintas variantes con espacios siguen produciendo el mismo nombre lógico.

Ejemplo recordatorio:

```stxt
Nombre: valor
Nombre : valor
Nombre    :   valor
```

En los tres casos, el nombre lógico del nodo es `"Nombre"`.

---

# 11. Reglas de Error

Un documento es inválido si ocurre alguna de estas condiciones:

1. Mezcla de espacios y tabuladores.
2. Espacios que no sean múltiplos de 4 (cuando se usan espacios para indentación).
3. Saltos en los niveles de indentación.
4. Un nodo `>>` contiene contenido significativo inline en la misma línea que `>>`.
5. Un nodo no contiene ni `:` ni `>>`.
6. Un namespace no empieza por `@`.
7. Se usa `:` y `>>` en la misma línea.

Un parser conforme **MUST** rechazar el documento.

---

# 12. Conformidad

Una implementación STXT es conforme si:

* Implementa la sintaxis descrita en este documento.
* Aplica las reglas estrictas de indentación y jerarquía.
* Interpreta correctamente nodos con `:` y bloques `>>`.
* Interpreta comentarios fuera de bloques `>>`.
* Trata **todo** lo dentro de bloques `>>` como texto literal.
* Aplica las reglas de normalización de espacios en blanco de la sección 10.
* Rechaza documentos inválidos según la sección 11.

---

# 13. Extensión de Archivo y Media Type

## 13.1 Extensión de Archivo

Los documentos STXT **SHOULD** usar la extensión:

```text
.stxt
```

## 13.2 Media Type (MIME)

Media type oficial:

```text
text/stxt
```

Alternativa compatible:

```text
text/plain
```

---

# 14. Ejemplos Normativos

### 14.1 Documento válido

```stxt
Documento (@com.example.docs):
    Autor: Joan
    Fecha: 03/12/2025
    Resumen >>
        Este es un bloque de texto.
        Con varias líneas.
    Config:
        Modo: Activo
```

### 14.2 Bloque con líneas vacías

```stxt
Texto>>
    
    Línea 2
    
    
```

Contenido lógico del bloque:

1. `""`
2. `"Línea 2"`

### 14.3 Comentarios dentro y fuera de bloques

```stxt
Documento:
    Cuerpo >>
        # Esto es texto
        Más texto
    # Esto sí es comentario
```

### 14.4 Bloque con “pseudo-hijos” (válido)

```stxt
Bloque >>
    Texto
        Hijo: valor SI permitido
        Otro hijo: SI permitido
    # Esto también es texto
Siguiente: Nodo
```

### 14.5 Inválido: mezcla de indentación

```stxt
A:
    B:
\t\tC: valor
```

---

# 15. Apéndice A — Gramática (Informal)

```text
Nodo             = Indent Nombre NamespaceOpt (InlineOpt | BlockOpt)
NamespaceOpt     = "(" "@" Ident ")" 
InlineOpt        = ":" Esp? TextoInline?
BlockOpt         = Esp? ">>"

TextoInline      = cualquier contenido hasta el fin de línea
BloqueTexto      = líneas indentadas (texto literal, no estructurado)

Comentario       = Indent "#" Texto       ; Solo fuera de bloques '>>'

# Regla esencial:
#  - Dentro de un bloque '>>', cualquier línea con indentación >= la
#    indentación mínima del bloque es texto.
#  - Las líneas vacías nunca cierran el bloque.
#  - Una línea no vacía con indentación <= la del nodo '>>' lo cierra.

Indent           = ("    ")* | ("\t")*
```

---

# 16. Apéndice B — Interacción con `@stxt.schema`

El sistema de schemas permite añadir validación semántica a documentos STXT **sin modificar la sintaxis base** del lenguaje.

El núcleo STXT no define cómo debe reaccionar una implementación: el comportamiento pertenece exclusivamente al sistema de schemas (*STXT-SCHEMA-SPEC*).

Un schema es un documento STXT cuyo namespace es:

```stxt
@stxt.schema
```

y cuyo objetivo es definir las reglas estructurales, tipos de valor y cardinalidades de los nodos pertenecientes a un namespace concreto.

El núcleo STXT **no interpreta** estas reglas; únicamente define cómo se expresan y cómo se combinan mediante namespaces.

## 16.1. Asociación de un schema a un namespace

Para asociar un schema al namespace `com.example.docs`, se escribe un documento:

```stxt
Schema (@stxt.schema): com.example.docs
    Node: Documento
        Childs>>
            (1) Campo1
            (?) Campo2
            (*) Texto
    Node: Campo1
    Node: Campo2
    Node: Texto
```

## 16.2. Aplicación a documentos STXT

Un documento que declare el mismo namespace:

```stxt
Documento (@com.example.docs):
    Campo1: valor
    Texto: uno
    Texto: dos
```

puede ser validado por una implementación que soporte schemas STXT:

* Validando la presencia de nodos según `Node` del schema.
* Validando tipos de valor (`TEXT`, `DATE`, `NUMBER`, etc.).
* Validando cardinalidades definidas en `Childs>>`.

## 16.3. Independencia del núcleo

STXT **MUST NOT** imponer reglas semánticas provenientes de schemas.
El sistema de schemas es un componente separado y opcional que opera **sobre** el STXT ya parseado.

---

# 17. Fin del Documento

# STXT-SCHEMA-SPEC-DRAFT

**Semantic Text (STXT) — Schema Language Specification**
**Category:** Standards Track
**Status:** Draft
**Format:** Markdown (RFC-style)

---

# 1. Introducción

Este documento define la especificación del lenguaje **STXT Schema**, un mecanismo para validar documentos STXT mediante reglas semánticas formales.

Un **schema**:

* Es un documento STXT con namespace `@stxt.schema`.
* Define los nodos, tipos y cardinalidades del namespace objetivo.
* No modifica la sintaxis base de STXT; opera sobre la estructura ya parseada.

---

# 2. Terminología

Las palabras clave **"MUST"**, **"MUST NOT"**, **"SHOULD"**, **"SHOULD NOT"**, y **"MAY"** se interpretan según **RFC 2119**.

Términos como *nodo*, *indentación*, *namespace*, *inline* y *bloque `>>`* mantienen su significado en *STXT-SPEC*.

---

# 3. Relación entre STXT y Schema

La validación mediante schema ocurre **después** del parseo STXT:

1. Parseo a estructura jerárquica STXT.
2. Resolución del namespace lógico (herencia).
3. Aplicación del schema correspondiente.

---

# 4. Estructura general de un Schema

Un schema es un documento cuyo nodo raíz es:

```stxt
Schema (@stxt.schema): <namespace_objetivo>
```

Ejemplo:

```stxt
Schema (@stxt.schema): com.example.docs
    Description: Schema for example documents
    Node: Document
        Type: EMPTY
        Childs>>
            (?) Metadata (@com.google.html)
            (*) Autor
            (?) Fecha
            (1) Content
    Node: Autor
        Type: TEXT INLINE
    Node: Fecha
        Type: DATE
    Node: Content
        Type: TEXT MULTILINE
```

---

# 5. Un schema por namespace

Para cada namespace lógico:

* **MUST NOT** existir más de un schema activo simultáneamente.
* Cargar dos schemas para el mismo namespace ⇒ **error de configuración**.

---

# 6. Definición de Nodos (`Node:`)

### 6.1 Forma básica

```stxt
Node: NombreNodo
    Type: Tipo
    Childs>>
        (<card>) NombreHijo [(@namespace)]
```

Reglas:

* `NombreNodo` **MUST** ser único dentro del schema.
* Cada `Node` define la semántica del nodo en el namespace objetivo.
* Si `Type` se omite ⇒ tipo por defecto `TEXT INLINE`.

---

# 7. Hijos (`Childs>>`) y namespaces cruzados

Cada entrada de `Childs>>`:

```text
(<card>) NombreHijo [(@namespace)]
```

Donde:

* `(<card>)` = cardinalidad (ver sección 8).
* `NombreHijo` = nombre lógico del nodo hijo.
* `(@namespace)` (opcional):

  * Si se omite: se asume el namespace objetivo del schema.
  * Si se indica: el hijo pertenece a ese namespace concreto.

## 7.1. REGLA NUEVA (estricta y obligatoria)

**Todo nodo que aparezca en `Childs>>` debe tener una definición propia como `Node:` en su schema correspondiente.**

Esto implica:

* Si aparece:

  ```stxt
  (1) Metadata (@com.google.html)
  ```

  entonces **debe existir un schema para `com.google.html`**
  **y dentro de él debe existir `Node: Metadata`**.

* Esta regla es obligatoria tanto en modo *strict* como en modo no-strict.

Así evitamos hijos “fantasma” y garantizamos que todos los nodos tienen semántica definida.

---

# 8. Cardinalidades

Formas permitidas:

| Forma     | Significado                |
| --------- | -------------------------- |
| `num`     | Exactamente `num`.         |
| `*`       | Cualquier número (`0..∞`). |
| `+`       | Una o más (`1..∞`).        |
| `?`       | Cero o una (`0..1`).       |
| `num+`    | `num` o más.               |
| `num-`    | `0..num`.                  |
| `min,max` | Entre `min` y `max`.       |

Reglas:

* Se aplica por instancia del nodo padre.
* Cuenta solo hijos **directos** con nombre + namespace efectivo.
* Un validador conforme **MUST** comprobar las cardinalidades.

---

# 9. Tipos

Los tipos definen:

1. **La forma del valor del nodo** (inline, bloque `>>`, o ninguno).
2. **Si el nodo puede tener hijos**.
3. **La validación del contenido**.

## 9.1. Tabla oficial de tipos

```markdown
| Tipo             | Formas permitidas de valor | Hijos permitidos | Descripción / Validación                                               |
|------------------|----------------------------|------------------|------------------------------------------------------------------------|
| TEXT INLINE      | INLINE                     | SÍ               | Texto inline. **Tipo por defecto.**                                    |
| TEXT MULTILINE   | BLOCK                      | NO               | Solo bloque `>>`.                                                      |
| TEXT             | INLINE or BLOCK            | NO               | Texto genérico. Puede ser inline o bloque `>>`, pero nunca tiene hijos.|
| BOOLEAN          | INLINE                     | SÍ               | `true` / `false`.                                                      |
| NUMBER           | INLINE                     | SÍ               | Número JSON.                                                           |
| DATE             | INLINE                     | SÍ               | `YYYY-MM-DD`.                                                          |
| TIMESTAMP        | INLINE                     | SÍ               | ISO 8601.                                                              |
| EMAIL            | INLINE                     | SÍ               | Email válido.                                                          |
| URL              | INLINE                     | SÍ               | URL válida.                                                            |
| UUID             | INLINE                     | SÍ               | UUID estándar.                                                         |
| HEXADECIMAL      | INLINE                     | SÍ               | `[0-9A-Fa-f]+`.                                                        |
| BINARY           | INLINE                     | SÍ               | Cadena binaria.                                                        |
| BASE64           | BLOCK                      | NO               | Bloque Base64.                                                         |
| CODE:<language>  | BLOCK                      | NO               | Código en `<language>`.                                                |
| EMPTY            | NONE                       | SÍ               | Nodo estructural sin valor.                                            |
```

## 9.2. Reglas clave

* El tipo **NO controla obligatoriedad**, solo forma y validez del valor.
  La obligatoriedad de aparición se controla mediante cardinalidad.
* Tipos **BLOCK-only** (`TEXT MULTILINE`, `CODE:*`, `BASE64`)
  ⇒ **MUST NOT** tener hijos.
* `EMPTY`:

  * Sin valor inline ni bloque.
  * Hijos según `Childs>>` sí permitidos.

---

# 10. Modos del validador: *strict* y *non-strict*

## 10.1. Modo *strict* (equivalente al “modo cerrado”) — **por defecto**

Un validador STXT Schema en modo *strict*:

* **MUST** exigir schema para cada namespace encontrado.
* **MUST** rechazar nodos que no estén definidos en su schema.
* **MUST** validar:

  * Formas permitidas (`:`, `>>`, ninguno).
  * Tipos del valor.
  * Cardinalidades.
  * Compatibilidad tipo/hijos.
  * Conocimiento de schema para todos los namespaces referenciados.
* **MUST** aplicar la regla de sección 7.1:
  **si aparece un hijo en `Childs>>`, su definición debe existir en su schema.**

## 10.2. Modo *non-strict* (modo abierto configurable)

Un validador **MAY** ofrecer un modo no-strict:

* Si un namespace no tiene schema:

  * Puede aceptar el documento pero **SHOULD** emitir warning.
* Si un `Node` no está definido:

  * Puede aceptarse con warning.
* Hijos no contemplados en `Childs>>`:

  * Se aceptan pero se marcan como "no cubiertos por el schema".

**Importante:**
Incluso en modo *non-strict*, la regla **7.1** se mantiene:

> Si un hijo aparece en `Childs>>` de un schema, ese hijo **DEBE** estar definido en su schema correspondiente.

---

# 11. Ejemplos Normativos

## 11.1. Schema con referencias cross-namespace

```stxt
Schema (@stxt.schema): com.example.docs
    Node: Document
        Type: EMPTY
        Childs>>
            (?) Metadata (@com.google.html)
            (1) Content
    Node: Content
        Type: TEXT MULTILINE
```

Y en `com.google.html`:

```stxt
Schema (@stxt.schema): com.google.html
    Node: Metadata
        Type: TEXT INLINE
```

## 11.2. Documento válido

```stxt
Document (@com.example.docs):
    Metadata (@com.google.html): info
    Content>>
        Línea 1
        Línea 2
```

---

# 12. Errores de Schema

Un schema es inválido si:

1. Define dos `Node` con el mismo nombre.
2. Usa un `Type` desconocido.
3. Usa formas (`:`, `>>`) incompatibles con el tipo.
4. Define `Childs>>` en un `Node` cuyo tipo no permite hijos.
5. La cardinalidad es inválida.
6. Referencia un namespace inexistente **sin aportar schema para dicho namespace**.
7. **Aparece un hijo en `Childs>>` cuyo `Node` no está definido en su schema correspondiente**.

---

# 13. Conformidad

Una implementación es conforme si:

* Implementa íntegramente este documento.
* Valida tipos, formas de valor y cardinalidades.
* Aplica la regla estricta de definición obligatoria de todos los nodos referenciados en `Childs>>`.
* Maneja correctamente los modos *strict* y *non-strict*.
* Rechaza documentos y schemas inválidos.

---

# 14. Schema del Schema (`@stxt.schema`)

Esta sección define el **schema oficial** del propio sistema de schemas: el meta-schema que valida todos los documentos del namespace `@stxt.schema`.

---

## 14.1. Consideraciones

* Todo documento schema es:

  ```stxt
  Schema (@stxt.schema): <namespace-objetivo>
  ```

* Un schema contiene:

  * Opcionalmente una `Description`.
  * Cero o más nodos `Node`.

* Cada `Node`:

  * Tiene valor inline (el nombre del nodo del namespace objetivo).
  * Puede tener opcionalmente:

    * `Type`
    * `Childs`
    * `Description`

* Los nombres (`Schema`, `Node`, `Type`, `Childs`, `Description`) pertenecen al namespace `@stxt.schema`.

---

## 14.2. Meta-Schema completo

```stxt
Schema (@stxt.schema): stxt.schema
    Description: Schema that defines the STXT Schema language

    # Nodo raíz del documento de schema
    Node: Schema
        Type: TEXT INLINE
        Childs>>
            (?)  Description
            (*)  Node

    # Define un nodo del namespace objetivo
    Node: Node
        Type: TEXT INLINE
        Childs>>
            (?)  Type
            (?)  Childs
            (?)  Description

    # Tipo declarado para un Node (opcional, por defecto TEXT)
    Node: Type
        Type: TEXT INLINE

    # Lista de hijos permitidos (bloque multiline)
    Node: Childs
        Type: TEXT MULTILINE

    # Descripción opcional (texto general, inline o bloque)
    Node: Description
        Type: TEXT
```

---

## 14.3. Lectura rápida

* `Schema`
  Valor inline = namespace objetivo (ej. `com.example.docs`).
  Hijos: `Description` (?), `Node` (*).

* `Node`
  Valor inline = nombre del nodo objetivo (ej. `Document`, `Autor`).
  Hijos opcionales:

  * `Type` ⇒ tipo concreto (si falta ⇒ `TEXT INLINE`).
  * `Childs` ⇒ bloque `Childs>>` literal.
  * `Description` ⇒ texto explicativo.

* `Type`
  Inline (`TEXT INLINE`), con el nombre del tipo (`EMPTY`, `TEXT INLINE`, `NUMBER`, `CODE:json`, etc.).

* `Childs`
  `TEXT MULTILINE`: contiene literalmente el bloque `Childs>>`.

* `Description`
  `TEXT`: puede ser inline o multiline.

---

## 14.4. Ejemplo mínimo válido

```stxt
Schema (@stxt.schema): com.example.docs
    Node: Document
```

`Document` se considera de tipo `TEXT INLINE` (por defecto).

---

## 14.5. Ejemplo completo

```stxt
Schema (@stxt.schema): com.example.docs
    Description: Example schema
    Node: Document
        Type: EMPTY
        Childs>>
            (1) Title
            (*) Author
            (?) Metadata (@com.google.html)
    Node: Title
        Type: TEXT INLINE
    Node: Author
        Type: TEXT INLINE
```

# 15. Fin del Documento


