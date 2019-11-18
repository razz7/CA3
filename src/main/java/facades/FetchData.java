/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facades;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.json.Json;
import static org.eclipse.persistence.sessions.remote.corba.sun.TransporterHelper.id;

/**
 *
 * @author Rumle
 */
public class FetchData {

    
    public List<Object> fetchData(String[] URL) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(URL.length);

        List<Future<String>> list = new ArrayList<Future<String>>();

        for (int i = 0; i < URL.length; i++) {
            Future future = executor.submit(new Fetching(URL[i]));
            list.add(future);

        }
        List<Object> dataList = new ArrayList<>();
        for (Future f : list) {
            
                dataList.add(f.get());

            

            

        }
        executor.shutdown();
        return dataList;
    }

    class Fetching implements Callable<String> {

        private String URL;

        public Fetching(String URL) {
            this.URL = URL;
        }

        @Override
        public String call() throws Exception {
            return getFetch(URL);

        }

        private String getFetch(String URL) throws MalformedURLException, ProtocolException, IOException {

            URL url = new URL(URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json;charset=UTF-8");
            con.setRequestProperty("User-Agent", "server"); //remember if you are using SWAPI
            Scanner scan = new Scanner(con.getInputStream());
            String jsonStr = null;
            if (scan.hasNext()) {
                jsonStr = scan.nextLine();
            }
            scan.close();
            return jsonStr;
        }
    }
}

