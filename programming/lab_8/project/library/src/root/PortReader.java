package root;

import java.io.*;

public class PortReader {
    public static int readPort(String path) throws IOException {
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException("Ошибка! Файл с номером порта не найден.");
        if (!file.canRead())
            throw new IOException("Ошибка! Нет прав на чтение файла с номером порта.");
        try (BufferedReader bf = new BufferedReader(new FileReader(file))) {
            return Integer.parseInt(bf.readLine());
        }
    }
}