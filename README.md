# Herramienta de Cifrado - Aplicacion de Criptografia

Una herramienta web para cifrar y descifrar texto usando los metodos clasicos de Caesar y Vigenere. El proyecto esta desarrollado completamente en Java con Spring Boot, procesando todo en el servidor.

## Que hace esta aplicacion?

Esta aplicacion permite cifrar mensajes para ocultarlos o descifrar mensajes que ya estan cifrados. Si se conoce la clave, la aplicacion puede intentar descifrarlos automaticamente probando diferentes combinaciones.

La aplicacion maneja dos tipos de cifrado:
- **Caesar**: Desplaza cada letra por un numero fijo de posiciones
- **Vigenere**: Usa una palabra clave para desplazar cada letra de forma diferente

## Tecnologias usadas

- **Java 17** con Spring Boot como framework
- **HTMX** para las interacciones dinamicas en el frontend, que requieran datos del backend
- **Thymeleaf** para las plantillas HTML
- **Threads** Libreria de Java para procesar multiples intentos en paralelo

## Como funciona el Cifrado Caesar

Esta es la mas simple de los dos. En esta tenemos una key especifica que nos dice cuantas veces corremos el alfabeto.

### Algoritmo

```java
private String processText(String text, int shift) {
    StringBuilder result = new StringBuilder();
    for (char c : text.toUpperCase().toCharArray()) {
        int pos = alphabet.indexOf(c);
        if (pos != -1) {
            int newPos = (pos + shift) % alphabet.length();
            result.append(alphabet.charAt(newPos));
        } else {
            result.append(c); // Mantiene espacios y símbolos
        }
    }
    return result.toString();
}
```

### Ejemplo practico:
```
Texto original: HOLA MUNDO
Desplazamiento: 3
Resultado: KROD PXQGR

H -> K (H esta en posicion 7, 7+3=10, K)
O -> R (O esta en posicion 14, 14+3=17, R)
```

Para descifrar, usamos el desplazamiento inverso:

```java
public String decrypt(String text, String key) {
    int shift = Integer.parseInt(key);
    return processText(text, alphabet.length() - (shift % alphabet.length()));
}
```

en el codigo modulamos shift, para evitar errores en donde se indexa una parte invalida del alfabeto

### Fuerza Bruta en Caesar

Cuando no se conoce la clave, probamos las 26 posibilidades:

```java
private List<Callable<List<DecryptionAttempt>>> createCaesarBruteForceTasks(String text, String language) {
    List<Callable<List<DecryptionAttempt>>> tasks = new ArrayList<>();
    CipherUtility cipher = CipherFactory.createCipher("caesar");
    
    for (int shift = 0; shift < 26; shift++) {
        int finalShift = shift;
        tasks.add(() -> {
            String decrypted = cipher.decrypt(text, String.valueOf(finalShift));
            int score = CipherUtils.countCommonWords(decrypted, language);
            return Arrays.asList(new DecryptionAttempt(decrypted, String.valueOf(finalShift), score));
        });
    }
    return tasks;
}
```

Cada desplazamiento se ejecuta en un hilo separado, y después se ordenan los resultados por la cantidad de palabras reconocibles que se encontraron.

## Como funciona el Cifrado Vigenere

Este es algoritmo es mas complejo. En lugar de usar un numero fijo, usa una palabra clave que se repite (se repite si el texto es mas largo que la llave).
Esta palabra es la llave

### El Algoritmo

```java
private String processText(String text, String key, boolean encrypt) {
    StringBuilder result = new StringBuilder();
    String upperKey = key.toUpperCase();
    int keyIndex = 0;
    
    for (char c : text.toUpperCase().toCharArray()) {
        int pos = alphabet.indexOf(c);
        if (pos != -1) {
            int shift = alphabet.indexOf(upperKey.charAt(keyIndex % upperKey.length()));
            int newPos;
            if (encrypt) {
                newPos = (pos + shift) % alphabet.length();
            } else {
                newPos = (pos - shift + alphabet.length()) % alphabet.length();
            }
            result.append(alphabet.charAt(newPos));
            keyIndex++;
        } else {
            result.append(c);
        }
    }
    return result.toString();
}
```

### Ejemplo paso a paso:
```
Texto: HOLA MUNDO
Clave: GATO (se repite: GATOG ATOGA)

H + G = H(7) + G(6) = 13 -> N
O + A = O(14) + A(0) = 14 -> O  
L + T = L(11) + T(19) = 4 -> E
A + O = A(0) + O(14) = 14 -> O
```

En este tipo de cifrado cada letra usa un desplazamiento diferente segun la posicion en la clave.

### Fuerza Bruta en Vigenere

Tenemos dos metodos principales de fuerza bruta:

#### 1. Ataque por Diccionario (RockyYou)

Usa las contraseñas más comunes como posibles claves:

```java
private List<String> generateVigenereKeys(CipherForm form) {
    if ("rockyou".equals(form.getBreakMethod())) {
        int n = Math.min(form.getNumPasswords(), CipherUtils.rockyouPasswords.size());
        return CipherUtils.rockyouPasswords.subList(0, n);
    }
    // ...
}
```

#### 2. Fuerza Bruta Pura
Genera todas las combinaciones posibles de letras:

```java
// Para longitud 3: AAA, AAB, AAC, ..., ZZZ
// Total: 26³ = 17,576 combinaciones
return CipherUtils.generateKeys(length);
```

en nuestro codigo permitimos hasta 5 de longitud maxima

26⁵ = 11881376

para esta cantidad de caracteres consume 7GB de memoria ram y se demora aproximadamente 14 segundos

### Procesamiento Paralelo

Para aumentar la eficiencia en el codigo se divide el trabajo:

```java
int chunkSize = Math.max(1, keys.size() / cores);
for (int i = 0; i < keys.size(); i += chunkSize) {
    List<String> chunk = keys.subList(i, Math.min(i + chunkSize, keys.size()));
    tasks.add(() -> {
        List<DecryptionAttempt> chunkAttempts = new ArrayList<>();
        for (String key : chunk) {
            String decrypted = cipher.decrypt(text, key);
            int score = CipherUtils.countCommonWords(decrypted, language);
            chunkAttempts.add(new DecryptionAttempt(decrypted, key, score));
        }
        // Mantiene solo los mejores 100 de cada chunk
        chunkAttempts.sort((a, b) -> b.getScore() - a.getScore());
        return chunkAttempts.stream().limit(TOP_N_PER_CHUNK).collect(Collectors.toList());
    });
}
```

Si se tienen 4 núcleos y 1000 claves, cada núcleo procesa ~250 claves en paralelo.

## Sistema de Puntuacion

Para saber cual descifrado es el correcto, contamos palabras comunes:

```java
// En CipherUtils
public static int countCommonWords(String text, String language) {
    // Cuenta cuántas palabras del texto aparecen en el diccionario
    // Mas palabras reconocibles = mayor puntuacion = mejor candidato
}
```

## Validaciones Inteligentes

Una parte importante del código son las validaciones contextuales:

```java
private void validateInput(CipherForm form) {
    // Siempre valida el texto
    if (form.getText() == null || form.getText().trim().isEmpty()) {
        throw new IllegalArgumentException("El texto no puede estar vacío.");
    }
    
    // Solo valida claves cuando las necesitas
    boolean needsKey = "Encrypt".equals(form.getOperation()) || form.isUseKnownKey();
    
    if (needsKey) {
        if ("Caesar".equals(form.getCipher())) {
            if (form.getKey() < 0 || form.getKey() > 25) {
                throw new IllegalArgumentException("La clave Caesar debe estar entre 0 y 25.");
            }
        }
        // ...
    }
}
```

Esto evita errores como pedir una clave de Vigenere cuando se va a hacer fuerza bruta.

## Estructura del Codigo

El proyecto usa el patrón Factory para crear los cifradores:

```java
public static CipherUtility createCipher(String cipherType) {
    switch (cipherType.toLowerCase()) {
        case "caesar":
            return new CaesarCipher();
        case "vigenere":
            return new VigenereCipher();
        default:
            throw new IllegalArgumentException("Tipo de cifrado no soportado: " + cipherType);
    }
}
```

Y una interfaz comun para ambos:

```java
public interface CipherUtility {
    String encrypt(String text, String key);
    String decrypt(String text, String key);
}
```

## Optimizaciones de Rendimiento

- **Timeout de 30 segundos**: Evita que el servidor se cuelgue
- **Limite de resultados**: Maximo 120 intentos mostrados
- **Procesamiento por chunks**: No carga todo en memoria
- **Threads por nucleo**: Aprovecha todo el procesador

## Interfaz Web con HTMX

La página se actualiza dinámicamente sin recargar:

```html
<!-- El formulario cambia según el cifrado seleccionado -->
<select hx-get="/update-form" hx-target="#form-options">
    <option value="Caesar">Caesar</option>
    <option value="Vigenère">Vigenère</option>
</select>
```

Todo el procesamiento sigue siendo en el servidor, HTMX solo hace que la experiencia sea mas fluida.

## Como ejecutar

```bash
git clone <url-del-repositorio>
cd cipher-tool
mvn spring-boot:run
```

Despues ve a `http://localhost:8080` y empieza a cifrar.

## Ejemplos de Uso

1. **Cifrar con Caesar**: Texto="HOLA", Clave=5 → Resultado="MTRG"
2. **Descifrar Vigenère**: Texto cifrado + clave conocida
3. **Romper Caesar**: Solo el texto cifrado, prueba las 26 claves
4. **Romper Vigenere**: Usa diccionario o fuerza bruta
