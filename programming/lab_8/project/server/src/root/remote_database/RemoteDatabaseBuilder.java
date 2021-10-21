package root.remote_database;

import java.io.*;

public class RemoteDatabaseBuilder {
    public static RemoteDatabase newRemoteDatabase(String path) throws IOException {
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException("Ошибка загрузки! Файл не найден.");
        if (!file.canRead())
            throw new IOException("Ошибка загрузки! Нет прав на чтение файла.");
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String URL = null, login = null, password = null;

        String[] line = new String[2];
        while ((line[0] = bf.readLine()) != null) {
            line = line[0].split(": ");
            switch (line[0]) {
                case "URL":
                    URL = line[1];
                    break;
                case "login":
                    login = line[1];
                    break;
                case "password":
                    password = line[1];
            }
        }

        bf.close();
        return new RemoteDatabase(URL, login, password);
    }
}