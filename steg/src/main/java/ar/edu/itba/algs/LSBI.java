package ar.edu.itba.algs;

import java.util.Map;

import ar.edu.itba.Algorithm;

import java.io.ByteArrayOutputStream;

import java.util.HashMap;

public class LSBI implements Algorithm {
    private static final int PATTERN_BYTES = 4;
    private static final int PATTERN_AMOUNT = 4;
    private static final int PATTERN_MASK = 0x6; // 110 en binario
    private static final int[] PATTERNS = { 0x0, 0x2, 0x4, 0x6 };

    private int[] patternInversion = new int[PATTERN_AMOUNT];

    @Override
    public Integer getBitsUsed() {
        return 1;
    }

    @Override
    public int embed(byte[] message, byte[] output, int offset) {
        int realOffset = offset;
        int patternOffset = realOffset + PATTERN_BYTES;

        // Verificación de límites mejorada
        if (message.length * 8 > (output.length - patternOffset) * 8 / 3) {
            throw new IllegalStateException("Output buffer overflow - insufficient space for message");
        }

        // Mapa para contar cambios por patrón
        Map<Integer, CountPair> changedPatternMap = new HashMap<>();
        for (int pattern : PATTERNS) {
            changedPatternMap.put(pattern, new CountPair(0, 0));
        }

        // Primera pasada: análisis de cambios necesarios
        int outputIndex = patternOffset;
        for (byte messageByte : message) {
            int bitIndex = 7;
            while (bitIndex >= 0) {
                if (outputIndex % 3 == 2) {
                    outputIndex++;
                    continue;
                }
                int bitToHide = (messageByte >> bitIndex) & 1;
                int bitInOutput = output[outputIndex] & 1;
                int pattern = output[outputIndex] & PATTERN_MASK;

                CountPair countPair = changedPatternMap.get(pattern);
                if (countPair == null) {

                    // Protección contra patrones inválidos
                    outputIndex++;
                    bitIndex--;
                    continue;
                }

                if (bitToHide != bitInOutput) {
                    countPair.incrementChanged();
                } else {
                    countPair.incrementUnchanged();
                }
                outputIndex++;
                bitIndex--;
            }
        }

        // Determinación de inversión por patrón
        int[] shouldInvert = new int[PATTERN_AMOUNT];
        for (int i = 0; i < PATTERN_AMOUNT; i++) {

            CountPair countPair = changedPatternMap.get(PATTERNS[i]);
            shouldInvert[i] = (countPair.getChangedCount() > countPair.getUnchangedCount()) ? 1 : 0;
        }

        // Almacenamiento de información de inversión
        for (int i = 0; i < PATTERN_AMOUNT; i++) {
            output[realOffset + i] = (byte) (PATTERNS[i] | shouldInvert[i]);
        }

        // Segunda pasada: inserción del mensaje con inversiones
        outputIndex = patternOffset;
        for (byte messageByte : message) {
            int bitIndex = 7;
            while (bitIndex >= 0) {
                if (outputIndex % 3 == 2) {
                    outputIndex++;
                    continue;
                }

                int bitToHide = (messageByte >> bitIndex) & 1;
                int pattern = output[outputIndex] & PATTERN_MASK;
                int patternIndex = pattern >> 1;

                if (patternIndex < PATTERN_AMOUNT && shouldInvert[patternIndex] == 1) {
                    bitToHide ^= 1;
                }

                output[outputIndex] = (byte) ((output[outputIndex] & ~1) | bitToHide);
                outputIndex++;
                bitIndex--;
            }
        }

        return realOffset + PATTERN_BYTES;
    }

    @Override
    public int extract(byte[] inputBytes, ByteArrayOutputStream content, int offset) {

        for (int i = 0; i < PATTERN_AMOUNT;) {
            patternInversion[i++] = inputBytes[offset++] & 1;
        }

        byte b = 0;
        for (int bitCount = 0; offset < inputBytes.length;) {
            if (offset % 3 == 2) {
                offset++;
                continue;
            }
            int pattern = inputBytes[offset] & PATTERN_MASK;
            int inversion = patternInversion[pattern >> 1];
            int bit = inputBytes[offset] & 1;

            if (inversion == 1) {
                bit ^= 1;
            }

            b <<= 1;
            b |= bit;
            bitCount++;
            if (bitCount % Byte.SIZE == 0) {
                content.write(b);
                b = 0;
                bitCount = 0;
            }
            offset++;

        }
        return offset;
    }

    @Override
    public String extractExtension(byte[] inputBytes, int offset) {
        StringBuilder extension = new StringBuilder();
        byte b = 0;
        for (int bitCount = 0; offset < inputBytes.length;) {
            if (offset % 3 == 2) {
                offset++;
                continue;
            }
            int pattern = inputBytes[offset] & PATTERN_MASK;
            int inversion = patternInversion[pattern >> 1];
            int bit = inputBytes[offset] & 1;

            if (inversion == 1) {
                bit ^= 1;
            }

            b <<= 1;
            b |= bit;
            bitCount++;
            if (bitCount % Byte.SIZE == 0) {
                extension.append((char) b);
                if (b == 0) {
                    break;
                }
                b = 0;
                bitCount = 0;
            }
            offset++;

        }
        return extension.toString();
    }
}

class CountPair {
    private int changedCount;
    private int unchangedCount;

    public CountPair(int changed, int unchanged) {
        this.changedCount = changed;
        this.unchangedCount = unchanged;
    }

    public void incrementChanged() {
        changedCount++;
    }

    public void incrementUnchanged() {
        unchangedCount++;
    }

    public int getChangedCount() {
        return changedCount;
    }

    public int getUnchangedCount() {
        return unchangedCount;
    }
}
