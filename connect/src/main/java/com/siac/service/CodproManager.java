package com.siac.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CodproManager {

    private static final String FILE_PATH = "last_codpro.txt"; // Caminho do arquivo para salvar o último CODPRO

    // Carregar o último CODPRO verificado
    public static String loadLastCodpro() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            return reader.readLine(); // Retorna o valor salvo
        } catch (IOException e) {
            return "000000"; // Valor inicial se o arquivo não existir ou houver erro
        }
    }

    // Salvar o último CODPRO verificado
    public static void saveLastCodpro(String codpro) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(codpro);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

