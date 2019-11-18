package rest;

import entities.Role;
import entities.User;
import facades.FetchData;
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
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import utils.EMF_Creator;

/**
 * @author lam@cphbusiness.dk
 */
@Path("info")
public class DemoResource {

    private static EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory(EMF_Creator.DbSelector.DEV, EMF_Creator.Strategy.CREATE);

    @Context
    private UriInfo context;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getInfoForAll() {
        return "{\"msg\":\"Hello anonymous\"}";
    }

    //Just to verify if the database is setup
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("all")
    public String allUsers() {

        EntityManager em = EMF.createEntityManager();
        try {
            List<User> users = em.createQuery("select user from User user").getResultList();
            return "[" + users.size() + "]";
        } finally {
            em.close();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user")
    @RolesAllowed("user")
    public String getFromUser() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to User: " + thisuser + "\"}";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("admin")
    @RolesAllowed("admin")
    public String getFromAdmin() {
        String thisuser = securityContext.getUserPrincipal().getName();
        return "{\"msg\": \"Hello to (admin) User: " + thisuser + "\"}";
    }
     @GET
    @Produces(MediaType.APPLICATION_JSON)
     @Path("getdata")
    public String getSwappiData() throws MalformedURLException, IOException, InterruptedException, ExecutionException{
         
       String [] urls = {"https://swapi.co/api/people/1","https://swapi.co/api/people/2","https://swapi.co/api/people/3"};
        List<Object> fetchData = fetchData(urls);
        return fetchData.toString();
    }
        
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
    
        @Path("makeUsers")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String makeUsers() {
       EntityManagerFactory emf = EMF_Creator.createEntityManagerFactory(EMF_Creator.DbSelector.DEV, EMF_Creator.Strategy.CREATE);
    EntityManager em = emf.createEntityManager();
    
    // IMPORTAAAAAAAAAANT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // This breaks one of the MOST fundamental security rules in that it ships with default users and passwords
    // CHANGE the three passwords below, before you uncomment and execute the code below
    // Also, either delete this file, when users are created or rename and add to .gitignore
    // Whatever you do DO NOT COMMIT and PUSH with the real passwords

    User user = new User("user1", "password1");
    User admin = new User("admin2", "password2");
    User both = new User("user_admin3", "password3");

    if(admin.getUserPass().equals("test")||user.getUserPass().equals("test")||both.getUserPass().equals("test"))
      throw new UnsupportedOperationException("You have not changed the passwords");

    em.getTransaction().begin();
    Role userRole = new Role("user");
    Role adminRole = new Role("admin");
    user.addRole(userRole);
    admin.addRole(adminRole);
    both.addRole(userRole);
    both.addRole(adminRole);
    em.persist(userRole);
    em.persist(adminRole);
    em.persist(user);
    em.persist(admin);
    em.persist(both);
    em.getTransaction().commit();
    System.out.println("PW: " + user.getUserPass());
    System.out.println("Testing user with OK password: " + user.verifyPassword("test", user.getUserPass()));
    System.out.println("Testing user with wrong password: " + user.verifyPassword("qeasdqwe", user.getUserPass()));
    System.out.println("Created TEST Users");
   
  
        return "{\"Done\"}";  //Done manually so no need for a DTO
    }
    }
    


