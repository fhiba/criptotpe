#!/bin/bash

# Input files
INPUT_FILE="../ejemplo2024sinEnc/logoitba.png"
CARRIER_FILE="../ejemplo2024sinEnc/lado.bmp"
PASSWORD="1234"

# Arrays for different parameters
STEG_ALGORITHMS=("lsb1" "lsb4" "lsbi")
ENCRYPTION_ALGORITHMS=("aes128" "aes192" "aes256" "3des")
ENCRYPTION_MODES=("ecb" "cfb" "ofb" "cbc")

# Create output directory if it doesn't exist
mkdir -p ../test_results

# Function to run a single test
run_test() {
    local steg=$1
    local enc_algo=$2
    local enc_mode=$3
    
    # Generate descriptive filenames
    local encoded_file="../test_results/${steg}_${enc_algo}_${enc_mode}_encoded.bmp"
    local decoded_file="../test_results/${steg}_${enc_algo}_${enc_mode}_decoded"
    
    echo "Testing: Steg: $steg, Encryption: $enc_algo, Mode: $enc_mode"
    
    # Embed command with password
    echo "Embedding..."
    mvn exec:java -Dexec.mainClass="ar.edu.itba.App" -Dexec.args="-in $INPUT_FILE -p $CARRIER_FILE --steg $steg -a $enc_algo -m $enc_mode -pass $PASSWORD --out $encoded_file --embed"
    
    # Check if embed was successful
    if [ $? -eq 0 ]; then
        echo "Embedding successful. Attempting extraction..."
        
        # Extract command with password
        mvn exec:java -Dexec.mainClass="ar.edu.itba.App" -Dexec.args="-p $encoded_file --steg $steg -a $enc_algo -m $enc_mode -pass $PASSWORD --out $decoded_file --extract"
        
        if [ $? -eq 0 ]; then
            echo "Extraction successful"
        else
            echo "Extraction failed"
        fi
    else
        echo "Embedding failed"
    fi
    
    echo "----------------------------------------"
}

# Main execution
echo "Starting steganography testing suite..."
echo "----------------------------------------"

# Test without encryption (base cases)
for steg in "${STEG_ALGORITHMS[@]}"; do
    echo "Testing $steg without encryption"
    
    encoded_file="../test_results/${steg}_no_encryption_encoded.bmp"
    decoded_file="../test_results/${steg}_no_encryption_decoded"
    
    # Embed (no password needed for no encryption)
    mvn exec:java -Dexec.mainClass="ar.edu.itba.App" -Dexec.args="-in $INPUT_FILE -p $CARRIER_FILE --steg $steg --out $encoded_file --embed"
    
    # Extract (no password needed for no encryption)
    mvn exec:java -Dexec.mainClass="ar.edu.itba.App" -Dexec.args="-p $encoded_file --steg $steg --out $decoded_file --extract"
    
    echo "----------------------------------------"
done

# Test all combinations with encryption
for steg in "${STEG_ALGORITHMS[@]}"; do
    for enc_algo in "${ENCRYPTION_ALGORITHMS[@]}"; do
        for enc_mode in "${ENCRYPTION_MODES[@]}"; do
            run_test "$steg" "$enc_algo" "$enc_mode"
        done
    done
done

echo "Testing completed. Results are in ../test_results directory"

# Print summary
echo "Summary of tests performed:"
echo "- Steganography algorithms tested: ${#STEG_ALGORITHMS[@]}"
echo "- Encryption algorithms tested: ${#ENCRYPTION_ALGORITHMS[@]}"
echo "- Encryption modes tested: ${#ENCRYPTION_MODES[@]}"
echo "- Total combinations tested: $((${#STEG_ALGORITHMS[@]} * (${#ENCRYPTION_ALGORITHMS[@]} * ${#ENCRYPTION_MODES[@]} + 1)))"
