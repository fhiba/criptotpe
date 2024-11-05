# CriptoTPE

Este proyecto es una herramienta para realizar esteganografía en archivos BMP. Permite ocultar mensajes en archivos BMP utilizando diferentes métodos de esteganografía y recuperarlos posteriormente.

## Compilación

Para compilar el proyecto, desde la raíz de la carpeta `steg`, ejecuta:

```bash
mvn clean package
```

## Ejecución

### Para ocultar un archivo

Para embeber (ocultar) un archivo en una imagen BMP, ejecuta el siguiente comando desde la raíz de la carpeta `steg`:

```bash
mvn exec:java -Dexec.mainClass="ar.edu.itba.App" -Dexec.args="-in <file> -p <bitmapfile> --steg <steg> --out <bitmapFileOut> --embed -pass <pass> -a <alg> -m <mode>"
```

#### Parámetros requeridos

- `-embed`: Indica que se va a ocultar información.
- `-in <file>`: Especifica el archivo que se desea ocultar.
- `-p <bitmapfile>`: Imagen BMP que servirá de portadora.
- `-out <bitmapFileOut>`: Imagen BMP de salida que contendrá el archivo oculto.
- `-steg <lsb1 | lsb4 | lsbi>`: Método de esteganografía a utilizar:
  - `lsb1`: LSB de 1 bit.
  - `lsb4`: LSB de 4 bits.
  - `lsbi`: LSB mejorado.

#### Parámetros opcionales

- `-a <aes128 | aes192 | aes256 | 3des>`: Algoritmo de cifrado para el archivo oculto.
- `-m <ecb | cfb | ofb | cbc>`: Modo de cifrado.
- `-pass <password>`: Contraseña para la encriptación.

### Para extraer un archivo

Para extraer un archivo oculto de una imagen BMP, ejecuta:

```bash
mvn exec:java -Dexec.mainClass="ar.edu.itba.App" -Dexec.args="-p <bitmapfile> --steg <steg> -a <alg> -m <mode> -pass <pass> --out <file> --extract"
```

#### Parámetros requeridos

- `-extract`: Indica que se va a extraer información.
- `-p <bitmapfile>`: Imagen BMP portadora del archivo oculto.
- `-out <file>`: Archivo de salida que se generará con la información extraída.
- `-steg < lsb1 | lsb4 | lsbi >`: Método de esteganografía utilizado:
  - `lsb1`: LSB de 1 bit.
  - `lsb4`: LSB de 4 bits.
  - `lsbi`: LSB mejorado.

#### Parámetros opcionales

- `-a <aes128 | aes192 | aes256 | 3des>`: Algoritmo de cifrado.
- `-m <ecb | cfb | ofb | cbc>`: Modo de cifrado.
- `-pass <password>`: Contraseña utilizada para la encriptación.

## Testing

Dentro de la carpeta steg se encuentra un script llamado test.sh, el cual corre todas las configuraciones posibles para chequear el correcto funcionamiento de estas.

```bash
chmod +x test.sh
./test.sh
```
